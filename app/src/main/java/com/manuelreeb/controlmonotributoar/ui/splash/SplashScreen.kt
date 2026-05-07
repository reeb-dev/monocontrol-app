package com.reeb.controlmonotributoar.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reeb.controlmonotributoar.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Celeste  = Color(0xFF75AADB)
private val Blanco   = Color(0xFFFFFFFF)
private val Texto    = Color(0xFF0D2A4A)
private val Amarillo = Color(0xFFFBB81C)

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {

    // Entrada más fluida y moderna.
    val scale = remember { Animatable(0.78f) }
    val alpha = remember { Animatable(0f) }
    val rotation = remember { Animatable(-8f) }
    val offsetY = remember { Animatable(24f) }
    var introTerminada by remember { mutableStateOf(false) }

    // Respiración sutil luego de la entrada.
    val breathing by rememberInfiniteTransition(label = "logo_breathing")
        .animateFloat(
            initialValue = 1f,
            targetValue = 1.035f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "breathing_scale"
        )

    LaunchedEffect(Unit) {
        // Paralelizamos para una entrada con más vida.
        kotlinx.coroutines.coroutineScope {
            launch {
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = 0.72f,
                        stiffness = 280f
                    )
                )
            }
            launch { alpha.animateTo(1f, animationSpec = tween(650, easing = LinearOutSlowInEasing)) }
            launch { rotation.animateTo(0f, animationSpec = tween(700, easing = FastOutSlowInEasing)) }
            launch { offsetY.animateTo(0f, animationSpec = tween(600, easing = FastOutSlowInEasing)) }
        }

        introTerminada = true
        delay(1700)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Blanco),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 🔥 Logo grande, responsive
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)   // 👈 ocupa gran parte de la pantalla
                    .aspectRatio(1f)       // 👈 siempre cuadrado (círculo perfecto)
                    .offset(y = offsetY.value.dp)
                    .rotate(rotation.value)
                    .scale(scale.value * if (introTerminada) breathing else 1f)
                    .alpha(alpha.value)
                    .shadow(16.dp, CircleShape, clip = false)
                    .clip(CircleShape)
                    .background(Blanco),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo1),
                    contentDescription = "Monotributo Al Día",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Líneas decorativas ──
            Box(
                modifier = Modifier
                    .alpha(alpha.value)
                    .width(260.dp)
                    .height(5.dp)
                    .background(Celeste, RoundedCornerShape(3.dp))
            )

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .alpha(alpha.value)
                    .width(200.dp)
                    .height(3.dp)
                    .background(Amarillo, RoundedCornerShape(3.dp))
            )

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .alpha(alpha.value)
                    .width(260.dp)
                    .height(5.dp)
                    .background(Celeste, RoundedCornerShape(3.dp))
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Tu monotributo bajo control",
                modifier = Modifier.alpha(alpha.value),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Texto,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    letterSpacing = 0.4.sp
                )
            )
        }
    }
}