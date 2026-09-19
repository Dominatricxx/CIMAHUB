package com.example.cimahub.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class VitalSigns(
    @SerialName("frecuencia_cardiaca") val frecuencia_cardiaca: Int = 0,
    @SerialName("presion_arterial") val presion_arterial: String = "0/0",
    @SerialName("frecuencia_respiratoria") val frecuencia_respiratoria: Int = 0,
    @SerialName("temperatura") val temperatura: Float = 0f,
    @SerialName("saturacion") val saturacion: Int = 0,
    @SerialName("bis") val bis: Int = 0,
    @SerialName("etco2") val etco2: Int = 0
)

@Serializable
data class Study(
    @SerialName("nombre") val nombre: String = "",
    @SerialName("url") val url: String = ""
)

@Serializable
data class Procedure(
    @SerialName("nombre") val nombre: String = "",
    @SerialName("url_video") val url_video: String = ""
)

@Serializable
data class QuizQuestion(
    @SerialName("pregunta") val pregunta: String = "",
    @SerialName("opciones") val opciones: List<String> = emptyList(),
    @SerialName("indice_respuesta_correcta") val indice_respuesta_correcta: Int = 0,
    @SerialName("explicacion") val explicacion: String = ""
)

@Serializable
data class ClinicalCase(
    val id: Int? = null,
    @SerialName("titulo") val title: String,
    @SerialName("resumen") val summary: String = "",
    @SerialName("carpeta") val folder: String = "Sin carpeta",
    @SerialName("anamnesis") val anamnesis: String = "",
    @SerialName("exploracion_fisica") val physicalExamination: String = "",
    @SerialName("url_simulacion") val simulationUrl: String = "",
    @SerialName("id_punto_interes") val hotspotId: String? = null,
    @SerialName("nombre_paciente") val patientName: String = "Paciente Desconocido",
    @SerialName("sexo_paciente") val patientSex: String = "No especificado",
    @SerialName("edad_paciente") val patientAge: Int = 0,
    @SerialName("peso_paciente") val patientWeight: Float = 0f,
    @SerialName("altura_paciente") val patientHeight: Int = 0,
    @SerialName("motivo_paciente") val patientMotive: String = "",
    @SerialName("antecedentes_paciente") val patientHistory: String = "",
    
    // Corregido: signos_vitales es un objeto (1:1), NO una lista
    @SerialName("signos_vitales") val vitalSigns: VitalSigns? = null,
    
    @SerialName("estudios") val studies: List<Study> = emptyList(),
    @SerialName("procedimientos") val procedures: List<Procedure> = emptyList(),
    @SerialName("preguntas_quiz") val quiz: List<QuizQuestion> = emptyList()
)
