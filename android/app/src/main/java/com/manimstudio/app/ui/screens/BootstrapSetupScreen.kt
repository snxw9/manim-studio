package com.manimstudio.app.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manimstudio.app.engine.SetupState
import com.manimstudio.app.engine.SetupViewModel
import com.manimstudio.app.ui.components.animations.RenderingGradientBackground
import com.manimstudio.app.ui.components.animations.SparkIcon
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun BootstrapSetupScreen(
    setupViewModel: SetupViewModel,
    userName: String,
    setText: (Clipboard.(AnnotatedString) -> Unit)? = null,
    onComplete: () -> Unit,
) {
    val state by setupViewModel.state.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboard.current

    // Auto-proceed when ready
    LaunchedEffect(state.phase) {
        if (state.phase == SetupState.Phase.READY) {
            delay(800.milliseconds) // brief pause so user sees 100%
            onComplete()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        // Gradient activates during download
        AnimatedVisibility(
            visible = state.phase == SetupState.Phase.INSTALLING,
            enter = fadeIn(tween(1000)),
            exit = fadeOut(tween(500)),
        ) {
            RenderingGradientBackground()
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            when (state.phase) {
                SetupState.Phase.CHECKING -> {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp),
                    )
                    Text("Checking installation...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                SetupState.Phase.NEEDS_SETUP -> {
                    NeedsSetupContent(
                        userName = userName,
                        bootstrapSizeBytes = state.bootstrapSizeBytes,
                        onStart = { setupViewModel.startInstallation(allowMobileData = false) },
                    )
                }

                SetupState.Phase.WIFI_REQUIRED -> {
                    WifiRequiredContent(
                        bootstrapSizeBytes = state.bootstrapSizeBytes,
                        onWifiOnly = { /* User will connect to Wi-Fi and retry */ },
                        onContinueAnyway = { setupViewModel.confirmMobileDataInstall() },
                    )
                }

                SetupState.Phase.INSTALLING -> {
                    InstallingContent(state = state)
                }

                SetupState.Phase.READY -> {
                    ReadyContent()
                }

                SetupState.Phase.ERROR -> {
                    ErrorContent(
                        error = state.error ?: "Unknown error",
                        diagnostics = state.diagnostics,
                        onRetry = { setupViewModel.retrySetup() },
                        onCopyDiagnostics = {
                            setText?.invoke(
                                clipboardManager,
                                androidx.compose.ui.text.AnnotatedString(
                                    "ERROR:\n${state.error}\n\nDIAGNOSTICS:\n${state.diagnostics}"
                                )
                            )
                        },
                    )
                }
            }
        }
    }

    // Start checking immediately on composition
    LaunchedEffect(Unit) {
        if (state.phase == SetupState.Phase.CHECKING) {
            setupViewModel.beginSetupCheck()
        }
    }
}

@Composable
private fun NeedsSetupContent(
    userName: String,
    bootstrapSizeBytes: Long,
    onStart: () -> Unit,
) {
    val totalSize = formatSize(bootstrapSizeBytes)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SparkIcon(modifier = Modifier.size(48.dp))

        Text(
            "One more step, $userName",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.Center,
        )
        Text(
            "Manim Studio needs to download the animation engine. " +
            "This happens once and takes about 5 minutes on Wi-Fi.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
        )

        // What's in the download
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                DownloadItem("Python 3.11 + Manim", "~80MB")
                DownloadItem("Cairo rendering engine", "~30MB")
                DownloadItem("FFmpeg video encoder", "~50MB")
                DownloadItem("LaTeX math renderer", "~240MB")
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(vertical = 2.dp),
                )
                DownloadItem("Total", totalSize, highlight = true)
            }
        }

        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(percent = 50),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
            Icon(Icons.Outlined.Download, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Download Engine", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun DownloadItem(label: String, size: String, highlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (highlight) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (highlight) FontWeight.SemiBold else FontWeight.Normal,
        )
        Text(
            size,
            style = MaterialTheme.typography.bodyMedium,
            color = if (highlight) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (highlight) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
private fun InstallingContent(state: SetupState) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            state.stage,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Light,
        )

        // Progress bar
        LinearProgressIndicator(
            progress = { state.progress / 100f },
            modifier = Modifier.fillMaxWidth().height(3.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outlineVariant,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            val detail = when {
                state.bytesTotal > 0 -> {
                    val dl = state.bytesDownloaded / (1024 * 1024)
                    val total = state.bytesTotal / (1024 * 1024)
                    "${dl}MB / ${total}MB"
                }
                state.detail.isNotEmpty() -> state.detail.takeLast(40)
                else -> ""
            }
            Text(detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1f))
            Text("${state.progress}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace)
        }

        Text(
            "Keep the app open. This only happens once.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ReadyContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            Icons.Outlined.CheckCircle, null,
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(48.dp),
        )
        Text("Engine ready",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground)
        Text("Preparing your welcome...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ErrorContent(
    error: String,
    diagnostics: String,
    onRetry: () -> Unit,
    onCopyDiagnostics: () -> Unit,
) {
    val scrollState = rememberScrollState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
    ) {
        Icon(
            Icons.Outlined.ErrorOutline, null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(40.dp),
        )

        Text(
            "Setup failed",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error,
        )

        // Error message box
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                error,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    lineHeight = 16.sp,
                ),
                modifier = Modifier.padding(12.dp),
            )
        }

        // Diagnostics — always visible, full detail
        if (diagnostics.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Diagnostics",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = onCopyDiagnostics) {
                    Icon(Icons.Outlined.ContentCopy, null,
                        modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Copy", style = MaterialTheme.typography.labelSmall)
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    0.5.dp,
                    MaterialTheme.colorScheme.outlineVariant,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    diagnostics,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        lineHeight = 13.sp,
                    ),
                    modifier = Modifier.padding(10.dp),
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(percent = 50),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
            Text("Try Again", fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun WifiRequiredContent(
    bootstrapSizeBytes: Long,
    onWifiOnly: () -> Unit,
    onContinueAnyway: () -> Unit,
) {
    val totalSize = formatSize(bootstrapSizeBytes)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(Icons.Outlined.Wifi, null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp))
        Text("Wi-Fi recommended",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Light)
        Text(
            "The download is approximately $totalSize. " +
            "This may use significant mobile data and could take longer.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center, lineHeight = 22.sp,
        )
        Button(
            onClick = onWifiOnly,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(percent = 50),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary),
        ) { Text("Wait for Wi-Fi", fontWeight = FontWeight.SemiBold) }

        OutlinedButton(
            onClick = onContinueAnyway,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(percent = 50),
        ) {
            Text("Continue on mobile data",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun formatSize(bytes: Long): String {
    if (bytes <= 0L) return "~600MB" // fallback while loading
    val mb = bytes / (1024L * 1024L)
    return if (mb >= 1024) {
        val gb = mb / 1024f
        "%.1f GB".format(gb)
    } else {
        "${mb}MB"
    }
}
