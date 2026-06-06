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
    val bytesDownloaded: Long = 0L,
    val bytesTotal: Long = 0L,
    val error: String? = null,
    val updateAvailable: BootstrapManifest? = null,
    val showWifiDialog: Boolean = false,
) {
    enum class Phase {
        CHECKING,       // first launch check
        NEEDS_SETUP,    // not installed, show setup screen
        WIFI_REQUIRED,  // on mobile data, need confirmation
        INSTALLING,     // download + extract in progress
        READY,          // all good
        ERROR,          // installation failed
    }
}

class SetupViewModel(app: Application) : AndroidViewModel(app) {

    val engine = ProotEngine(app)
    private val renderer = ManimRenderer(app, engine)

    private val _state = MutableStateFlow(SetupState())
    val state: StateFlow<SetupState> = _state

    init {
        checkInstallation()
    }

    // ── Setup flow ────────────────────────────────────────────────────────────

    private fun checkInstallation() {
        viewModelScope.launch {
            val installer = makeInstaller()
            if (installer.isInstalled()) {
                _state.value = SetupState(
                    phase = SetupState.Phase.READY,
                    progress = 100,
                    stage = "Ready",
                )
                // Background update check — non-blocking
                checkForUpdate()
            } else {
                _state.value = SetupState(phase = SetupState.Phase.NEEDS_SETUP)
            }
        }
    }

    fun startInstallation(allowMobileData: Boolean = false) {
        viewModelScope.launch {
            _state.value = SetupState(
                phase = SetupState.Phase.INSTALLING,
                stage = "Starting...",
            )

            val installer = makeInstaller()
            when (val result = installer.install(allowMobileData)) {
                is InstallResult.Success -> {
                    _state.value = SetupState(
                        phase = SetupState.Phase.READY,
                        progress = 100,
                        stage = "Manim Studio is ready",
                    )
                }
                is InstallResult.WifiRequired -> {
                    _state.value = SetupState(
                        phase = SetupState.Phase.WIFI_REQUIRED,
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

    fun confirmMobileDataInstall() {
        startInstallation(allowMobileData = true)
    }

    fun retrySetup() {
        engine.usrDir.deleteRecursively()
        File(engine.filesDir, ".installed").delete()
        _state.value = SetupState(phase = SetupState.Phase.NEEDS_SETUP)
    }

    // ── Update flow ───────────────────────────────────────────────────────────

    private fun checkForUpdate() {
        viewModelScope.launch {
            val installer = makeInstaller()
            val update = installer.checkForUpdate()
            if (update != null) {
                _state.value = _state.value.copy(updateAvailable = update)
            }
        }
    }

    fun getInstalledVersion(): String? = makeInstaller().getInstalledVersion()

    // ── Test render ───────────────────────────────────────────────────────────

    fun testRender(onResult: (RenderResult) -> Unit) {
        viewModelScope.launch {
            val testCode = """
from manim import *

class WelcomeTest(Scene):
    def construct(self):
        text = Text("Manim Studio", font_size=48, color=WHITE)
        self.play(Write(text))
        self.wait(1)
        self.play(FadeOut(text))
""".trimIndent()
            val result = renderer.render(testCode, "480p") { line ->
                _state.value = _state.value.copy(detail = line.takeLast(60))
            }
            onResult(result)
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun makeInstaller() = BootstrapInstaller(
        context = getApplication(),
        engine = engine,
        onProgress = { progress ->
            _state.value = SetupState(
                phase = SetupState.Phase.INSTALLING,
                progress = progress.percent,
                stage = progress.stage,
                detail = progress.detail,
                bytesDownloaded = progress.bytesDownloaded,
                bytesTotal = progress.bytesTotal,
            )
        },
    )
}
