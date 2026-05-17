package com.manimstudio.app.engine

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

data class SetupState(
    val phase: Phase = Phase.CHECKING,
    val progress: Int = 0,
    val stage: String = "Checking...",
    val detail: String = "",
    val error: String? = null,
) {
    enum class Phase {
        CHECKING,      // App just launched, checking if installed
        NEEDS_SETUP,   // Not installed, show setup screen
        INSTALLING,    // Download + install in progress
        READY,         // All good, show main app
        ERROR,         // Something went wrong
    }
}

class SetupViewModel(app: Application) : AndroidViewModel(app) {

    private val engine = ProotEngine(app)
    private val renderer = ManimRenderer(app, engine)

    private val _state = MutableStateFlow(SetupState())
    val state: StateFlow<SetupState> = _state

    init {
        checkInstallation()
    }

    private fun checkInstallation() {
        viewModelScope.launch {
            val markerFile = File(engine.filesDir, ".installed")
            if (markerFile.exists() && engine.isInstalled) {
                _state.value = SetupState(phase = SetupState.Phase.READY,
                    progress = 100, stage = "Ready")
            } else {
                _state.value = SetupState(phase = SetupState.Phase.NEEDS_SETUP,
                    stage = "Setup required")
            }
        }
    }

    fun startInstallation() {
        viewModelScope.launch {
            _state.value = SetupState(phase = SetupState.Phase.INSTALLING,
                stage = "Starting setup...")

            val installer = BootstrapInstaller(
                context = getApplication(),
                engine = engine,
                onProgress = { progress ->
                    _state.value = SetupState(
                        phase = SetupState.Phase.INSTALLING,
                        progress = progress.percent,
                        stage = progress.stage,
                        detail = progress.detail,
                    )
                }
            )

            when (val result = installer.install()) {
                is InstallResult.Success -> {
                    _state.value = SetupState(
                        phase = SetupState.Phase.READY,
                        progress = 100,
                        stage = "Manim Studio is ready",
                    )
                }
                is InstallResult.Error -> {
                    _state.value = SetupState(
                        phase = SetupState.Phase.ERROR,
                        stage = "Setup failed",
                        error = result.message,
                    )
                }
            }
        }
    }

    fun testRender(onResult: (RenderResult) -> Unit) {
        viewModelScope.launch {
            val testCode = """
from manim import *

class TestScene(Scene):
    def construct(self):
        circle = Circle(radius=1.5, color=BLUE, fill_opacity=0.4)
        text = Text("Manim Studio", font_size=36)
        text.next_to(circle, DOWN, buff=0.4)
        self.play(Create(circle), Write(text))
        self.wait(1)
        self.play(FadeOut(circle), FadeOut(text))
""".trimIndent()

            val result = renderer.render(testCode, "480p") { line ->
                _state.value = _state.value.copy(detail = line.takeLast(60))
            }
            onResult(result)
        }
    }

    fun retrySetup() {
        // Clean up failed installation
        engine.usrDir.deleteRecursively()
        File(engine.filesDir, ".installed").delete()
        startInstallation()
    }
}
