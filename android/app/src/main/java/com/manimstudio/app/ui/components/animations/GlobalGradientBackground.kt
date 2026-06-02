package com.manimstudio.app.ui.components.animations

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * A reusable full-screen animated gradient backdrop.
 * Uses the current MaterialTheme primary color so it adapts
 * to both Material You dynamic colors and fixed themes.
 * intensity: 0.0 = invisible, 1.0 = full opacity
 */
@Composable
fun GlobalGradientBackground(
    modifier: Modifier = Modifier,
    intensity: Float = 0.6f,
    animate: Boolean = true,
) {
    val primary = MaterialTheme.colorScheme.primary

    val transition = rememberInfiniteTransition(label = "globalGradient")

    val orb1Y by if (animate) transition.animateFloat(
        initialValue = 0.05f, targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            tween(6000, easing = EaseInOutSine), RepeatMode.Reverse
        ), label = "orb1Y"
    ) else remember { mutableStateOf(0.05f) }

    val orb2X by if (animate) transition.animateFloat(
        initialValue = 0.1f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            tween(9000, easing = EaseInOutSine), RepeatMode.Reverse
        ), label = "orb2X"
    ) else remember { mutableStateOf(0.5f) }

    val orb3Alpha by if (animate) transition.animateFloat(
        initialValue = 0.3f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            tween(4000, easing = EaseInOutSine), RepeatMode.Reverse
        ), label = "orb3A"
    ) else remember { mutableStateOf(0.5f) }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Bottom glow — anchored low, primary color
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    primary.copy(alpha = 0.18f * intensity),
                    primary.copy(alpha = 0.04f * intensity),
                    Color.Transparent,
                ),
                center = Offset(w * 0.5f, h * 0.92f),
                radius = w * 0.7f,
            ), size = size,
        )

        // Slow-moving upper orb
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF1A1A6E).copy(alpha = 0.12f * intensity),
                    Color.Transparent,
                ),
                center = Offset(w * 0.3f, h * orb1Y),
                radius = w * 0.8f,
            ), size = size,
        )

        // Wide horizontal sweep
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF6E1A5E).copy(alpha = 0.08f * intensity * orb3Alpha),
                    Color.Transparent,
                ),
                center = Offset(w * orb2X, h * 0.5f),
                radius = w * 0.65f,
            ), size = size,
        )
    }
}
