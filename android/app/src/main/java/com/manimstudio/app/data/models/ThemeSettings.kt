package com.manimstudio.app.data.models

import androidx.compose.ui.graphics.Color

data class ThemeSettings(
    val useMaterialYou: Boolean = true,
    val themeColor: ThemeColor = ThemeColor.ORANGE,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val pureBlackBackground: Boolean = true,
    val fontOption: FontOption = FontOption.INTER,
)

enum class FontOption { INTER, SYSTEM }

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class ThemeColor(val color: Color, val label: String) {
    ORANGE(Color(0xFFE8621A), "Manim Orange"),
    BLUE(Color(0xFF1A73E8), "Ocean Blue"),
    GREEN(Color(0xFF34A853), "Forest Green"),
    PURPLE(Color(0xFF9C27B0), "Deep Purple"),
    RED(Color(0xFFEA4335), "Crimson"),
    TEAL(Color(0xFF009688), "Teal"),
}
