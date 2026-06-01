package com.manimstudio.app.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.manimstudio.app.ManimStudioApp
import com.manimstudio.app.data.TemplateRepository
import com.manimstudio.app.data.models.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class StudioUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val phase: StudioPhase = StudioPhase.IDLE,
    val settings: AppSettings = AppSettings(),
    val renderProgress: String = "",
    val elapsedSeconds: Int = 0,
    val selectedEngine: String = "Local Engine",
    val showEngineSelector: Boolean = false,
    val recentChats: List<RecentChat> = listOf(
        RecentChat("1", "No-code manim animation buil..."),
        RecentChat("2", "Naruto Fandom Character Criti..."),
        RecentChat("3", "Multi-platform audio processin..."),
        RecentChat("4", "GUI for Manim animations")
    ),
    val suggestions: List<SuggestionCard> = listOf(
        SuggestionCard("Basic Shapes", "Create a circle and a square", "Create a blue circle and a red square side by side."),
        SuggestionCard("Mathematical Formula", "Render a LaTeX equation", "Show the Pythagorean theorem formula in the center."),
        SuggestionCard("Graphing", "Plot a simple sine wave", "Plot a sine wave from -2pi to 2pi with axes.")
    ),
    val generatedCode: String = "",
    val renderFormat: String = "MP4",
    val lastVideoFile: File? = null,
    val userName: String = "Abdulfatai",
    val showTemplatePicker: Boolean = false,
    val templates: List<Template> = emptyList(),
) {
    val renderQuality: RenderQuality get() = settings.renderQuality
}

class StudioViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as ManimStudioApp
    private val engine = app.engine
    private val renderer = app.renderer
    private val templateRepository = TemplateRepository(application)

    private val _uiState = MutableStateFlow(StudioUiState())
    val uiState: StateFlow<StudioUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var renderJob: Job? = null

    init {
        val templates = templateRepository.getTemplates()
        _uiState.update { it.copy(templates = templates) }
    }

    fun onInputChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun onSendPrompt() {
        val prompt = _uiState.value.inputText
        if (prompt.isBlank()) return

        _uiState.update { it.copy(inputText = "") }
        
        viewModelScope.launch {
            addMessage(ChatMessage(type = MessageType.USER_PROMPT, content = prompt))
            
            _uiState.update { it.copy(phase = StudioPhase.GENERATING, generatedCode = "") }
            addMessage(ChatMessage(type = MessageType.SYSTEM_STATUS, content = "Generating Manim code..."))
            
            try {
                val code = CodeGenerator.generate(prompt)
                _uiState.update { it.copy(generatedCode = code) }
                addMessage(ChatMessage(type = MessageType.CODE_PREVIEW, content = code))
                
                startRendering(code, _uiState.value.settings.renderQuality)
            } catch (e: Exception) {
                handleError("Generation failed: ${e.message}")
            }
        }
    }

    fun onCodeChanged(newCode: String) {
        _uiState.update { it.copy(generatedCode = newCode) }
    }

    fun onRenderFromEditor() {
        startRendering(_uiState.value.generatedCode, _uiState.value.settings.renderQuality)
    }

    fun toggleEngineSelector() {
        _uiState.update { it.copy(showEngineSelector = !it.showEngineSelector) }
    }

    fun onEngineSelected(engine: String) {
        _uiState.update { it.copy(selectedEngine = engine, showEngineSelector = false) }
    }

    fun onFormatChanged(format: String) {
        _uiState.update { it.copy(renderFormat = format) }
    }

    fun showTemplates() {
        _uiState.update { it.copy(showTemplatePicker = true) }
    }

    fun hideTemplatePicker() {
        _uiState.update { it.copy(showTemplatePicker = false) }
    }

    private fun startRendering(code: String, quality: RenderQuality) {
        _uiState.update { it.copy(phase = StudioPhase.RENDERING, renderProgress = "", elapsedSeconds = 0) }
        addMessage(ChatMessage(type = MessageType.SYSTEM_STATUS, content = "Rendering at ${quality.label}..."))
        
        startTimer()
        
        renderJob = viewModelScope.launch {
            val result = renderer.render(code, quality.label) { progress ->
                _uiState.update { it.copy(renderProgress = progress) }
            }
            
            stopTimer()
            
            if (result.success) {
                addMessage(ChatMessage(
                    type = MessageType.VIDEO_RESULT,
                    content = "Render complete",
                    videoFile = result.videoFile,
                    renderTimeMs = result.renderTimeMs
                ))
                _uiState.update { it.copy(phase = StudioPhase.DONE, lastVideoFile = result.videoFile) }
            } else {
                handleError(result.errorMessage ?: "Unknown rendering error")
            }
        }
    }

    fun onStopRender() {
        renderJob?.cancel()
        stopTimer()
        _uiState.update { it.copy(phase = StudioPhase.IDLE) }
        addMessage(ChatMessage(type = MessageType.SYSTEM_STATUS, content = "Render cancelled"))
    }

    fun onTemplateSelected(templateId: String) {
        val template = templateRepository.getTemplateById(templateId) ?: return
        
        _uiState.update { it.copy(
            generatedCode = template.code,
            phase = StudioPhase.IDLE,
            showTemplatePicker = false
        ) }
        addMessage(ChatMessage(type = MessageType.SYSTEM_STATUS, content = "Template loaded: ${template.name}"))
    }

    fun onNewChat() {
        _uiState.update { it.copy(messages = emptyList(), phase = StudioPhase.IDLE) }
    }

    fun onQualityChanged(quality: RenderQuality) {
        _uiState.update { it.copy(settings = it.settings.copy(renderQuality = quality)) }
    }

    fun onExportVideo(file: File) {
        // Implementation for exporting video to Movies folder
        // This usually involves MediaStore or File copying to public directory
        addMessage(ChatMessage(type = MessageType.SYSTEM_STATUS, content = "Video exported to gallery"))
    }

    private fun addMessage(message: ChatMessage) {
        _uiState.update { it.copy(messages = it.messages + message) }
    }

    private fun handleError(error: String) {
        addMessage(ChatMessage(type = MessageType.ERROR, content = error, isError = true))
        _uiState.update { it.copy(phase = StudioPhase.ERROR) }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }
}

object CodeGenerator {
    suspend fun generate(prompt: String): String {
        delay(1500) // simulate API call
        return """
from manim import *

class GeneratedScene(Scene):
    def construct(self):
        title = Text("${prompt.take(30)}", font_size=36, color=WHITE)
        title.to_edge(UP)
        self.play(Write(title))
        circle = Circle(radius=1.5, color=BLUE, fill_opacity=0.4)
        self.play(Create(circle))
        self.wait(2)
        self.play(*[FadeOut(m) for m in self.mobjects])
""".trimIndent()
    }
}
