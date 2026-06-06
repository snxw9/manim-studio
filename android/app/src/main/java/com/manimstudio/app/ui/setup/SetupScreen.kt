package com.manimstudio.app.ui.setup

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import com.manimstudio.app.engine.SetupState
import com.manimstudio.app.engine.SetupViewModel

@Composable
fun SetupScreen(
    state: SetupState,
    viewModel: SetupViewModel,
    onStartSetup: () -> Unit,
    onRetry: () -> Unit,
    onTestRender: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val orangeAccent = MaterialTheme.colorScheme.primary
    val textPrimary = MaterialTheme.colorScheme.onBackground
    val textMuted = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        // Settings Button (Top End)
        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = textMuted)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // App name
            Text(
                text = "Manim Studio",
                fontSize = 28.sp,
                color = textPrimary,
                fontFamily = FontFamily.Monospace,
            )

            when (state.phase) {
                SetupState.Phase.CHECKING -> {
                    CircularProgressIndicator(color = orangeAccent)
                }

                SetupState.Phase.NEEDS_SETUP -> {
                    NeedsSetupContent(
                        onStart = onStartSetup,
                        orangeAccent = orangeAccent,
                        textPrimary = textPrimary,
                        textMuted = textMuted,
                    )
                }

                SetupState.Phase.WIFI_REQUIRED -> {
                    WifiRequiredContent(
                        onWifiOnly = {
                            viewModel.retrySetup()
                        },
                        onContinueAnyway = {
                            viewModel.confirmMobileDataInstall()
                        },
                        orangeAccent = orangeAccent,
                        textPrimary = textPrimary,
                        textMuted = textMuted,
                    )
                }

                SetupState.Phase.INSTALLING -> {
                    InstallingContent(
                        state = state,
                        orangeAccent = orangeAccent,
                        textPrimary = textPrimary,
                        textMuted = textMuted,
                    )
                }

                SetupState.Phase.READY -> {
                    ReadyContent(
                        onTestRender = onTestRender,
                        orangeAccent = orangeAccent,
                        textPrimary = textPrimary,
                        textMuted = textMuted,
                    )
                }

                SetupState.Phase.ERROR -> {
                    ErrorContent(
                        error = state.error ?: "Unknown error",
                        onRetry = onRetry,
                        orangeAccent = orangeAccent,
                        textPrimary = textPrimary,
                        textMuted = textMuted,
                    )
                }
            }
        }
    }
}

@Composable
private fun NeedsSetupContent(
    onStart: () -> Unit,
    orangeAccent: Color,
    textPrimary: Color,
    textMuted: Color,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "One-time setup required",
            fontSize = 18.sp,
            color = textPrimary,
        )

        Text(
            text = "Manim Studio needs to download the animation engine (≈300MB). " +
                   "This happens once and requires a Wi-Fi connection.",
            fontSize = 14.sp,
            color = textMuted,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
        )

        // What gets installed
        SetupInfoCard(orangeAccent, textMuted)

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onStart,
            colors = ButtonDefaults.buttonColors(containerColor = orangeAccent),
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(
                text = "Set Up Manim Studio",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onPrimary,
                fontFamily = FontFamily.Monospace,
            )
        }
    }
}

