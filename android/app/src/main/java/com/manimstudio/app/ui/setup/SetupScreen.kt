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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import com.manimstudio.app.engine.SetupState

@Composable
fun SetupScreen(
    state: SetupState,
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
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = state.stage, fontSize = 16.sp, color = textPrimary,
            fontFamily = FontFamily.Monospace)

        LinearProgressIndicator(
            progress = { state.progress / 100f },
            modifier = Modifier.fillMaxWidth().height(4.dp),
            color = orangeAccent,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = state.detail.takeLast(50),
                fontSize = 11.sp,
                color = textMuted,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${state.progress}%",
                fontSize = 11.sp,
                color = textMuted,
                fontFamily = FontFamily.Monospace,
            )
        }

        Text(
            text = "Keep the app open. This may take 5–10 minutes on first launch.",
            fontSize = 12.sp,
            color = textMuted,
            textAlign = TextAlign.Center,
        )
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
