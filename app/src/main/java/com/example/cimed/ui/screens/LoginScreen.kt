package com.example.cimed.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cimed.R
import com.example.cimed.ui.theme.*
import com.example.cimed.ui.viewmodel.UserRole

@Composable
fun LoginScreen(onLoginSuccess: (UserRole) -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            Text(
                text = "CIMED",
                color = Color.White,
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )
            Text(
                text = "Innovación y Desarrollo Tecnológico UABC",
                color = UabcGoldLight,
                fontSize = 16.sp,
                fontWeight = FontWeight.Light
            )

            Spacer(modifier = Modifier.weight(1f))

            Image(
                painter = painterResource(id = R.drawable.logo_cimahub),
                contentDescription = "Logo CIMED",
                modifier = Modifier
                    .size(160.dp)
                    .padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Card(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .padding(bottom = 60.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f))
            ) {
                Column(
                    modifier = Modifier
                        .padding(32.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PulseIndicator()
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "CI",
                            color = UabcGreen,
                            fontSize = 72.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "MED",
                            color = UabcGold,
                            fontSize = 54.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.offset(y = 10.dp)
                        )
                    }

                    Text(
                        text = "Simulador Médico",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = UabcGreen
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Text(
                        text = "¿Cómo deseas ingresar?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { onLoginSuccess(UserRole.Student) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = UabcGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "SOY ESTUDIANTE",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { onLoginSuccess(UserRole.Teacher) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(2.dp, UabcGreen),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = UabcGreen)
                    ) {
                        Text(
                            text = "SOY DOCENTE",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun PulseIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 4.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .background(UabcGreen, RoundedCornerShape(50.dp))
        )
    }
}
