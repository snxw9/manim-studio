package com.manimstudio.app.ui.components.animations

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.manimstudio.app.ui.theme.GradientBlueGlow
import com.manimstudio.app.ui.theme.GradientPurpleGlow

@Composable
fun RenderingGradientBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "render_bg")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "offset",
    )
    Canvas(modifier = modifier.fillMaxSize()) {
        val colors = listOf(
            GradientBlueGlow.copy(alpha = 0.4f),
            GradientPurpleGlow.copy(alpha = 0.3f),
            Color.Transparent,
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = colors,
                center = Offset(size.width * 0.5f, size.height * (0.3f + offset * 0.2f)),
                radius = size.width * 0.8f,
            ),
        )
    }
}
