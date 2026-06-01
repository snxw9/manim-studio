package com.manimstudio.app.ui.components.animations

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun RenderingGradientBackground(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "renderGradient")

    // Animate three independent properties for organic movement
    val offset1 by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(5000, easing = EaseInOutSine), RepeatMode.Reverse
        ), label = "o1"
    )
    val offset2 by transition.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            tween(7000, easing = EaseInOutSine), RepeatMode.Reverse
        ), label = "o2"
    )
    val alpha by transition.animateFloat(
        initialValue = 0.55f, targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            tween(3000, easing = EaseInOutSine), RepeatMode.Reverse
        ), label = "alpha"
    )

    // CRITICAL: fillMaxSize so it covers the entire screen
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // First orb — moves vertically in the upper half
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF1A1A6E).copy(alpha = alpha),
                    Color.Transparent,
                ),
                center = Offset(w * 0.5f, h * (0.1f + offset1 * 0.35f)),
                radius = w * 1.1f,
            ),
            size = size,
        )

        // Second orb — moves horizontally across mid screen
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF6E1A6E).copy(alpha = alpha * 0.7f),
                    Color.Transparent,
                ),
                center = Offset(w * (0.2f + offset2 * 0.6f), h * 0.5f),
                radius = w * 0.9f,
            ),
            size = size,
        )

        // Third orb — subtle, always bottom-right
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF1A4A6E).copy(alpha = 0.3f),
                    Color.Transparent,
                ),
                center = Offset(w * 0.85f, h * 0.8f),
                radius = w * 0.7f,
            ),
            size = size,
        )
    }
}
