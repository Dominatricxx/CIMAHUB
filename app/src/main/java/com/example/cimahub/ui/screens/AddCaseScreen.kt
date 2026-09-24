package com.example.cimahub.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cimahub.data.models.ClinicalCase
import com.example.cimahub.data.models.Study
import com.example.cimahub.data.models.VitalSigns
import com.example.cimahub.data.repository.MedicalRepository
import com.example.cimahub.ui.viewmodel.MedicalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCaseScreen(
    viewModel: MedicalViewModel,
    caseId: Int? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isOffline by viewModel.isOffline.collectAsState()
    val error by viewModel.error.collectAsState()

    val existingCase = remember(caseId) { caseId?.let { MedicalRepository.getCaseById(it) } }

    var titulo by remember { mutableStateOf(existingCase?.title ?: "") }
    var resumen by remember { mutableStateOf(existingCase?.summary ?: "") }
    var carpeta by remember { mutableStateOf(existingCase?.folder ?: "Sin carpeta") }
    var anamnesis by remember { mutableStateOf(existingCase?.anamnesis ?: "") }
    var exploracionFisica by remember { mutableStateOf(existingCase?.physicalExamination ?: "") }
    
    // Signos Vitales
    val vitals = existingCase?.vitalSigns
    var fc by remember { mutableStateOf(vitals?.frecuencia_cardiaca?.let { if (it > 0) it.toString() else "" } ?: "") }
    var ta by remember { mutableStateOf(vitals?.presion_arterial ?: "") }
    var fr by remember { mutableStateOf(vitals?.frecuencia_respiratoria?.let { if (it > 0) it.toString() else "" } ?: "") }
    var temp by remember { mutableStateOf(vitals?.temperatura?.let { if (it > 0f) it.toString() else "" } ?: "") }
    var sat by remember { mutableStateOf(vitals?.saturacion?.let { if (it > 0) it.toString() else "" } ?: "") }
    var bis by remember { mutableStateOf(vitals?.bis?.let { if (it > 0) it.toString() else "" } ?: "") }
    var etco2 by remember { mutableStateOf(vitals?.etco2?.let { if (it > 0) it.toString() else "" } ?: "") }

    // Lista de estudios (PDF)
    val studiesList = remember(existingCase) {
        mutableStateListOf<Study>().apply {
            if (existingCase != null) {
                addAll(existingCase.studies)
            }
        }
    }

    var showAddStudyDialog by remember { mutableStateOf(false) }
    var studyNameInput by remember { mutableStateOf("") }
    var studyUrlInput by remember { mutableStateOf("") }

    val isEditing = caseId != null

    // Diálogo para añadir nuevo estudio/documento PDF
    if (showAddStudyDialog) {
        AlertDialog(
            onDismissRequest = { showAddStudyDialog = false },
            title = { Text("Agregar Estudio o Documento PDF") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = studyNameInput,
                        onValueChange = { studyNameInput = it },
                        label = { Text("Nombre del Documento (ej. Gasometría Arterial PDF)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = studyUrlInput,
                        onValueChange = { studyUrlInput = it },
                        label = { Text("URL o Enlace del Archivo PDF") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (studyNameInput.isNotBlank()) {
                            studiesList.add(Study(nombre = studyNameInput.trim(), url = studyUrlInput.trim()))
                            studyNameInput = ""
                            studyUrlInput = ""
                            showAddStudyDialog = false
                        } else {
                            Toast.makeText(context, "Ingresa un nombre para el documento", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A8F5A))
                ) {
                    Text("Agregar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddStudyDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    val onSaveClick = {
        if (titulo.isBlank()) {
            Toast.makeText(context, "Por favor ingresa un título para el caso.", Toast.LENGTH_SHORT).show()
        } else {
            val clinicalCase = ClinicalCase(
                id = caseId,
                title = titulo,
                summary = resumen,
                folder = carpeta,
                anamnesis = anamnesis,
                physicalExamination = exploracionFisica,
                studies = studiesList.toList(),
                procedures = existingCase?.procedures ?: emptyList(),
                quiz = existingCase?.quiz ?: emptyList()
            )
            val updatedVitals = VitalSigns(
                frecuencia_cardiaca = fc.toIntOrNull() ?: 0,
                presion_arterial = ta,
                frecuencia_respiratoria = fr.toIntOrNull() ?: 0,
                temperatura = temp.toFloatOrNull() ?: 0f,
                saturacion = sat.toIntOrNull() ?: 0,
                bis = bis.toIntOrNull() ?: 0,
                etco2 = etco2.toIntOrNull() ?: 0
            )

            if (isEditing) {
                viewModel.updateCase(clinicalCase, updatedVitals, studiesList.toList()) { success ->
                    if (success) {
                        Toast.makeText(context, "Caso modificado exitosamente", Toast.LENGTH_SHORT).show()
                        onBack()
                    } else {
                        Toast.makeText(context, "Error al actualizar el caso", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                viewModel.addCase(clinicalCase, updatedVitals, studiesList.toList()) { success ->
                    if (success) {
                        if (isOffline) {
                            Toast.makeText(context, "Caso guardado localmente. Se subirá automáticamente a Supabase cuando vuelva el internet.", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Caso publicado exitosamente en Supabase", Toast.LENGTH_SHORT).show()
                        }
                        onBack()
                    } else {
                        Toast.makeText(context, "Error al guardar el caso", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Modificar Caso Clínico" else "Nuevo Caso Clínico", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    IconButton(onClick = { onSaveClick() }) {
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
            if (isOffline) {
                Surface(
                    color = Color(0xFFE2F0D9),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.WifiOff, contentDescription = null, tint = Color(0xFF385723))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Modo sin conexión: Los cambios se guardarán localmente y se actualizarán en Supabase al reconectar.",
                            color = Color(0xFF385723),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (error != null) {
                Surface(
                    color = Color(0xFFFFF3CD),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = error!!,
                        color = Color(0xFF856404),
                        modifier = Modifier.padding(12.dp),
                        fontSize = 13.sp
                    )
                }
            }

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

            // SECCIÓN DE ESTUDIOS Y DOCUMENTOS PDF CON BOTÓN +
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Estudios y Documentos (PDF)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A8F5A))
                
                IconButton(
                    onClick = { showAddStudyDialog = true },
                    modifier = Modifier.background(Color(0xFF1A8F5A).copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Documento PDF", tint = Color(0xFF1A8F5A))
                }
            }

            if (studiesList.isEmpty()) {
                Text("No se han agregado estudios o documentos PDF a este caso.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            } else {
                studiesList.forEachIndexed { index, study ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.Red)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(study.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    if (study.url.isNotBlank()) {
                                        Text(study.url, style = MaterialTheme.typography.labelSmall, color = Color.Gray, maxLines = 1)
                                    }
                                }
                            }
                            IconButton(onClick = { studiesList.removeAt(index) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar estudio", tint = Color.Red.copy(alpha = 0.7f))
                            }
                        }
                    }
                }
            }

            Button(
                onClick = { onSaveClick() },
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00723F)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isEditing) "Guardar Cambios" else "Guardar Caso Clínico", fontWeight = FontWeight.Bold)
            }
        }
    }
}
