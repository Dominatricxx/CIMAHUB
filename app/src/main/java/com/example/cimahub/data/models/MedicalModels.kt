package com.example.cimahub.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class VitalSigns(
    @SerialName("frecuencia_cardiaca") val heartRate: Int = 0,
    @SerialName("presion_arterial") val bloodPressure: String = "0/0",
    @SerialName("frecuencia_respiratoria") val respiratoryRate: Int = 0,
    val temperatura: Float = 0f,
    val saturacion: Int = 0,
    val bis: Int = 0,
    val etco2: Int = 0
)

@Serializable
data class Study(
    val nombre: String = "",
    val url: String = ""
)

@Serializable
data class Procedure(
    val nombre: String = "",
    @SerialName("url_video") val videoUrl: String = ""
)

@Serializable
data class QuizQuestion(
    val pregunta: String = "",
    val opciones: List<String> = emptyList(),
    @SerialName("indice_respuesta_correcta") val correctAnswerIndex: Int = 0,
    val explicacion: String = ""
)

@Serializable
data class ClinicalCase(
    val id: Int,
    @SerialName("titulo") val title: String,
    @SerialName("resumen") val summary: String = "",
    @SerialName("carpeta") val folder: String = "Sin carpeta",
    val anamnesis: String = "",
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
    
    // Cambiado de List a Objeto único (1:1)
    @SerialName("signos_vitales") val vitalSigns: VitalSigns? = null,
    
    // Estos se mantienen como listas (1:N)
    @SerialName("estudios") val studies: List<Study> = emptyList(),
    @SerialName("procedimientos") val procedures: List<Procedure> = emptyList(),
    @SerialName("preguntas_quiz") val quiz: List<QuizQuestion> = emptyList()
)
