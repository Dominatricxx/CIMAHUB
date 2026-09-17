package com.example.cimahub.data.models

data class VitalSigns(
    val heartRate: Int,
    val bloodPressure: String,
    val respiratoryRate: Int,
    val temperature: Float,
    val saturation: Int,
    val bis: Int = 0,
    val etco2: Int = 0
)

data class Study(
    val name: String,
    val url: String
)

data class Procedure(
    val name: String,
    val videoUrl: String
)

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String
)

data class ClinicalCase(
    val id: Int,
    val title: String,
    val summary: String,
    val folder: String,
    val anamnesis: String,
    val physicalExamination: String,
    val vitalSigns: VitalSigns,
    val simulationUrl: String,
    val hotspotId: String? = null,
    
    // Nuevos campos del segmento HTML
    val patientName: String = "Paciente Desconocido",
    val patientSex: String = "No especificado",
    val patientAge: Int = 0,
    val patientWeight: Float = 0f,
    val patientHeight: Int = 0,
    val patientMotive: String = "",
    val patientHistory: String = "",
    val studies: List<Study> = emptyList(),
    val procedures: List<Procedure> = emptyList(),
    val quiz: List<QuizQuestion> = emptyList()
)

data class Folder(
    val name: String,
    val cases: List<ClinicalCase>
)
