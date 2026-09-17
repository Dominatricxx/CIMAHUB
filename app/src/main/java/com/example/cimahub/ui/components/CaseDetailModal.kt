package com.example.cimahub.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cimahub.data.models.ClinicalCase
import com.example.cimahub.data.models.VitalSigns

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaseDetailModal(
    selectedCase: ClinicalCase?,
    onDismiss: () -> Unit,
    onStartSimulation: (Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(selectedCase) {
        showBottomSheet = selectedCase != null
    }

    if (showBottomSheet && selectedCase != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
                onDismiss()
            },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            CaseDetailContent(
                clinicalCase = selectedCase,
                onStartSimulation = {
                    showBottomSheet = false
                    onStartSimulation(selectedCase.id)
                }
            )
        }
    }
}

@Composable
fun CaseDetailContent(
    clinicalCase: ClinicalCase,
    onStartSimulation: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 40.dp)
    ) {
        Text(
            text = "Detalles del Caso Clínico", 
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = clinicalCase.title, 
            style = MaterialTheme.typography.headlineMedium, 
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A8F5A)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Box(modifier = Modifier.heightIn(max = 400.dp)) {
            LazyColumn {
                item {
                    DetailSection("Información Clínica", clinicalCase.anamnesis)
                    DetailSection("Exploración Física", clinicalCase.physicalExamination)
                    
                    Text(
                        text = "Signos Vitales", 
                        style = MaterialTheme.typography.titleSmall, 
                        color = Color(0xFF1A8F5A),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    VitalSignsGrid(clinicalCase.vitalSigns)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onStartSimulation,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A8F5A)),
            shape = RoundedCornerShape(50.dp)
        ) {
            Icon(Icons.Default.PlayCircle, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Iniciar Simulación", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DetailSection(title: String, content: String) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            text = title, 
            style = MaterialTheme.typography.titleSmall, 
            color = Color(0xFF1A8F5A),
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = content, 
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 20.sp,
            color = Color.DarkGray
        )
    }
}

@Composable
fun VitalSignsGrid(vitals: VitalSigns) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                VitalItem("TA", vitals.bloodPressure)
                VitalItem("FC", "${vitals.heartRate} lpm")
                VitalItem("FR", "${vitals.respiratoryRate} rpm")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                VitalItem("Temp", "${vitals.temperature} °C")
                VitalItem("SpO2", "${vitals.saturation} %")
                Box(Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun RowScope.VitalItem(label: String, value: String) {
    Column(modifier = Modifier.weight(1f)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}
