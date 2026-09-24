package com.example.cimed.data.models

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class PendingCase(
    val idTemp: String = UUID.randomUUID().toString(),
    val clinicalCase: ClinicalCase,
    val vitalSigns: VitalSigns,
    val timestamp: Long = System.currentTimeMillis()
)
