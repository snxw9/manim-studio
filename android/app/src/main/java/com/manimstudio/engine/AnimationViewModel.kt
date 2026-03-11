package com.manimstudio.engine

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AnimationViewModel : ViewModel() {
    private val _prompt = MutableStateFlow("")
    val prompt: StateFlow<String> = _prompt.asStateFlow()

    private val _generatedCode = MutableStateFlow("")
    val generatedCode: StateFlow<String> = _generatedCode.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    fun updatePrompt(newPrompt: String) {
        _prompt.value = newPrompt
    }

    fun generateAnimation() {
        _isGenerating.value = true
        // Placeholder for AI generation via Chaquopy or Retrofit
        _generatedCode.value = "from manim import *\nclass MyScene(Scene):\n    def construct(self):\n        pass"
        _isGenerating.value = false
    }
}
