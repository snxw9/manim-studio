package com.manimstudio.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EngineBottomSheet(
    visible: Boolean,
    selected: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(36.dp).height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))
            )
        },
        tonalElevation = 2.dp,
    ) {
        // Title
        Text(
            "Select engine",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            thickness = 0.5.dp,
        )

        val engines = listOf(
            EngineInfo(
                name = "Local Engine",
                description = "Private · renders on your device",
                icon = Icons.Outlined.PhoneAndroid,
                tint = Color(0xFF4CAF50), // green — offline/private
                badge = "OFFLINE",
            ),
            EngineInfo(
                name = "Groq · Llama 3.3",
                description = "Fast · free tier available",
                icon = Icons.Outlined.Bolt,
                tint = Color(0xFF2196F3), // blue — fast
                badge = "FREE",
            ),
            EngineInfo(
                name = "Gemini 2.0 Flash",
                description = "Advanced math & code generation",
                icon = Icons.Outlined.AutoAwesome,
                tint = Color(0xFF9C27B0), // purple — AI
                badge = null,
            ),
            EngineInfo(
                name = "OpenAI GPT-4o",
                description = "Premium quality generation",
                icon = Icons.Outlined.Stars,
                tint = Color(0xFFFF9800), // orange — premium
                badge = "API KEY",
            ),
        )

        engines.forEach { engine ->
            val isSelected = selected == engine.name
            val bgColor by animateColorAsState(
                targetValue = if (isSelected)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                else Color.Transparent,
                animationSpec = tween(200),
                label = "engineBg",
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bgColor)
                    .clickable { onSelect(engine.name); onDismiss() }
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Engine icon with colored background pill
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(engine.tint.copy(alpha = if (isSelected) 0.2f else 0.1f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        engine.icon, null,
                        tint = engine.tint,
                        modifier = Modifier.size(20.dp),
                    )
                }

                // Text
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            engine.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface,
                        )
                        // Badge pill
                        engine.badge?.let { badge ->
                            Text(
                                badge,
                                style = TextStyle(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                ),
                                color = engine.tint,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(engine.tint.copy(alpha = 0.12f))
                                    .padding(horizontal = 5.dp, vertical = 2.dp),
                            )
                        }
                    }
                    Text(
                        engine.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Checkmark
                AnimatedVisibility(
                    visible = isSelected,
                    enter = fadeIn(tween(150)) + scaleIn(
                        initialScale = 0.6f,
                        animationSpec = spring(Spring.DampingRatioMediumBouncy),
                    ),
                    exit = fadeOut(tween(100)) + scaleOut(targetScale = 0.6f),
                ) {
                    Icon(
                        Icons.Outlined.Check, null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }

        Spacer(Modifier.navigationBarsPadding().height(8.dp))
    }
}

data class EngineInfo(
    val name: String,
    val description: String,
    val icon: ImageVector,
    val tint: Color,
    val badge: String?,
)
