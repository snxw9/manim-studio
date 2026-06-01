package com.manimstudio.app.data.models

enum class StudioPhase {
    IDLE,           // waiting for input
    GENERATING,     // calling AI to make Manim code
    RENDERING,      // proot running manim
    DONE,           // video ready
    ERROR,          // something failed
}
