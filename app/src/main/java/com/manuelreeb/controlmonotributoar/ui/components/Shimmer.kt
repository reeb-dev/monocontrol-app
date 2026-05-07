package com.reeb.controlmonotributoar.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

fun Modifier.shimmerLoading(
    baseColor: Color = Color(0xFFE8EEF7),
    highlightColor: Color = Color(0xFFF8FBFF)
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val xAnim = transition.animateFloat(
        initialValue = -300f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_x"
    )
    background(
        brush = Brush.linearGradient(
            colors = listOf(baseColor, highlightColor, baseColor),
            start = Offset(xAnim.value, 0f),
            end = Offset(xAnim.value + 280f, 280f)
        )
    )
}

@Composable
fun ShimmerBlock(modifier: Modifier) {
    Box(modifier = modifier.shimmerLoading().fillMaxSize())
}
