package com.example.cimed.data.local

import android.content.Context
import android.util.Log
import com.example.cimed.data.models.ClinicalCase
import com.example.cimed.data.models.PendingCase
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class LocalCaseStorage(private val context: Context) {
    private val tag = "LocalCaseStorage"
    private val cacheFile = File(context.filesDir, "casos_cache.json")
    private val pendingFile = File(context.filesDir, "pending_cases.json")

    private val jsonInstance = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        prettyPrint = true
    }

    fun saveCases(cases: List<ClinicalCase>) {
        try {
            val jsonString = jsonInstance.encodeToString(cases)
            cacheFile.writeText(jsonString)
            Log.d(tag, "Guardados ${cases.size} casos en caché local: ${cacheFile.absolutePath}")
        } catch (e: Exception) {
            Log.e(tag, "Error guardando casos en caché local", e)
        }
    }

    fun getCachedCases(): List<ClinicalCase> {
        // 1. Intentar cargar desde el archivo en filesDir (casos_cache.json)
        if (cacheFile.exists() && cacheFile.length() > 0) {
            try {
                val jsonString = cacheFile.readText()
                val cases = jsonInstance.decodeFromString<List<ClinicalCase>>(jsonString)
                if (cases.isNotEmpty()) {
                    Log.d(tag, "Cargados ${cases.size} casos desde la caché local.")
                    return cases
                }
            } catch (e: Exception) {
                Log.e(tag, "Error leyendo caché local, intentando desde assets", e)
            }
        }

        // 2. Si no hay caché o falló, cargar desde assets/default_cases.json
        try {
            val assetJson = context.assets.open("default_cases.json").bufferedReader().use { it.readText() }
            val cases = jsonInstance.decodeFromString<List<ClinicalCase>>(assetJson)
            Log.d(tag, "Cargados ${cases.size} casos por defecto desde assets.")
            if (cases.isNotEmpty()) {
                saveCases(cases)
            }
            return cases
        } catch (e: Exception) {
            Log.e(tag, "Error cargando casos desde assets/default_cases.json", e)
        }

        return emptyList()
    }

    // --- COLA DE CASOS PENDIENTES DE SINCRONIZAR ---

    fun getPendingCases(): List<PendingCase> {
        if (!pendingFile.exists() || pendingFile.length() == 0L) return emptyList()
        return try {
            val jsonString = pendingFile.readText()
            jsonInstance.decodeFromString<List<PendingCase>>(jsonString)
        } catch (e: Exception) {
            Log.e(tag, "Error leyendo casos pendientes", e)
            emptyList()
        }
    }

    fun savePendingCases(pendingList: List<PendingCase>) {
        try {
            val jsonString = jsonInstance.encodeToString(pendingList)
            pendingFile.writeText(jsonString)
            Log.d(tag, "Guardados ${pendingList.size} casos pendientes en cola local.")
        } catch (e: Exception) {
            Log.e(tag, "Error guardando lista de casos pendientes", e)
        }
    }

    fun addPendingCase(pendingCase: PendingCase) {
        val current = getPendingCases().toMutableList()
        current.add(pendingCase)
        savePendingCases(current)
    }

    fun removePendingCase(idTemp: String) {
        val current = getPendingCases().filter { it.idTemp != idTemp }
        savePendingCases(current)
    }
}
