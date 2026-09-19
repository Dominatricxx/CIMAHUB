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

        // Partícula 1: Verde UABC - Movimiento suave y sutil (Usamos coeficientes enteros 1x y 1x para un bucle perfecto)
        val p1X = w / 2 + (w * 0.4f) * cos(time)
        val p1Y = h / 2 + (h * 0.3f) * sin(time)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(UabcGreen.copy(alpha = 0.4f), Color.Transparent),
                center = Offset(p1X, p1Y),
                radius = w * 1.1f
            ),
            center = Offset(p1X, p1Y),
            radius = w * 1.1f
        )

        // Partícula 2: Oro UABC - Movimiento pausado (Intercambiamos sin/cos y usamos entero 1x para el ciclo cerrado)
        val p2X = w / 2 + (w * 0.35f) * sin(time)
        val p2Y = h / 2 + (h * 0.4f) * cos(time)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(UabcGold.copy(alpha = 0.35f), Color.Transparent),
                center = Offset(p2X, p2Y),
                radius = w * 1.0f
            ),
            center = Offset(p2X, p2Y),
            radius = w * 1.0f
        )

        // Partícula 3: Azul UABC - Movimiento en órbita invertida (Usamos entero 1x)
        val p3X = w / 2 + (w * 0.5f) * cos(-time)
        val p3Y = h / 2 + (h * 0.5f) * sin(-time)
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
