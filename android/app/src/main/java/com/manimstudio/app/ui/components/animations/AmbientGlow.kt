package com.manimstudio.app.ui.components.animations

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme

@Composable
fun AmbientGlow(modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val transition = rememberInfiniteTransition(label = "ambientGlow")
    val alpha by transition.animateFloat(
        initialValue = 0.12f, targetValue = 0.22f,
        animationSpec = infiniteRepeatable(
            tween(3500, easing = EaseInOutSine), RepeatMode.Reverse
        ), label = "glowAlpha"
    )
    val spread by transition.animateFloat(
        initialValue = 0.7f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            tween(4000, easing = EaseInOutSine), RepeatMode.Reverse
        ), label = "glowSpread"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp) // tall enough to feel immersive
    ) {
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    primary.copy(alpha = alpha),
                    primary.copy(alpha = alpha * 0.3f),
                    Color.Transparent,
                ),
                center = Offset(size.width * 0.5f, size.height),
                radius = size.width * spread,
            ),
            size = size,
        )
    }
}
