package com.manimstudio.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manimstudio.app.ui.components.animations.GlobalGradientBackground
import com.manimstudio.app.ui.components.animations.SparkIcon
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun OnboardingScreen(onNameSaved: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var displayedText by remember { mutableStateOf("") }
    val fullText = "Hi, I'm Manim Studio"

    LaunchedEffect(Unit) {
        delay(400.milliseconds)
        fullText.forEach { char ->
            displayedText += char
            delay(45.milliseconds)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding(),
        contentAlignment = Alignment.Center,
    ) {
        GlobalGradientBackground(intensity = 0.5f)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            SparkIcon(modifier = Modifier.size(56.dp))

            AnimatedVisibility(
                visible = displayedText.isNotEmpty(),
                enter = fadeIn(tween(400)),
            ) {
                Text(
                    text = displayedText,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Light,
                )
            }

            AnimatedVisibility(
                visible = displayedText == fullText,
                enter = fadeIn(tween(600, delayMillis = 200)) + slideInVertically { it / 2 },
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    Text(
                        text = "What should I call you?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )

                    BasicTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(20.dp),
                        textStyle = TextStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Light,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { if (name.isNotBlank()) onNameSaved(name.trim()) }
                        ),
                        singleLine = true,
                        decorationBox = { inner ->
                            Box(contentAlignment = Alignment.Center) {
                                if (name.isEmpty()) {
                                    Text(
                                        "Your name or nickname",
                                        style = TextStyle(
                                            fontSize = 18.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                                .copy(alpha = 0.35f),
                                            textAlign = TextAlign.Center,
                                        ),
                                    )
                                }
                                inner()
                            }
                        },
                    )
                }
            }

            AnimatedVisibility(
                visible = name.isNotBlank(),
                enter = fadeIn() + scaleIn(
                    initialScale = 0.85f,
                    animationSpec = spring(Spring.DampingRatioMediumBouncy),
                ),
                exit = fadeOut() + scaleOut(targetScale = 0.85f),
            ) {
                Button(
                    onClick = { onNameSaved(name.trim()) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(percent = 50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Text(
                        "Continue",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Outlined.ArrowForward, null,
                        modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
