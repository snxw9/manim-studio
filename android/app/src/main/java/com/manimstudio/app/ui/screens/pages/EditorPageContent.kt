package com.manimstudio.app.ui.screens.pages

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manimstudio.app.data.models.ChatMessage
import com.manimstudio.app.ui.components.SyntaxHighlightedEditor
import com.manimstudio.app.ui.theme.*
import com.manimstudio.app.viewmodel.StudioPhase

@Composable
fun EditorPageContent(
    code: String,
    onCodeChanged: (String) -> Unit,
    phase: StudioPhase,
    renderProgress: String,
    elapsedSeconds: Int,
    messages: List<ChatMessage>,
) {
    Column(modifier = Modifier.fillMaxSize()) {

        // Phase indicator bar below tab row
        AnimatedContent(
            targetState = phase,
            transitionSpec = {
                fadeIn(tween(300)) togetherWith fadeOut(tween(300))
            },
            label = "phase_bar"
        ) { currentPhase ->
            when (currentPhase) {
                StudioPhase.GENERATING -> {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().height(2.dp),
                        color = Primary,
                        trackColor = SurfaceVariant,
                    )
                }
                StudioPhase.RENDERING -> {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().height(2.dp),
                        color = Primary,
                        trackColor = SurfaceVariant,
                    )
                }
                else -> Spacer(modifier = Modifier.height(2.dp))
            }
        }

        // Status row
        AnimatedVisibility(
            visible = phase == StudioPhase.GENERATING ||
                      phase == StudioPhase.RENDERING,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryContainer)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        color = Primary,
                        strokeWidth = 2.dp,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = renderProgress.ifEmpty {
                            if (phase == StudioPhase.GENERATING) "Generating code..."
                            else "Rendering..."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = OnPrimaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = "${elapsedSeconds}s",
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceVariant,
                )
            }
        }

        // Code editor area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF0D0D0D)),
        ) {
            if (code.isEmpty() && phase == StudioPhase.GENERATING) {
                // Typing animation placeholder
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            } else {
                SyntaxHighlightedEditor(
                    code = code,
                    onCodeChanged = onCodeChanged,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            // Copy button top right
            IconButton(
                onClick = { /* copy to clipboard */ },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(36.dp)
                    .background(SurfaceVariant, CircleShape),
            ) {
                Icon(
                    imageVector = Icons.Outlined.ContentCopy,
                    contentDescription = "Copy code",
                    tint = OnSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        // Bottom console strip
        Surface(
            color = SurfaceDim,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Terminal,
                        contentDescription = null,
                        tint = OnSurfaceDim,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CONSOLE",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceDim,
                        letterSpacing = 1.sp,
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                color = when (phase) {
                                    StudioPhase.RENDERING -> Primary
                                    StudioPhase.DONE -> Success
                                    StudioPhase.ERROR -> Error
                                    else -> OnSurfaceDim
                                },
                                shape = CircleShape,
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (phase) {
                            StudioPhase.RENDERING -> "Rendering"
                            StudioPhase.GENERATING -> "Generating"
                            StudioPhase.DONE -> "Ready"
                            StudioPhase.ERROR -> "Error"
                            else -> "Idle"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceDim,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(120.dp)) // space for input + chips
    }
}
