package com.example.cimahub.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.cimahub.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun UabcHyperDynamicBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "uabc_hyper")
    
    // Animación mucho más lenta y suave (25 segundos por ciclo)
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Partícula 1: Verde UABC - Movimiento suave y sutil
        val p1X = w / 2 + (w * 0.4f) * cos(time * 0.8f)
        val p1Y = h / 2 + (h * 0.3f) * sin(time * 0.5f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(UabcGreen.copy(alpha = 0.4f), Color.Transparent),
                center = Offset(p1X, p1Y),
                radius = w * 1.1f
            ),
            center = Offset(p1X, p1Y),
            radius = w * 1.1f
        )

        // Partícula 2: Oro UABC - Movimiento pausado
        val p2X = w / 2 + (w * 0.35f) * sin(time * 0.6f)
        val p2Y = h / 2 + (h * 0.4f) * cos(time * 0.7f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(UabcGold.copy(alpha = 0.35f), Color.Transparent),
                center = Offset(p2X, p2Y),
                radius = w * 1.0f
            ),
            center = Offset(p2X, p2Y),
            radius = w * 1.0f
        )

        // Partícula 3: Azul UABC - Movimiento perimetral lento
        val p3X = w / 2 + (w * 0.5f) * cos(time * 0.3f)
        val p3Y = h / 2 + (h * 0.5f) * sin(time * 0.3f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(UabcBlue.copy(alpha = 0.3f), Color.Transparent),
                center = Offset(p3X, p3Y),
                radius = w * 1.2f
            ),
            center = Offset(p3X, p3Y),
            radius = w * 1.2f
        )
    }
}
