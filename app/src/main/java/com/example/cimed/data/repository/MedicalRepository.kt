package com.example.cimed.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.util.Log
import com.example.cimed.data.local.LocalCaseStorage
import com.example.cimed.data.models.*
import com.example.cimed.data.network.SupabaseClient
import com.example.cimed.utils.NetworkUtils
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object MedicalRepository {
    private const val TAG = "MedicalRepository"
    private val _allCases = MutableStateFlow<List<ClinicalCase>>(emptyList())
    val allCases: StateFlow<List<ClinicalCase>> = _allCases.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    private val _pendingCount = MutableStateFlow(0)
    val pendingCount: StateFlow<Int> = _pendingCount.asStateFlow()

    private var localStorage: LocalCaseStorage? = null
    private var appContext: Context? = null
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    fun init(context: Context) {
        if (appContext != null) return // Evitar doble inicialización
        appContext = context.applicationContext
        val storage = LocalCaseStorage(appContext!!)
        localStorage = storage

        // 1. Cargar casos guardados localmente
        val cachedCases = storage.getCachedCases()
        if (cachedCases.isNotEmpty()) {
            _allCases.value = cachedCases
            Log.d(TAG, "Inicialización: Cargados ${cachedCases.size} casos desde la caché local.")
        }

        // 2. Cargar número de casos pendientes
        _pendingCount.value = storage.getPendingCases().size

        // 3. Registrar callback de conectividad para auto-sincronizar en segundo plano cuando vuelva el internet
        try {
            val connectivityManager = appContext?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            connectivityManager?.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    Log.d(TAG, "Conexión a internet restablecida. Intentando sincronizar cola de casos pendientes...")
                    repositoryScope.launch {
                        syncPendingCases()
                    }
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error al registrar NetworkCallback", e)
        }
    }

    suspend fun fetchAllCases() {
        val context = appContext
        val storage = localStorage

        // Asegurar que haya datos locales si _allCases está vacío
        if (_allCases.value.isEmpty() && storage != null) {
            val cached = storage.getCachedCases()
            if (cached.isNotEmpty()) {
                _allCases.value = cached
            }
        }

        // Intentar primero sincronizar cualquier envío pendiente antes de descargar
        val hasNetwork = context != null && NetworkUtils.isNetworkAvailable(context)
        if (hasNetwork) {
            syncPendingCases()
        } else {
            _isOffline.value = true
            Log.w(TAG, "Sin conexión a internet. Utilizando datos locales guardados.")
            if (_allCases.value.isNotEmpty()) {
                return // Éxito en modo offline
            } else {
                throw Exception("No hay conexión a internet ni datos locales guardados.")
            }
        }

        try {
            Log.d(TAG, "Conectando a Supabase...")
            val response = SupabaseClient.client.postgrest["casos_clinicos"].select(
                Columns.raw("*, signos_vitales(*), estudios(*), procedimientos(*), preguntas_quiz(*)")
            )
            
            val results = response.decodeList<ClinicalCase>()
            Log.d(TAG, "Descarga completada de Supabase: ${results.size} casos.")
            
            _allCases.value = results
            _isOffline.value = false
            
            // Guardar en la caché local
            storage?.saveCases(results)
        } catch (e: Exception) {
            Log.e(TAG, "Error en fetchAllCases desde red: ${e.message}")
            _isOffline.value = true
            
            if (_allCases.value.isNotEmpty()) {
                Log.i(TAG, "Usando ${_allCases.value.size} casos en caché local tras fallo de red.")
            } else {
                throw e
            }
        }
    }

    fun getCases(): List<ClinicalCase> = _allCases.value
    fun getCaseById(id: Int): ClinicalCase? = _allCases.value.find { it.id == id }
    fun getFolders(): List<String> = _allCases.value.map { it.folder }.distinct().sortedBy { if (it == "Sin carpeta") 1 else 0 }

    fun updateCaseFolder(caseId: Int?, targetFolder: String) {
        if (caseId == null) return
        val updated = _allCases.value.map {
            if (it.id == caseId) it.copy(folder = targetFolder) else it
        }
        _allCases.value = updated
        localStorage?.saveCases(updated)
    }

    fun reorderCases(newOrder: List<ClinicalCase>) {
        _allCases.value = newOrder
        localStorage?.saveCases(newOrder)
    }

    suspend fun updateCase(clinicalCase: ClinicalCase, vitalSigns: VitalSigns, studies: List<Study> = emptyList()) {
        val context = appContext
        val storage = localStorage
        val hasNetwork = context != null && NetworkUtils.isNetworkAvailable(context)
        val caseWithDetails = clinicalCase.copy(vitalSigns = vitalSigns, studies = studies)

        if (hasNetwork && clinicalCase.id != null) {
            try {
                SupabaseClient.client.postgrest["casos_clinicos"].update(caseWithDetails) {
                    filter {
                        eq("id", clinicalCase.id)
                    }
                }
                SupabaseClient.client.postgrest["signos_vitales"].update(mapOf(
                    "frecuencia_cardiaca" to vitalSigns.frecuencia_cardiaca,
                    "presion_arterial" to vitalSigns.presion_arterial,
                    "frecuencia_respiratoria" to vitalSigns.frecuencia_respiratoria,
                    "temperatura" to vitalSigns.temperatura,
                    "saturacion" to vitalSigns.saturacion,
                    "bis" to vitalSigns.bis,
                    "etco2" to vitalSigns.etco2
                )) {
                    filter {
                        eq("id_caso", clinicalCase.id)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error actualizando en Supabase, guardando cambios localmente.", e)
            }
        }

        val currentList = _allCases.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == clinicalCase.id }

        if (index >= 0) {
            currentList[index] = caseWithDetails
        } else {
            currentList.add(0, caseWithDetails)
        }

        _allCases.value = currentList
        storage?.saveCases(currentList)
    }

    fun addStudyToCase(caseId: Int?, study: Study) {
        if (caseId == null) return
        val currentList = _allCases.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == caseId }
        if (index >= 0) {
            val target = currentList[index]
            val updatedStudies = target.studies.toMutableList().apply { add(study) }
            val updatedCase = target.copy(studies = updatedStudies)
            currentList[index] = updatedCase
            _allCases.value = currentList
            localStorage?.saveCases(currentList)
        }
    }

    suspend fun insertCase(clinicalCase: ClinicalCase, vitalSigns: VitalSigns, studies: List<Study> = emptyList()) {
        val context = appContext
        val storage = localStorage
        val hasNetwork = context != null && NetworkUtils.isNetworkAvailable(context)
        val caseWithDetails = clinicalCase.copy(vitalSigns = vitalSigns, studies = studies)

        if (hasNetwork) {
            try {
                syncPendingCases()

                val insertedCase = SupabaseClient.client.postgrest["casos_clinicos"]
                    .insert(caseWithDetails) {
                        select()
                    }.decodeSingle<ClinicalCase>()

                val caseId = insertedCase.id ?: throw Exception("Error al obtener ID del caso insertado")

                SupabaseClient.client.postgrest["signos_vitales"].insert(mapOf(
                    "id_caso" to caseId,
                    "frecuencia_cardiaca" to vitalSigns.frecuencia_cardiaca,
                    "presion_arterial" to vitalSigns.presion_arterial,
                    "frecuencia_respiratoria" to vitalSigns.frecuencia_respiratoria,
                    "temperatura" to vitalSigns.temperatura,
                    "saturacion" to vitalSigns.saturacion,
                    "bis" to vitalSigns.bis,
                    "etco2" to vitalSigns.etco2
                ))

                fetchAllCases()
                return
            } catch (e: Exception) {
                Log.e(TAG, "Error en envío directo a Supabase. Se guardará en cola local para reintento.", e)
            }
        }

        // MODO SIN CONEXIÓN O FALLO DE RED: Guardar en la cola local de pendientes
        if (storage != null) {
            val pending = PendingCase(
                clinicalCase = caseWithDetails,
                vitalSigns = vitalSigns
            )
            storage.addPendingCase(pending)

            val updatedList = _allCases.value.toMutableList().apply { add(0, caseWithDetails) }
            _allCases.value = updatedList
            storage.saveCases(updatedList)

            _pendingCount.value = storage.getPendingCases().size
            _isOffline.value = true
        } else {
            throw IllegalStateException("Almacenamiento local no inicializado.")
        }
    }

    suspend fun syncPendingCases() {
        val context = appContext
        val storage = localStorage ?: return
        if (context == null || !NetworkUtils.isNetworkAvailable(context)) return

        val pendingList = storage.getPendingCases()
        if (pendingList.isEmpty()) {
            _pendingCount.value = 0
            return
        }

        Log.d(TAG, "Iniciando sincronización de ${pendingList.size} casos pendientes a Supabase...")

        var syncedCount = 0
        for (pending in pendingList) {
            try {
                val insertedCase = SupabaseClient.client.postgrest["casos_clinicos"]
                    .insert(pending.clinicalCase) {
                        select()
                    }.decodeSingle<ClinicalCase>()

                val caseId = insertedCase.id
                if (caseId != null) {
                    SupabaseClient.client.postgrest["signos_vitales"].insert(mapOf(
                        "id_caso" to caseId,
                        "frecuencia_cardiaca" to pending.vitalSigns.frecuencia_cardiaca,
                        "presion_arterial" to pending.vitalSigns.presion_arterial,
                        "frecuencia_respiratoria" to pending.vitalSigns.frecuencia_respiratoria,
                        "temperatura" to pending.vitalSigns.temperatura,
                        "saturacion" to pending.vitalSigns.saturacion,
                        "bis" to pending.vitalSigns.bis,
                        "etco2" to pending.vitalSigns.etco2
                    ))

                    storage.removePendingCase(pending.idTemp)
                    syncedCount++
                    Log.d(TAG, "Caso '${pending.clinicalCase.title}' subido exitosamente a Supabase.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sincronizando caso pendiente '${pending.clinicalCase.title}'", e)
            }
        }

        _pendingCount.value = storage.getPendingCases().size
        if (syncedCount > 0) {
            try {
                val response = SupabaseClient.client.postgrest["casos_clinicos"].select(
                    Columns.raw("*, signos_vitales(*), estudios(*), procedimientos(*), preguntas_quiz(*)")
                )
                val results = response.decodeList<ClinicalCase>()
                _allCases.value = results
                _isOffline.value = false
                storage.saveCases(results)
            } catch (e: Exception) {
                Log.e(TAG, "Error refrescando catálogo tras sincronización", e)
            }
        }
    }

    suspend fun deleteCase(caseId: Int) {
        val context = appContext
        if (context == null || !NetworkUtils.isNetworkAvailable(context)) {
            _isOffline.value = true
            throw IllegalStateException("Se requiere conexión a internet para eliminar casos clínicos.")
        }

        try {
            Log.d(TAG, "Eliminando caso $caseId en Supabase...")
            SupabaseClient.client.postgrest["casos_clinicos"].delete {
                filter {
                    eq("id", caseId)
                }
            }
            
            fetchAllCases()
        } catch (e: Exception) {
            Log.e(TAG, "Error eliminando caso $caseId", e)
            throw e
        }
    }
}
