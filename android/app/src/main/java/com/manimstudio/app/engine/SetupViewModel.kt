package com.manimstudio.app.engine

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    val diagnostics: String = "",
    val updateAvailable: BootstrapManifest? = null,
    val bootstrapSizeBytes: Long = 0L,
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
        viewModelScope.launch {
            if (isBootstrapInstalled()) {
                _state.value = SetupState(phase = SetupState.Phase.READY, progress = 100)
                checkForUpdateAsync()
            } else {
                _state.value = SetupState(phase = SetupState.Phase.NEEDS_SETUP)
                // Fetch manifest in background to get real size
                launch {
                    try {
                        val installer = makeInstaller()
                        val manifest = installer.fetchManifestPublic()
                        if (manifest != null) {
                            _state.update { it.copy(
                                bootstrapSizeBytes = manifest.bootstrap_size_bytes
                            )}
                        }
                    } catch (_: Exception) {}
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
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
                        diagnostics = result.diagnostics,
                    )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun confirmMobileDataInstall() = startInstallation(allowMobileData = true)

    fun retrySetup() {
        installJob?.cancel()
        engine.rootfsDir.deleteRecursively()
        File(engine.filesDir, ".installed").delete()
        _state.value = SetupState(phase = SetupState.Phase.NEEDS_SETUP)
    }

    // ── Welcome render (only called after bootstrap is confirmed ready) ────────

    suspend fun runWelcomeRender(code: String): RenderResult {
        return renderer.render(code, "480p") { /* progress ignored for welcome */ }
    }

    // ── Background update check ───────────────────────────────────────────────

    private fun makeInstaller(): BootstrapInstaller {
        return BootstrapInstaller(
            context = getApplication(), engine = engine, onProgress = {})
    }

    private fun checkForUpdateAsync() {
        viewModelScope.launch {
            try {
                val installer = makeInstaller()
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
