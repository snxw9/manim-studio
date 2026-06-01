package com.manimstudio.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.manimstudio.app.data.models.RenderQuality

@Composable
fun CompactRenderChips(
    quality: RenderQuality,
    format: String,
    onQualityChange: (RenderQuality) -> Unit,
    onFormatChange: (String) -> Unit,
    onRender: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            // Single row: quality chips + format chips + render button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                // Quality
                RenderQuality.entries.forEach { q ->
                    SmallChip(
                        label = q.label,
                        selected = quality == q,
                        onClick = { onQualityChange(q) },
                    )
                }
                Spacer(Modifier.width(2.dp))
                // Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(18.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                Spacer(Modifier.width(2.dp))
                // Format
                listOf("MP4", "GIF").forEach { fmt ->
                    SmallChip(
                        label = fmt,
                        selected = format == fmt,
                        onClick = { onFormatChange(fmt) },
                    )
                }
                Spacer(Modifier.weight(1f))
                // Compact render button
                FilledTonalButton(
                    onClick = onRender,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Icon(Icons.Outlined.PlayArrow, null,
                        modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Render", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
fun SmallChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer
                     else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(200), label = "chipBg",
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                     else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200), label = "chipText",
    )
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = textColor,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}
