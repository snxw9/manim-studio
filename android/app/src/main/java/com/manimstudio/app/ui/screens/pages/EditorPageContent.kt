package com.manimstudio.app.ui.screens.pages

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manimstudio.app.data.models.ChatMessage
import com.manimstudio.app.data.models.StudioPhase
import com.manimstudio.app.ui.components.SyntaxHighlightedEditor

@Composable
fun EditorPageContent(
    code: String,
    onCodeChanged: (String) -> Unit,
    phase: StudioPhase,
    renderProgress: String,
    elapsedSeconds: Int,
    messages: List<ChatMessage> = emptyList(),
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Code editor — full screen, starts from top
        SyntaxHighlightedEditor(
            code = code,
            onCodeChanged = onCodeChanged,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 0.dp), // starts from very top
        )

        // Fade behind top bar (same as home page)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .align(Alignment.TopStart)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
                            Color.Transparent,
                        )
                    )
                )
        )

        // Rendering progress bar — slim, modern, under tab dots
        AnimatedVisibility(
            visible = phase == StudioPhase.RENDERING || phase == StudioPhase.GENERATING,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 90.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Animated shimmer progress bar
                val transition = rememberInfiniteTransition(label = "shimmer")
                val shimmerOffset by transition.animateFloat(
                    initialValue = -1f, targetValue = 2f,
                    animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing)),
                    label = "shimmerOffset",
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                        Color.Transparent,
                                    ),
                                    startX = shimmerOffset * 1000f,
                                    endX = (shimmerOffset + 0.5f) * 1000f,
                                )
                            )
                    )
                }
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = if (phase == StudioPhase.GENERATING)
                            "Generating" else "Rendering",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${elapsedSeconds}s",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Monospace,
                    )
                    if (renderProgress.isNotEmpty()) {
                        Text(
                            text = "· ${renderProgress.takeLast(40)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }

        // Console strip at bottom — stays visible
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 160.dp), // above input bar
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Outlined.Terminal, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp))
                    Text("CONSOLE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.8.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    // Status dot with pulse animation
                    val pulseAnim = rememberInfiniteTransition(label = "pulse")
                    val pulseAlpha by pulseAnim.animateFloat(
                        initialValue = 1f, targetValue = 0.3f,
                        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
                        label = "pulseAlpha",
                    )
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(
                                color = when(phase) {
                                    StudioPhase.RENDERING -> MaterialTheme.colorScheme.primary
                                        .copy(alpha = if (phase == StudioPhase.RENDERING) pulseAlpha else 1f)
                                    StudioPhase.DONE -> Color(0xFF4CAF50)
                                    StudioPhase.ERROR -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                shape = CircleShape,
                            )
                    )
                    Text(
                        text = when(phase) {
                            StudioPhase.RENDERING -> "Rendering"
                            StudioPhase.GENERATING -> "Generating"
                            StudioPhase.DONE -> "Ready"
                            StudioPhase.ERROR -> "Error"
                            else -> "Idle"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
