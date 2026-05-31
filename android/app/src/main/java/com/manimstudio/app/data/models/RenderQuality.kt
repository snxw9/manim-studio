package com.manimstudio.app.data.models

enum class RenderQuality(val label: String, val flag: String, val estimatedSeconds: String) {
    LOW("480p", "-ql", "15–60s"),
    MID("720p", "-qm", "30–120s"),
    HIGH("1080p", "-qh", "60–240s"),
}
