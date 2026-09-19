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
}
