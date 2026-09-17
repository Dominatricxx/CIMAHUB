package com.example.cimahub.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.cimahub.ui.viewmodel.MedicalViewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AtlasScreen(viewModel: MedicalViewModel) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale *= zoomChange
        offset += offsetChange
        viewModel.setZoomLevel(scale)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Atlas Anatómico Interactivo") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White.copy(alpha = 0.9f),
                    titleContentColor = Color(0xFF1A8F5A)
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
                .transformable(state = state)
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        // Coordenadas normalizadas aproximadas para el Canvas de 400dp
                        val normalizedX = tapOffset.x / 400.dp.toPx()
                        val normalizedY = tapOffset.y / 400.dp.toPx()
                        
                        detectHotspot(normalizedX, normalizedY, scale, viewModel)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            AtlasCanvas(scale)
        }
        
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            ZoomLevelIndicator(scale, modifier = Modifier.padding(bottom = 80.dp))
        }
    }
}

private fun detectHotspot(x: Float, y: Float, scale: Float, viewModel: MedicalViewModel) {
    if (scale > 1.2f) { // Nivel 2 o 3
        when {
            // Cerebro (Cabeza superior)
            y < 0.2f && x in 0.4f..0.6f -> viewModel.onHotspotClicked("brain")
            // Oído (Lateral cabeza)
            y in 0.1f..0.2f && (x < 0.4f || x > 0.6f) -> viewModel.onHotspotClicked("ear")
            // Garganta / Tiroides
            y in 0.2f..0.25f && x in 0.45f..0.55f -> {
                if (scale > 2.5f) viewModel.onHotspotClicked("thyroid")
                else viewModel.onHotspotClicked("throat")
            }
            // Corazón
            y in 0.35f..0.45f && x in 0.4f..0.55f -> viewModel.onHotspotClicked("heart")
            // Pulmones
            y in 0.3f..0.5f && (x in 0.3f..0.4f || x in 0.6f..0.7f) -> viewModel.onHotspotClicked("lungs")
            // Útero / Pelvis
            y in 0.65f..0.8f && x in 0.4f..0.6f -> viewModel.onHotspotClicked("uterus")
            // Apéndice (FID)
            y in 0.7f..0.8f && x in 0.6f..0.7f -> viewModel.onHotspotClicked("appendix")
        }
    }
}

@Composable
fun AtlasCanvas(scale: Float) {
    Canvas(modifier = Modifier.size(400.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        
        // Nivel 1: Cuerpo General
        drawRect(
            color = Color.LightGray.copy(alpha = 0.3f),
            topLeft = Offset(size.width * 0.25f, size.height * 0.05f),
            size = Size(size.width * 0.5f, size.height * 0.9f),
            style = Stroke(width = 2f)
        )

        // Nivel 2: Órganos Regionales
        if (scale > 1.2f) {
            // Cabeza/Cerebro
            drawCircle(Color.Magenta.copy(alpha = 0.2f), radius = 30f, center = center.copy(y = center.y * 0.2f))
            // Pulmones
            drawOval(Color.Blue.copy(alpha = 0.3f), topLeft = Offset(size.width * 0.3f, size.height * 0.3f), size = Size(60f, 100f))
            drawOval(Color.Blue.copy(alpha = 0.3f), topLeft = Offset(size.width * 0.55f, size.height * 0.3f), size = Size(60f, 100f))
            // Corazón
            drawCircle(Color.Red.copy(alpha = 0.4f), radius = 25f, center = center.copy(x = center.x * 0.9f, y = center.y * 0.8f))
        }

        // Nivel 3: Patologías Específicas
        if (scale > 2.5f) {
            // Tiroides
            drawOval(Color(0xFFE91E63).copy(alpha = 0.6f), topLeft = Offset(size.width * 0.47f, size.height * 0.21f), size = Size(20f, 15f))
            // Apéndice inflamado
            drawCircle(Color.Red, radius = 10f, center = Offset(size.width * 0.65f, size.height * 0.75f))
            // Utero (HG)
            drawOval(Color(0xFF9C27B0).copy(alpha = 0.5f), topLeft = Offset(size.width * 0.45f, size.height * 0.7f), size = Size(40f, 50f))
        }
    }
}

@Composable
fun ZoomLevelIndicator(scale: Float, modifier: Modifier = Modifier) {
    val (level, color) = when {
        scale > 2.5f -> "Nivel 3: Quirúrgico / Patológico" to Color(0xFF1A8F5A)
        scale > 1.2f -> "Nivel 2: Regional (Abdominotorácico)" to Color(0xFF2F50FF)
        else -> "Nivel 1: Vista General" to Color.Gray
    }
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.1f),
        border = Stroke(width = 1f).let { null }, // placeholder
        shape = RoundedCornerShape(50.dp)
    ) {
        Text(
            text = level,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelLarge,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}
