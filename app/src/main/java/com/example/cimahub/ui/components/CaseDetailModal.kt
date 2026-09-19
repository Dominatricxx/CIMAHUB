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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import android.graphics.Paint
import android.graphics.ColorMatrixColorFilter
import com.example.cimahub.data.models.ClinicalCase
import com.example.cimahub.data.models.VitalSigns

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaseDetailModal(
    selectedCase: ClinicalCase?,
    visionMatrix: FloatArray? = null,
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawWithContent {
                        if (visionMatrix == null) {
                            drawContent()
                        } else {
                            drawIntoCanvas { canvas ->
                                val paint = Paint().apply {
                                    colorFilter = ColorMatrixColorFilter(visionMatrix)
                                }
                                canvas.nativeCanvas.saveLayer(null, paint)
                                drawContent()
                                canvas.nativeCanvas.restore()
                            }
                        }
                    }
            ) {
                CaseDetailContent(
                    clinicalCase = selectedCase,
                    onStartSimulation = {
                        showBottomSheet = false
                        onStartSimulation(selectedCase.id ?: 0)
                    }
                )
            }
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
            color = MaterialTheme.colorScheme.primary
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
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    clinicalCase.vitalSigns?.let { 
                        VitalSignsGrid(it)
                    } ?: Text("Signos vitales no disponibles", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onStartSimulation,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
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
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = content, 
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 20.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun VitalSignsGrid(vitals: VitalSigns) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                VitalItem("TA", vitals.presion_arterial)
                VitalItem("FC", "${vitals.frecuencia_cardiaca} lpm")
                VitalItem("FR", "${vitals.frecuencia_respiratoria} rpm")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                VitalItem("Temp", "${vitals.temperatura} °C")
                VitalItem("SpO2", "${vitals.saturacion} %")
                Box(Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun RowScope.VitalItem(label: String, value: String) {
    Column(modifier = Modifier.weight(1f)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}
