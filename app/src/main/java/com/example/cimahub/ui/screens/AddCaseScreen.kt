package com.example.cimahub.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cimahub.data.models.ClinicalCase
import com.example.cimahub.data.models.VitalSigns
import com.example.cimahub.ui.viewmodel.MedicalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCaseScreen(
    viewModel: MedicalViewModel,
    onBack: () -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var resumen by remember { mutableStateOf("") }
    var carpeta by remember { mutableStateOf("Sin carpeta") }
    var anamnesis by remember { mutableStateOf("") }
    var exploracionFisica by remember { mutableStateOf("") }
    
    // Signos Vitales
    var fc by remember { mutableStateOf("") }
    var ta by remember { mutableStateOf("") }
    var fr by remember { mutableStateOf("") }
    var temp by remember { mutableStateOf("") }
    var sat by remember { mutableStateOf("") }
    var bis by remember { mutableStateOf("") }
    var etco2 by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Caso Clínico", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (titulo.isNotBlank()) {
                            val clinicalCase = ClinicalCase(
                                title = titulo,
                                summary = resumen,
                                folder = carpeta,
                                anamnesis = anamnesis,
                                physicalExamination = exploracionFisica
                            )
                            val vitals = VitalSigns(
                                frecuencia_cardiaca = fc.toIntOrNull() ?: 0,
                                presion_arterial = ta,
                                frecuencia_respiratoria = fr.toIntOrNull() ?: 0,
                                temperatura = temp.toFloatOrNull() ?: 0f,
                                saturacion = sat.toIntOrNull() ?: 0,
                                bis = bis.toIntOrNull() ?: 0,
                                etco2 = etco2.toIntOrNull() ?: 0
                            )
                            viewModel.addCase(clinicalCase, vitals) { success ->
                                if (success) onBack()
                            }
                        }
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Guardar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1A8F5A),
                    navigationIconContentColor = Color(0xFF1A8F5A)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Información Básica", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A8F5A))
            
            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título del caso") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = resumen,
                onValueChange = { resumen = it },
                label = { Text("Resumen / Descripción corta") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = carpeta,
                onValueChange = { carpeta = it },
                label = { Text("Categoría / Carpeta") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Detalles Clínicos", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A8F5A))

            OutlinedTextField(
                value = anamnesis,
                onValueChange = { anamnesis = it },
                label = { Text("Anamnesis") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            OutlinedTextField(
                value = exploracionFisica,
                onValueChange = { exploracionFisica = it },
                label = { Text("Exploración Física") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Text("Signos Vitales iniciales", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A8F5A))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = fc,
                    onValueChange = { fc = it },
                    label = { Text("FC (lpm)") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = ta,
                    onValueChange = { ta = it },
                    label = { Text("TA (e.g. 120/80)") },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = fr,
                    onValueChange = { fr = it },
                    label = { Text("FR (rpm)") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = temp,
                    onValueChange = { temp = it },
                    label = { Text("Temp (°C)") },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = sat,
                    onValueChange = { sat = it },
                    label = { Text("SpO2 (%)") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = etco2,
                    onValueChange = { etco2 = it },
                    label = { Text("EtCO2") },
                    modifier = Modifier.weight(1f)
                )
            }

            Button(
                onClick = {
                    if (titulo.isNotBlank()) {
                        val clinicalCase = ClinicalCase(
                            title = titulo,
                            summary = resumen,
                            folder = carpeta,
                            anamnesis = anamnesis,
                            physicalExamination = exploracionFisica
                        )
                        val vitals = VitalSigns(
                            frecuencia_cardiaca = fc.toIntOrNull() ?: 0,
                            presion_arterial = ta,
                            frecuencia_respiratoria = fr.toIntOrNull() ?: 0,
                            temperatura = temp.toFloatOrNull() ?: 0f,
                            saturacion = sat.toIntOrNull() ?: 0,
                            bis = bis.toIntOrNull() ?: 0,
                            etco2 = etco2.toIntOrNull() ?: 0
                        )
                        viewModel.addCase(clinicalCase, vitals) { success ->
                            if (success) onBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00723F)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar Caso Clínico", fontWeight = FontWeight.Bold)
            }
        }
    }
}
