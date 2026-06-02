package com.manimstudio.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.manimstudio.app.engine.ManimRenderer
import com.manimstudio.app.engine.SetupViewModel
import com.manimstudio.app.ui.components.VideoPlayerBubble
import com.manimstudio.app.ui.components.animations.AmbientGlow
import com.manimstudio.app.ui.components.animations.GlobalGradientBackground
import com.manimstudio.app.ui.components.animations.RenderingGradientBackground
import com.manimstudio.app.ui.components.animations.SparkIcon
import com.manimstudio.app.ui.theme.*
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun OnboardingScreen(
    onComplete: (String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var phase by remember { mutableStateOf(OnboardingPhase.NAME_INPUT) }
    var isAnimating by remember { mutableStateOf(false) }

    // Animated welcome text that types out letter by letter
    var displayedText by remember { mutableStateOf("") }
    val fullText = "Hi, I'm Manim Studio"

    LaunchedEffect(Unit) {
        delay(600)
        fullText.forEach { char ->
            displayedText += char
            delay(50)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        // Background animated gradient
        GlobalGradientBackground(intensity = 0.35f, animate = true)

        AnimatedContent(
            targetState = phase,
            transitionSpec = {
                fadeIn(tween(600)) togetherWith fadeOut(tween(400))
            },
            label = "onboarding",
        ) { currentPhase ->
            when (currentPhase) {
                OnboardingPhase.NAME_INPUT -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                    ) {
                        SparkIcon(
                            modifier = Modifier.size(64.dp),
                            color = MaterialTheme.colorScheme.primary,
                        )

                        Text(
                            text = displayedText,
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                        )

                        Text(
                            text = "What should I call you?",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Name input — clean, borderless style
                        BasicTextField(
                            value = name,
                            onValueChange = { name = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                                .padding(20.dp),
                            textStyle = TextStyle(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Light,
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center,
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (name.isNotBlank()) {
                                        phase = OnboardingPhase.RENDERING_WELCOME
                                    }
                                },
                            ),
                            decorationBox = { inner ->
                                Box(contentAlignment = Alignment.Center) {
                                    if (name.isEmpty()) {
                                        Text(
                                            "Your name or nickname",
                                            style = TextStyle(
                                                fontSize = 18.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                textAlign = TextAlign.Center,
                                            ),
                                        )
                                    }
                                    inner()
                                }
                            },
                        )

                        AnimatedVisibility(visible = name.isNotBlank()) {
                            Button(
                                onClick = { phase = OnboardingPhase.RENDERING_WELCOME },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(percent = 50),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                ),
                            ) {
                                Text(
                                    "Continue",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.AutoMirrored.Outlined.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                OnboardingPhase.RENDERING_WELCOME -> {
                    WelcomeRenderPhase(
                        name = name.trim(),
                        onComplete = { onComplete(name.trim()) },
                    )
                }
            }
        }
    }
}

enum class OnboardingPhase { NAME_INPUT, RENDERING_WELCOME }

@Composable
fun WelcomeRenderPhase(
    name: String,
    onComplete: () -> Unit,
) {
    val viewModel: SetupViewModel = viewModel()

    var renderState by remember { mutableStateOf(WelcomeRenderState.GENERATING) }
    var videoFile by remember { mutableStateOf<File?>(null) }

    // Generate and render welcome animation
    LaunchedEffect(name) {
        renderState = WelcomeRenderState.RENDERING

        val welcomeCode = """
import random
from manim import *

class WelcomeScene(Scene):
    def construct(self):
        # Animated welcome message
        welcome = Text(
            "Welcome",
            font_size=48,
            color=WHITE,
            weight=BOLD,
        )
        name_text = Text(
            "$name",
            font_size=64,
            color=ORANGE,
            weight=BOLD,
        )
        name_text.next_to(welcome, DOWN, buff=0.3)
        group = VGroup(welcome, name_text)
        group.move_to(ORIGIN)
        
        self.play(
            LaggedStart(
                Write(welcome),
                Write(name_text),
                lag_ratio=0.4,
            ),
            run_time=1.5,
        )
        self.wait(1)
        
        # Particle burst effect
        dots = VGroup(*[
            Dot(
                color=ORANGE,
                radius=random.uniform(0.05, 0.12),
            ).move_to(
                name_text.get_center() +
                np.array([
                    random.uniform(-3, 3),
                    random.uniform(-2, 2),
                    0
                ])
            )
            for _ in range(20)
        ])
        
        self.play(
            LaggedStart(*[
                GrowFromCenter(d) for d in dots
            ], lag_ratio=0.05),
            run_time=1,
        )
        self.wait(0.5)
        self.play(*[FadeOut(m) for m in self.mobjects], run_time=0.8)
""".trimIndent()

        // Use ManimRenderer directly
        val renderer = ManimRenderer(viewModel.getApplication(), viewModel.engine)
        val result = renderer.render(welcomeCode, "480p") { }

        if (result.success && result.videoFile != null) {
            videoFile = result.videoFile
            renderState = WelcomeRenderState.PLAYING
        } else {
            // Skip if render fails — don't block onboarding
            renderState = WelcomeRenderState.SKIPPED
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        when (renderState) {
            WelcomeRenderState.GENERATING, WelcomeRenderState.RENDERING -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Full-screen gradient — must be first child (background layer)
                    RenderingGradientBackground(
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // Content centered on top of gradient
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        SparkIcon(
                            modifier = Modifier.size(56.dp),
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.height(28.dp))
                        Text(
                            text = "Creating your welcome...",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Light,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Rendering a Manim animation just for you",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(48.dp))
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(2.dp)
                                .clip(RoundedCornerShape(1.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                    }
                }
            }

            WelcomeRenderState.PLAYING -> {
                videoFile?.let { file ->
                    // Play the rendered video
                    VideoPlayerBubble(
                        videoFile = file,
                        autoPlay = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(20.dp)),
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = onComplete,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(percent = 50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Text("Start creating", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            WelcomeRenderState.SKIPPED -> {
                // Fallback — just go to main app
                LaunchedEffect(Unit) {
                    delay(500)
                    onComplete()
                }
            }
        }
    }
}

enum class WelcomeRenderState { GENERATING, RENDERING, PLAYING, SKIPPED }
