package com.example.cimahub.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cimahub.data.models.*
import com.example.cimahub.data.repository.MedicalRepository
import com.example.cimahub.ui.viewmodel.MedicalViewModel
import com.example.cimahub.ui.viewmodel.UserRole
import kotlin.math.exp
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulationScreen(caseId: Int?, viewModel: MedicalViewModel, onBack: () -> Unit) {
    val clinicalCase = remember(caseId) { caseId?.let { MedicalRepository.getCaseById(it) } }
    var selectedTab by remember { mutableIntStateOf(0) }
    val userRole by viewModel.userRole.collectAsState()

    if (clinicalCase == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Caso no encontrado")
        }
        return
    }

    val tabs = listOf(
        "Caso" to Icons.Default.FilePresent,
        "Estudios" to Icons.Default.Folder,
        "Maniobras" to Icons.Default.PlayCircle,
        "Electro" to Icons.Default.MonitorHeart,
        "Quiz" to Icons.Default.QuestionAnswer
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Simulación Médica", style = MaterialTheme.typography.titleMedium)
                        Text(clinicalCase.title, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Regresar")
                    }
                },
                actions = {
                    if (userRole == UserRole.Teacher) {
                        IconButton(onClick = { /* Acción para editar */ }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar Caso")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White.copy(alpha = 0.95f),
                    titleContentColor = Color(0xFF1A8F5A),
                    navigationIconContentColor = Color(0xFF1A8F5A),
                    actionIconContentColor = Color(0xFF1A8F5A)
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Bar Estilo HTML
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White.copy(alpha = 0.8f),
                contentColor = Color(0xFF1A8F5A),
                edgePadding = 16.dp,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFF1A8F5A)
                    )
                }
            ) {
                tabs.forEachIndexed { index, pair ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(pair.second, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(pair.first, fontSize = 13.sp, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    )
                }
            }

            Crossfade(targetState = selectedTab, label = "tab_fade") { tabIndex ->
                when (tabIndex) {
                    0 -> CaseInfoPanel(clinicalCase)
                    1 -> StudiesPanel(clinicalCase.studies)
                    2 -> ProceduresPanel(clinicalCase.procedures)
                    3 -> ElectroPanel(clinicalCase)
                    4 -> QuizPanel(clinicalCase.quiz)
                }
            }
        }
    }
}

