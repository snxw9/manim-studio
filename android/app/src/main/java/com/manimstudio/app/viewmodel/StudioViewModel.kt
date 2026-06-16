package com.manimstudio.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.manimstudio.app.data.TemplateRepository
import com.manimstudio.app.data.models.*
import com.manimstudio.app.engine.ProotEngine
import com.manimstudio.app.engine.ManimRenderer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import kotlin.time.Duration.Companion.milliseconds

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
    val inputExpanded: Boolean = true,
)

class StudioViewModel(
    application: Application,
    private val prootEngine: ProotEngine,
) : AndroidViewModel(application) {

    private val renderer = ManimRenderer(prootEngine)
    private val templateRepository = TemplateRepository(application)

    private val _uiState = MutableStateFlow(StudioUiState())
    val uiState: StateFlow<StudioUiState> = _uiState.asStateFlow()

    // Expose templates as state flow for AppNavigation/TemplatesScreen
    val templates: StateFlow<List<Template>> = MutableStateFlow(emptyList<Template>()).apply {
        value = templateRepository.getTemplates()
    }.asStateFlow()

    private var timerJob: Job? = null
    private var renderJob: Job? = null

    init {
        val templatesList = templateRepository.getTemplates()
        _uiState.update { it.copy(templates = templatesList) }
    }

    private fun checkBootstrapOrError(): Boolean {
        val marker = File(prootEngine.filesDir, ".installed")
        if (!marker.exists() || !prootEngine.pythonBin.exists()) {
            val errorMsg = ChatMessage(
                type = MessageType.ERROR,
                content = "The Manim engine is not installed. " +
                          "Go to Settings → Engine to install it.",
                isError = true,
            )
            _uiState.update { it.copy(
                messages = it.messages + errorMsg,
                phase = StudioPhase.IDLE,
            ) }
            return false
        }
        return true
    }

    fun onInputChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun onSendPrompt() {
        if (!checkBootstrapOrError()) return
        val prompt = _uiState.value.inputText.trim()
        if (prompt.isEmpty()) return

        _uiState.update { it.copy(inputText = "") }
        
        viewModelScope.launch {
            addMessage(ChatMessage(type = MessageType.USER_PROMPT, content = prompt))
            
            _uiState.update { it.copy(phase = StudioPhase.GENERATING, generatedCode = "") }
            addMessage(ChatMessage(type = MessageType.SYSTEM_STATUS, content = "Generating Manim code..."))
            
            try {
                val code = CodeGenerator.generate(getApplication(), prompt)
                _uiState.update { it.copy(generatedCode = code) }
                addMessage(ChatMessage(type = MessageType.CODE_PREVIEW, content = code))
                
                startRendering(code, _uiState.value.settings.renderQuality)
            } catch (e: Exception) {
                handleError("Generation failed: ${e.message}")
            }
        }
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
                delay(1000.milliseconds)
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
    suspend fun generate(context: android.content.Context, prompt: String): String {
        return try {
            com.manimstudio.ai.GroqClient.generate(context, prompt)
        } catch (_: Exception) {
            delay(1500.milliseconds) // simulate API call delay on fallback
            """
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
}
