package com.manimstudio.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FloatingPromptInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
    onTemplateClick: () -> Unit,
    isRendering: Boolean,
    selectedEngine: String,
    onEngineClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = containerColor,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(
                start = 16.dp, end = 12.dp,
                top = 12.dp, bottom = 10.dp,
            ),
        ) {
            // Text input area (top of the box)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 36.dp)
                    .padding(bottom = 10.dp),
            ) {
                BasicTextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = { if (text.isNotBlank()) onSend() }
                    ),
                    decorationBox = { inner ->
                        if (text.isEmpty()) {
                            Text(
                                "Describe your animation...",
                                style = TextStyle(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 16.sp,
                                ),
                            )
                        }
                        inner()
                    },
                )
            }

            // Bottom action row — inside the box
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // [+] Templates button
                IconButton(
                    onClick = onTemplateClick,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        Icons.Outlined.Add,
                        contentDescription = "Templates",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Engine selector chip — like Claude's model selector
                Surface(
                    onClick = onEngineClick,
                    shape = RoundedCornerShape(percent = 50),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.height(32.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = selectedEngine,
                            style = TextStyle(
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                            ),
                        )
                        Icon(
                            Icons.Outlined.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Mic button
                AnimatedVisibility(
                    visible = !isRendering && text.isEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    IconButton(
                        onClick = { /* voice input */ },
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            Icons.Outlined.Mic,
                            contentDescription = "Voice input",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Send / Stop button
                AnimatedContent(
                    targetState = isRendering,
                    transitionSpec = {
                        scaleIn(tween(200)) + fadeIn() togetherWith
                        scaleOut(tween(150)) + fadeOut()
                    },
                    label = "sendStop",
                ) { rendering ->
                    if (rendering) {
                        // Stop — square icon in rounded container
                        Surface(
                            onClick = onStop,
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Outlined.Stop,
                                    contentDescription = "Stop",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    } else {
                        // Send — only enabled when text is not empty
                        Surface(
                            onClick = { if (text.isNotBlank()) onSend() },
                            shape = CircleShape,
                            color = if (text.isNotBlank())
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            modifier = Modifier.size(36.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Outlined.ArrowUpward,
                                    contentDescription = "Send",
                                    tint = if (text.isNotBlank())
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
