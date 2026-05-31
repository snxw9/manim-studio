package com.manimstudio.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.manimstudio.app.data.models.RenderQuality
import com.manimstudio.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenderSettingChips(
    quality: RenderQuality,
    format: String,
    onQualityChange: (RenderQuality) -> Unit,
    onFormatChange: (String) -> Unit,
    onRender: () -> Unit,
    estimatedTime: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Quality chips row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Quality",
                style = MaterialTheme.typography.labelMedium,
                color = OnSurfaceVariant,
                modifier = Modifier.width(52.dp),
            )
            RenderQuality.entries.forEach { q ->
                FilterChip(
                    selected = quality == q,
                    onClick = { onQualityChange(q) },
                    label = { Text(q.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryContainer,
                        selectedLabelColor = OnPrimaryContainer,
                        containerColor = SurfaceBright,
                        labelColor = OnSurfaceVariant,
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = quality == q,
                        selectedBorderColor = Primary,
                        borderColor = Outline,
                    ),
                )
            }
        }

        // Format chips row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Format",
                style = MaterialTheme.typography.labelMedium,
                color = OnSurfaceVariant,
                modifier = Modifier.width(52.dp),
            )
            listOf("MP4", "GIF").forEach { fmt ->
                FilterChip(
                    selected = format == fmt,
                    onClick = { onFormatChange(fmt) },
                    label = { Text(fmt) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryContainer,
                        selectedLabelColor = OnPrimaryContainer,
                        containerColor = SurfaceBright,
                        labelColor = OnSurfaceVariant,
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = format == fmt,
                        selectedBorderColor = Primary,
                        borderColor = Outline,
                    ),
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Estimate text
            Text(
                text = "~$estimatedTime",
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceDim,
            )
        }

        // Render button — full width orange pill
        Button(
            onClick = onRender,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(percent = 50),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Render Animation",
                style = MaterialTheme.typography.titleSmall,
            )
        }
    }
}
