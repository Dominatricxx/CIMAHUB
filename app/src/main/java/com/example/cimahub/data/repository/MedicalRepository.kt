package com.example.cimahub.data.repository

import android.util.Log
import com.example.cimahub.data.models.*
import com.example.cimahub.data.network.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.MutableStateFlow

object MedicalRepository {
    private const val TAG = "MedicalRepository"
    private val _allCases = MutableStateFlow<List<ClinicalCase>>(emptyList())

    suspend fun fetchAllCases() {
        try {
            Log.d(TAG, "Conectando a Supabase...")
            
            val response = SupabaseClient.client.postgrest["casos_clinicos"].select(
                Columns.raw("*, signos_vitales(*), estudios(*), procedimientos(*), preguntas_quiz(*)")
            )
            
            val results = response.decodeList<ClinicalCase>()
            
            Log.d(TAG, "Descarga completada: ${results.size} casos.")
            _allCases.value = results
        } catch (e: Exception) {
            Log.e(TAG, "Error en fetchAllCases: ${e.message}")
            throw e
        }
    }

    fun getCases(): List<ClinicalCase> = _allCases.value
    fun getCaseById(id: Int): ClinicalCase? = _allCases.value.find { it.id == id }
    fun getFolders(): List<String> = _allCases.value.map { it.folder }.distinct().sortedBy { if (it == "Sin carpeta") 1 else 0 }

    suspend fun insertCase(clinicalCase: ClinicalCase, vitalSigns: VitalSigns) {
        try {
            // 1. Insertar Caso Clínico
            val insertedCase = SupabaseClient.client.postgrest["casos_clinicos"]
                .insert(clinicalCase) {
                    select()
                }.decodeSingle<ClinicalCase>()

            val caseId = insertedCase.id ?: throw Exception("Error al obtener ID del caso insertado")

            // 2. Insertar Signos Vitales relacionados
            // Necesitamos asegurarnos de que vitalSigns tenga el ID del caso
            // Pero en la base de datos la tabla signos_vitales tiene id_caso como PK
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

            // Refrescar lista local
            fetchAllCases()
        } catch (e: Exception) {
            Log.e(TAG, "Error insertando caso", e)
            throw e
        }
    }

    fun getCases(): List<ClinicalCase> = _allCases.value
    fun getCaseById(id: Int): ClinicalCase? = _allCases.value.find { it.id == id }
    fun getFolders(): List<String> = _allCases.value.map { it.folder }.distinct().sortedBy { if (it == "Sin carpeta") 1 else 0 }
}
