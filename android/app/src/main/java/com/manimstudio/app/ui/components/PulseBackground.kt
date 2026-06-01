package com.manimstudio.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.manimstudio.app.ui.theme.GradientDeepBlue
import com.manimstudio.app.ui.theme.GradientPurple

@Composable
fun PulseBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val bgColor = MaterialTheme.colorScheme.background
    
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase"
    )

    Canvas(modifier = modifier.fillMaxSize().alpha(0.7f)) {
        val width = size.width
        val height = size.height
        
        val centerBlue = Offset(width * (0.2f + 0.6f * phase), height * (0.3f + 0.4f * phase))
        val centerPurple = Offset(width * (0.8f - 0.6f * phase), height * (0.7f - 0.4f * phase))

        drawRect(color = bgColor)
        
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(GradientDeepBlue, Color.Transparent),
                center = centerBlue,
                radius = width * 1.2f
            )
        )
        
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(GradientPurple, Color.Transparent),
                center = centerPurple,
                radius = width * 1.2f
            )
        )
    }
}
