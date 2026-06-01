package com.manimstudio.app.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring

object Motion {
    // For page transitions and large reveals
    val EnterSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )
    val ExitSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium,
    )
    // For small interactive elements (chips, buttons, icons)
    val BounceSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium,
    )
    // For drawer and large panels
    val DrawerSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow,
    )
}
