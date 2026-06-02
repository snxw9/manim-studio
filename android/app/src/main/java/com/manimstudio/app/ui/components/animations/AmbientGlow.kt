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
    val transition = rememberInfiniteTransition(label = "glow")
    val alpha by transition.animateFloat(
        initialValue = 0.10f, targetValue = 0.18f,
        animationSpec = infiniteRepeatable(
            tween(3500, easing = EaseInOutSine), RepeatMode.Reverse
        ), label = "glowAlpha",
    )
    val primaryColor = MaterialTheme.colorScheme.primary
    // Height 140dp max — stays below the input bar visually
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = alpha),
                    primaryColor.copy(alpha = alpha * 0.3f),
                    Color.Transparent,
                ),
                // Anchored to bottom center, radius doesn't exceed canvas
                center = Offset(size.width * 0.5f, size.height),
                radius = size.width * 0.65f,
            ),
            size = size,
        )
    }
}
