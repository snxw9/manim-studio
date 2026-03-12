package com.manimstudio.engine

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manimstudio.ai.GroqClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AnimationViewModel : ViewModel() {
    private val _prompt = MutableStateFlow("")
    val prompt: StateFlow<String> = _prompt.asStateFlow()

    private val _generatedCode = MutableStateFlow("")
    val generatedCode: StateFlow<String> = _generatedCode.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun updatePrompt(newPrompt: String) {
        _prompt.value = newPrompt
    }

    fun generateAnimation(context: Context) {
        val currentPrompt = _prompt.value
        if (currentPrompt.isBlank()) return

        viewModelScope.launch {
            _isGenerating.value = true
            _error.value = null
            try {
                val code = GroqClient.generate(context, currentPrompt)
                _generatedCode.value = code
            } catch (e: Exception) {
                _error.value = e.message ?: "Generation failed"
            } finally {
                _isGenerating.value = false
            }
        }
    }
}
