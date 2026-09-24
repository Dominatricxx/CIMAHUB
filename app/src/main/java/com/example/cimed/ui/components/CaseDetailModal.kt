package com.example.cimed.ui.components

import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cimed.data.models.ClinicalCase
import com.example.cimed.data.models.VitalSigns

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaseDetailModal(
    selectedCase: ClinicalCase?,
    visionMatrix: FloatArray? = null,
    isTeacher: Boolean = false,
    onDismiss: () -> Unit,
    onStartSimulation: (Int) -> Unit,
    onEditCase: ((Int) -> Unit)? = null,
    onDeleteCase: ((Int) -> Unit)? = null
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
                visionMatrix = visionMatrix,
                isTeacher = isTeacher,
                onStartSimulation = {
                    showBottomSheet = false
                    onStartSimulation(selectedCase.id ?: 0)
                },
                onEditCase = {
                    val caseId = selectedCase.id
                    showBottomSheet = false
                    if (caseId != null && onEditCase != null) {
                        onEditCase(caseId)
                    }
                },
                onDeleteCase = {
                    val caseId = selectedCase.id
                    if (caseId != null && onDeleteCase != null) {
                        showBottomSheet = false
                        onDeleteCase(caseId)
                    }
                }
            )
        }
    }
}

@Composable
fun CaseDetailContent(
    clinicalCase: ClinicalCase,
    visionMatrix: FloatArray?,
    isTeacher: Boolean = false,
    onStartSimulation: () -> Unit,
    onEditCase: () -> Unit = {},
    onDeleteCase: () -> Unit = {}
) {
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Eliminar Registro de Caso") },
            text = { Text("¿Estás seguro de que deseas eliminar permanentemente los datos del caso '${clinicalCase.title}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteCase()
                    }
                ) {
                    Text("Eliminar", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
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
        
        Box(modifier = Modifier.heightIn(max = 360.dp)) {
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
                    
                    clinicalCase.vitalSigns?.let { 
                        VitalSignsGrid(it)
                    } ?: Text("Signos vitales no disponibles", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Button(
            onClick = onStartSimulation,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00723F)),
            shape = RoundedCornerShape(50.dp)
        ) {
            Icon(Icons.Default.PlayCircle, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Iniciar Simulación", fontWeight = FontWeight.Bold)
        }

        if (isTeacher) {
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onEditCase,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A8F5A)),
                shape = RoundedCornerShape(50.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Modificar Datos del Caso", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = { showDeleteConfirmDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(0xFFFFEBEE),
                    contentColor = Color(0xFFC62828)
                ),
                border = BorderStroke(1.dp, Color(0xFFFFCDD2)),
                shape = RoundedCornerShape(50.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFC62828))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Eliminar Datos / Caso Clínico", fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
            }
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
        Text(text = label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}
