package com.example.cimahub.data.network

import android.util.Log
import com.example.cimahub.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.serialization.json.Json

object SupabaseClient {
    private const val TAG = "SupabaseClient"
    private const val SUPABASE_URL = BuildConfig.SUPABASE_URL
    private const val SUPABASE_KEY = BuildConfig.SUPABASE_KEY

    private val jsonInstance = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    val client by lazy {
        val url = if (SUPABASE_URL.isNotBlank()) SUPABASE_URL else "https://placeholder.supabase.co"
        createSupabaseClient(
            supabaseUrl = url,
            supabaseKey = SUPABASE_KEY
        ) {
            httpEngine = OkHttp.create()
            defaultSerializer = KotlinXSerializer(jsonInstance)
            install(Postgrest)
            Log.d(TAG, "Cliente Supabase inicializado con URL: $url")
        }
    }
}
