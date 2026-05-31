package com.manimstudio.app.data.models

data class AppSettings(
    val renderQuality: RenderQuality = RenderQuality.MID,
    val apiProvider: String = "auto",
    val groqApiKey: String = "",
    val geminiApiKey: String = "",
)
