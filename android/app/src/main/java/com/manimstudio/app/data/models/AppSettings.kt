package com.manimstudio.app.data.models

data class AppSettings(
    val themeSettings: ThemeSettings = ThemeSettings(),
    val renderQuality: RenderQuality = RenderQuality.MID,
    val apiProvider: String = "auto",
    val groqApiKey: String = "",
    val geminiApiKey: String = "",
    val userName: String = "",
)