@Composable
fun CaseInfoPanel(clinicalCase: ClinicalCase) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFF1A8F5A), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Nombre del Caso: ", fontWeight = FontWeight.Bold)
                    Text(clinicalCase.title, color = Color(0xFF1A8F5A), fontWeight = FontWeight.ExtraBold)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                Text(clinicalCase.summary, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.LightGray.copy(alpha = 0.5f))
                
                SectionHeader("Datos del Paciente", Icons.Default.Person)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        InfoLabelValue("Nombre", clinicalCase.patientName)
                        InfoLabelValue("Sexo", clinicalCase.patientSex)
                        InfoLabelValue("Motivo", clinicalCase.patientMotive)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        InfoLabelValue("Edad", "${clinicalCase.patientAge} años")
                        InfoLabelValue("Peso", "${clinicalCase.patientWeight} kg")
                        InfoLabelValue("Altura", "${clinicalCase.patientHeight} cm")
                    }
                }
                
                InfoLabelValue("Antecedentes", clinicalCase.patientHistory)

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.LightGray.copy(alpha = 0.5f))

                SectionHeader("Signos Vitales (Resumen)", Icons.Default.DeviceThermostat)
                
                clinicalCase.vitalSigns?.let { vitals ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            InfoLabelValue("FC", "${vitals.frecuencia_cardiaca} lpm")
                            InfoLabelValue("FR", "${vitals.frecuencia_respiratoria} rpm")
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            InfoLabelValue("TA", "${vitals.presion_arterial} mmHg")
                            InfoLabelValue("SAT", "${vitals.saturacion} %")
                            InfoLabelValue("Temp", "${vitals.temperatura} °C")
                        }
                    }
                } ?: Text("Signos vitales no disponibles", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
        Icon(icon, contentDescription = null, tint = Color(0xFF2F50FF), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, color = Color(0xFF2F50FF), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun InfoLabelValue(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
    }
}

@Composable
fun StudiesPanel(studies: List<Study>) {
    if (studies.isEmpty()) {
        EmptyState("No hay estudios disponibles", Icons.Default.FolderOff)
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(studies) { study ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(study.nombre, textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ProceduresPanel(procedures: List<Procedure>) {
    if (procedures.isEmpty()) {
        EmptyState("No hay maniobras disponibles", Icons.Default.VideoLabel)
        return
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(procedures) { procedure ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PlayCircleFilled, contentDescription = null, tint = Color.Red, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(procedure.nombre, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ElectroPanel(clinicalCase: ClinicalCase) {
    val vitals = clinicalCase.vitalSigns
    if (vitals == null) {
        EmptyState("Datos de monitor no disponibles", Icons.Default.MonitorHeart)
        return
    }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0A0A)),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(2.dp)) {
                // Barra de Alertas (Superior)
                Box(
                    modifier = Modifier.fillMaxWidth().height(24.dp).background(Color(0xFF1A1A1A)),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text("  ALARM LIMITS ACTIVE", color = Color.Yellow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Row(modifier = Modifier.fillMaxWidth().height(440.dp)) {
                    // Columna Izquierda: Canales de Onda
                    Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        MonitorChannel("I", "ECG", Color(0xFF00FF9F), WaveType.EKG, vitals.frecuencia_cardiaca)
                        MonitorChannel("SpO₂", "PLETH", Color(0xFF4DB8FF), WaveType.SPO2, vitals.frecuencia_cardiaca)
                        MonitorChannel("IBP", "ART", Color(0xFFFF6060), WaveType.IBP, vitals.frecuencia_cardiaca)
                        MonitorChannel("EEG", "BIS", Color(0xFF00E5CC), WaveType.EEG, 60)
                        MonitorChannel("CO₂", "CAPNO", Color(0xFFFFEE00), WaveType.CO2, vitals.frecuencia_respiratoria)
                    }
                    
                    // Columna Derecha: Valores Numéricos
                    Column(
                        modifier = Modifier.width(90.dp).fillMaxHeight().background(Color(0xFF050505)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        VitalBlock("HR", "${vitals.frecuencia_cardiaca}", Color(0xFF00FF9F), "bpm")
                        VitalBlock("SpO2", "${vitals.saturacion}", Color(0xFF4DB8FF), "%")
                        VitalBlock("IBP", vitals.presion_arterial, Color(0xFFFF6060), "mmHg")
                        VitalBlock("BIS", "${vitals.bis}", Color(0xFF00E5CC), "")
                        VitalBlock("CO₂", "${vitals.etco2}", Color(0xFFFFEE00), "RR ${vitals.frecuencia_respiratoria}")
                    }
                }

                // Barra de Estado (Inferior)
                Row(
                    modifier = Modifier.fillMaxWidth().height(28.dp).background(Color(0xFF1A1A1A)).padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF00FF9F)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CIMED MONITOR", color = Color(0xFF00FF9F), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("${vitals.frecuencia_cardiaca} BPM", color = Color.White, fontSize = 10.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    Text("SISTEMA ACTIVO", color = Color.Gray, fontSize = 9.sp)
                }
            }
        }
    }
}

@Composable
fun ColumnScope.MonitorChannel(name: String, sub: String, color: Color, type: WaveType, bpm: Int) {
    Box(modifier = Modifier.fillMaxWidth().weight(1f).padding(2.dp)) {
        MonitorGrid()
        RealisticWaveform(Modifier.fillMaxSize(), color, type, bpm)
        Column(modifier = Modifier.padding(4.dp)) {
            Text(name, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(sub, color = color.copy(alpha = 0.7f), fontSize = 8.sp)
        }
    }
}

@Composable
fun VitalBlock(label: String, value: String, color: Color, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(value, color = color, fontSize = 18.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
        if (unit.isNotEmpty()) {
            Text(unit, color = color, fontSize = 8.sp)
        }
    }
}

@Composable
fun QuizPanel(questions: List<QuizQuestion>) {
    if (questions.isEmpty()) {
        EmptyState("No hay preguntas disponibles", Icons.Default.QuestionMark)
        return
    }

    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableIntStateOf(-1) }
    var showExplanation by remember { mutableStateOf(false) }

    val question = questions[currentQuestionIndex]

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Pregunta ${currentQuestionIndex + 1} de ${questions.size}", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(question.pregunta, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                question.opciones.forEachIndexed { index, option ->
                    val color = when {
                        showExplanation && index == question.correctAnswerIndex -> Color(0xFF1A8F5A)
                        showExplanation && index == selectedOption && index != question.correctAnswerIndex -> Color.Red
                        selectedOption == index -> Color(0xFF2F50FF)
                        else -> Color.DarkGray
                    }
                    
                    OutlinedButton(
                        onClick = { if (!showExplanation) selectedOption = index },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = color),
                        border = BorderStroke(
                            width = if (selectedOption == index) 2.dp else 1.dp,
                            color = color.copy(alpha = 0.5f)
                        )
                    ) {
                        Text(option, textAlign = TextAlign.Start, modifier = Modifier.fillMaxWidth())
                    }
                }
                
                if (showExplanation) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = question.explicacion,
                        color = if (selectedOption == question.correctAnswerIndex) Color(0xFF1A8F5A) else Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                if (!showExplanation) {
                    if (selectedOption != -1) showExplanation = true
                } else {
                    if (currentQuestionIndex < questions.size - 1) {
                        currentQuestionIndex++
                        selectedOption = -1
                        showExplanation = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(0.7f),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A8F5A)),
            enabled = selectedOption != -1
        ) {
            Text(if (!showExplanation) "Responder" else "Siguiente")
        }
    }
}

@Composable
fun EmptyState(text: String, icon: ImageVector) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(text, color = Color.Gray)
    }
}