@Composable
private fun SetupInfoCard(accent: Color, textMuted: Color) {
    val items = listOf(
        "Python 3.11 runtime",
        "Cairo rendering engine",
        "Manim animation library",
        "FFmpeg video encoder",
        "LaTeX for math equations",
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items.forEach { item ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(accent)
                )
                Text(text = item, fontSize = 13.sp, color = textMuted,
                    fontFamily = FontFamily.Monospace)
            }
        }
    }
}
@Composable
private fun InstallingContent(
    state: SetupState,
    orangeAccent: Color,
    textPrimary: Color,
    textMuted: Color,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(state.stage, fontSize = 16.sp, color = textPrimary,
            fontFamily = FontFamily.Monospace)

        LinearProgressIndicator(
            progress = { state.progress / 100f },
            modifier = Modifier.fillMaxWidth().height(3.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = orangeAccent,
            trackColor = Color(0xFF2A2A2A),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // Show MB progress when downloading
            val detailText = if (state.bytesTotal > 0) {
                val dlMb = state.bytesDownloaded / (1024 * 1024)
                val totalMb = state.bytesTotal / (1024 * 1024)
                "${dlMb}MB / ${totalMb}MB"
            } else {
                state.detail.takeLast(45)
            }
            Text(
                detailText,
                fontSize = 11.sp,
                color = textMuted,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1f),
            )
            Text(
                "${state.progress}%",
                fontSize = 11.sp,
                color = textMuted,
                fontFamily = FontFamily.Monospace,
            )
        }

        Text(
            "Keep the app open — this only happens once.",
            fontSize = 11.sp,
            color = textMuted,
            textAlign = TextAlign.Center,
        )

        // Estimated time hint
        if (state.progress in 5..74) {
            Text(
                "Large download — Wi-Fi recommended for best speed.",
                fontSize = 10.sp,
                color = Color(0xFF555555),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ReadyContent(
    onTestRender: () -> Unit,
    orangeAccent: Color,
    textPrimary: Color,
    textMuted: Color,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = "Ready to animate", fontSize = 20.sp, color = textPrimary,
            fontFamily = FontFamily.Monospace)
        Text(text = "Manim is installed and ready on your device.",
            fontSize = 14.sp, color = textMuted, textAlign = TextAlign.Center)

        Button(
            onClick = onTestRender,
            colors = ButtonDefaults.buttonColors(containerColor = orangeAccent),
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text("Run Test Render", color = MaterialTheme.colorScheme.onPrimary,
                fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    orangeAccent: Color,
    textPrimary: Color,
    textMuted: Color,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = "Setup failed", fontSize = 18.sp, color = MaterialTheme.colorScheme.error)
        Text(
            text = "Check your internet connection and try again.",
            fontSize = 14.sp,
            color = textMuted,
            textAlign = TextAlign.Center
        )

        SelectionContainer {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = error,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = orangeAccent),
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text("Try Again", color = MaterialTheme.colorScheme.onPrimary, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun WifiRequiredContent(
    onWifiOnly: () -> Unit,
    onContinueAnyway: () -> Unit,
    orangeAccent: Color,
    textPrimary: Color,
    textMuted: Color,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
    ) {
        Icon(
            Icons.Outlined.Wifi,
            contentDescription = null,
            tint = orangeAccent,
            modifier = Modifier.size(48.dp),
        )

        Text(
            "Wi-Fi recommended",
            fontSize = 20.sp,
            color = textPrimary,
            fontFamily = FontFamily.Monospace,
        )

        Text(
            "The bootstrap archive is approximately 400MB. " +
            "Downloading on mobile data may incur charges from your carrier.",
            fontSize = 13.sp,
            color = textMuted,
            textAlign = TextAlign.Center,
            lineHeight = 19.sp,
        )

        // Size breakdown
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1A1A1A))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            SizeRow("Python 3.11 + Manim", "~80MB", textMuted)
            SizeRow("Cairo + Pango libraries", "~30MB", textMuted)
            SizeRow("FFmpeg + fonts", "~50MB", textMuted)
            SizeRow("LaTeX (for equations)", "~240MB", textMuted)
            HorizontalDivider(color = Color(0xFF2A2A2A))
            SizeRow("Total download", "~400MB", textPrimary)
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Primary — wait for Wi-Fi
        Button(
            onClick = onWifiOnly,
            colors = ButtonDefaults.buttonColors(containerColor = orangeAccent),
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text("Wait for Wi-Fi", color = Color(0xFF0D0D0D),
                fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
        }

        // Secondary — continue on mobile data
        OutlinedButton(
            onClick = onContinueAnyway,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color(0xFF3A3A3A)),
        ) {
            Text("Continue anyway (mobile data)",
                color = textMuted, fontSize = 13.sp,
                fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun SizeRow(label: String, size: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, fontSize = 12.sp, color = color,
            fontFamily = FontFamily.Monospace)
        Text(size, fontSize = 12.sp, color = color,
            fontFamily = FontFamily.Monospace)
    }
}
