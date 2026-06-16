package com.manimstudio.app.engine

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class SetupState(
    val phase: Phase = Phase.CHECKING,
    val progress: Int = 0,
    val stage: String = "",
    val detail: String = "",
    val bytesDownloaded: Long = 0L,
    val bytesTotal: Long = 0L,
    val error: String? = null,
    val updateAvailable: BootstrapManifest? = null,
) {
    enum class Phase {
        CHECKING, NEEDS_SETUP, WIFI_REQUIRED, INSTALLING, READY, ERROR
    }
}

class SetupViewModel(app: Application) : AndroidViewModel(app) {

    val engine = ProotEngine(app)
    private val renderer = ManimRenderer(engine)
    private var installJob: Job? = null

    private val _state = MutableStateFlow(SetupState())
    val state: StateFlow<SetupState> = _state.asStateFlow()

    // ── Public checks ─────────────────────────────────────────────────────────

    fun isBootstrapInstalled(): Boolean {
        val marker = File(engine.filesDir, ".installed")
        return marker.exists() && engine.pythonBin.exists()
    }

    fun getInstalledVersion(): String? =
        File(engine.filesDir, ".installed").takeIf { it.exists() }
            ?.readText()?.trim()

    // ── Setup flow ────────────────────────────────────────────────────────────

    fun beginSetupCheck() {
        if (isBootstrapInstalled()) {
            _state.value = SetupState(phase = SetupState.Phase.READY, progress = 100)
            checkForUpdateAsync()
        } else {
            _state.value = SetupState(phase = SetupState.Phase.NEEDS_SETUP)
        }
    }

    fun startInstallation(allowMobileData: Boolean = false) {
        installJob?.cancel()
        installJob = viewModelScope.launch {
            _state.value = SetupState(phase = SetupState.Phase.INSTALLING, stage = "Starting...")

            val installer = BootstrapInstaller(
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

            when (val result = installer.install(allowMobileData)) {
                is InstallResult.Success ->
                    _state.value = SetupState(
                        phase = SetupState.Phase.READY, progress = 100,
                        stage = "Engine ready",
                    )
                is InstallResult.WifiRequired ->
                    _state.value = SetupState(phase = SetupState.Phase.WIFI_REQUIRED)
                is InstallResult.Error ->
                    _state.value = SetupState(
                        phase = SetupState.Phase.ERROR,
                        error = result.message,
                    )
            }
        }
    }

    fun confirmMobileDataInstall() = startInstallation(allowMobileData = true)

    fun retrySetup() {
        installJob?.cancel()
        engine.usrDir.deleteRecursively()
        File(engine.filesDir, ".installed").delete()
        _state.value = SetupState(phase = SetupState.Phase.NEEDS_SETUP)
    }

    // ── Welcome render (only called after bootstrap is confirmed ready) ────────

    suspend fun runWelcomeRender(code: String): RenderResult {
        return renderer.render(code, "480p") { /* progress ignored for welcome */ }
    }

    // ── Background update check ───────────────────────────────────────────────

    private fun checkForUpdateAsync() {
        viewModelScope.launch {
            try {
                val installer = BootstrapInstaller(
                    context = getApplication(), engine = engine, onProgress = {})
                val update = installer.checkForUpdate()
                if (update != null) {
                    _state.value = _state.value.copy(updateAvailable = update)
                }
            } catch (_: Exception) {
                // Silent fail — update check is non-critical
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        installJob?.cancel()
    }
}