@Composable
fun MonitorGrid() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val step = 20.dp.toPx()
        for (x in 0..size.width.toInt() step step.toInt()) {
            drawLine(Color.DarkGray.copy(alpha = 0.2f), start = Offset(x.toFloat(), 0f), end = Offset(x.toFloat(), size.height))
        }
        for (y in 0..size.height.toInt() step step.toInt()) {
            drawLine(Color.DarkGray.copy(alpha = 0.2f), start = Offset(0f, y.toFloat()), end = Offset(size.width, y.toFloat()))
        }
    }
}

enum class WaveType { EKG, SPO2, IBP, CO2, EEG }

@Composable
fun RealisticWaveform(modifier: Modifier, color: Color, type: WaveType, bpm: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height / 2
        val path = Path()
        
        path.moveTo(0f, centerY)
        
        // Frecuencia de pulsos basada en BPM (o frecuencia respiratoria)
        val rate = if (type == WaveType.CO2) bpm.coerceAtLeast(1) else bpm.coerceAtLeast(1)
        val pulsesPerSecond = rate / 60f
        val displaySeconds = if (type == WaveType.CO2) 10f else 3f
        val pulseWidth = width / (pulsesPerSecond * displaySeconds)
        
        for (x in 0..width.toInt() step 2) {
            val nx = x / width
            val sweepX = (nx + phase) % 1.0f
            
            // Simular pulso recurrente
            val t = (sweepX * width) % pulseWidth
            val nt = t / pulseWidth
            
            val yOffset = when (type) {
                WaveType.EKG -> calculateEkgOffset(nt, height)
                WaveType.SPO2 -> calculateSpo2Offset(nt, height)
                WaveType.IBP -> calculateIbpOffset(nt, height)
                WaveType.CO2 -> calculateCo2Offset(nt, height)
                WaveType.EEG -> calculateEegOffset(nt, height)
            }
            
            if (x == 0) path.moveTo(x.toFloat(), centerY + yOffset)
            else path.lineTo(x.toFloat(), centerY + yOffset)
        }
        
        drawPath(path, color, style = Stroke(width = 1.5.dp.toPx()))
    }
}

fun calculateEkgOffset(t: Float, maxHeight: Float): Float {
    return when {
        t in 0.1f..0.2f -> -5f * sin((t - 0.1f) / 0.1f * Math.PI.toFloat())
        t in 0.25f..0.27f -> 10f * ((t - 0.25f) / 0.02f)
        t in 0.27f..0.32f -> -maxHeight * 0.4f * sin((t - 0.27f) / 0.05f * Math.PI.toFloat()) + 10f
        t in 0.32f..0.34f -> 15f * ((t - 0.32f) / 0.02f) - 5f
        t in 0.45f..0.6f -> -12f * sin((t - 0.45f) / 0.15f * Math.PI.toFloat())
        else -> 0f
    }
}

fun calculateSpo2Offset(t: Float, maxHeight: Float): Float {
    return if (t in 0.2f..0.8f) {
        val valT = (t - 0.2f) / 0.6f
        -maxHeight * 0.3f * (valT * exp(1f - valT)) * sin(valT * Math.PI.toFloat()).toFloat()
    } else 0f
}

fun calculateIbpOffset(t: Float, maxHeight: Float): Float {
    // Onda de presión arterial invasiva
    return when {
        t in 0.1f..0.3f -> { // Sístole
            val valT = (t - 0.1f) / 0.2f
            -maxHeight * 0.5f * sin(valT * Math.PI.toFloat())
        }
        t in 0.3f..0.35f -> { // Muesca dicrota
            -maxHeight * 0.2f
        }
        t in 0.35f..0.8f -> { // Diástole
            val valT = (t - 0.35f) / 0.45f
            -maxHeight * 0.2f * exp(-valT * 2f)
        }
        else -> 0f
    }
}

fun calculateCo2Offset(t: Float, maxHeight: Float): Float {
    // Onda de capnografía (forma de caja)
    return when {
        t in 0.2f..0.25f -> -maxHeight * 0.4f * ((t - 0.2f) / 0.05f) // Inspiración a Espiración
        t in 0.25f..0.7f -> -maxHeight * 0.4f // Meseta alveolar
        t in 0.7f..0.75f -> -maxHeight * 0.4f * (1f - (t - 0.7f) / 0.05f) // Lavado de CO2
        else -> 0f
    }
}

fun calculateEegOffset(t: Float, maxHeight: Float): Float {
    // Onda de EEG (ruido aleatorio suave)
    return -maxHeight * 0.1f * sin(t * 50f) * sin(t * 23f)
}
