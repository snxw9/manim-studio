package com.manimstudio.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.manimstudio.app.engine.SetupViewModel
import com.manimstudio.app.ui.components.VideoPlayerBubble
import com.manimstudio.app.ui.components.animations.RenderingGradientBackground
import kotlinx.coroutines.delay
import java.io.File
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun WelcomeRenderScreen(
    setupViewModel: SetupViewModel,
    userName: String,
    onComplete: () -> Unit,
) {
    var renderState by remember { mutableStateOf(WelcomeRenderState.RENDERING) }
    var videoFile by remember { mutableStateOf<File?>(null) }

    LaunchedEffect(Unit) {
        // Bootstrap is installed at this point — safe to render
        val welcomeCode = buildWelcomeCode(userName)
        val result = setupViewModel.runWelcomeRender(welcomeCode)
        when {
            result.success && result.videoFile != null -> {
                videoFile = result.videoFile
                renderState = WelcomeRenderState.PLAYING
            }
            else -> {
                // Render failed — don't block user, just skip to main app
                renderState = WelcomeRenderState.SKIPPED
            }
        }
    }

    // Auto-skip if render fails or takes too long
    LaunchedEffect(renderState) {
        if (renderState == WelcomeRenderState.SKIPPED) {
            delay(1200.milliseconds)
            onComplete()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        RenderingGradientBackground()

        Column(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            when (renderState) {
                WelcomeRenderState.RENDERING -> {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp),
                        strokeWidth = 3.dp,
                    )
                    Spacer(Modifier.height(20.dp))
                    Text("Creating your welcome animation...",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Light, textAlign = TextAlign.Center)
                    Text("First render may take a minute",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp))
                }
                WelcomeRenderState.PLAYING -> {
                    videoFile?.let { file ->
                        VideoPlayerBubble(
                            videoFile = file, autoPlay = true,
                            modifier = Modifier.fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(20.dp)),
                        )
                        Spacer(Modifier.height(28.dp))
                        Button(
                            onClick = onComplete,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(percent = 50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary),
                        ) {
                            Text("Start creating", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                WelcomeRenderState.SKIPPED -> {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(16.dp))
                    Text("Almost there...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

enum class WelcomeRenderState { RENDERING, PLAYING, SKIPPED }

private fun buildWelcomeCode(name: String): String = """
from manim import *
import random

class WelcomeScene(Scene):
    def construct(self):
        welcome = Text("Welcome", font_size=44, color=WHITE, weight=BOLD)
        name_text = Text("$name", font_size=64, color=ORANGE, weight=BOLD)
        name_text.next_to(welcome, DOWN, buff=0.3)
        VGroup(welcome, name_text).move_to(ORIGIN)
        self.play(
            LaggedStart(Write(welcome), Write(name_text), lag_ratio=0.4),
            run_time=1.5,
        )
        self.wait(1.5)
        self.play(*[FadeOut(m) for m in self.mobjects], run_time=0.8)
""".trimIndent()
