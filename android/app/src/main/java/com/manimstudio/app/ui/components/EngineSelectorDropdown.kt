package com.manimstudio.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.manimstudio.app.ui.theme.*

data class EngineOption(val name: String, val description: String, val icon: ImageVector)

@Composable
fun EngineSelectorDropdown(
    selected: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val engines = listOf(
        EngineOption("Local Engine", "Private · offline rendering",
            Icons.Outlined.PhoneAndroid),
        EngineOption("Groq · Llama 3.3", "Fast · free tier",
            Icons.Outlined.Cloud),
        EngineOption("Gemini 2.0 Flash", "Advanced math & code",
            Icons.Outlined.AutoAwesome),
        EngineOption("OpenAI GPT-4o", "Premium generation",
            Icons.Outlined.Stars),
    )

    // Dismiss on outside tap
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .align(Alignment.TopCenter)
                .clickable(enabled = false) { },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                engines.forEach { engine ->
                    val isSelected = selected == engine.name
                    ListItem(
                        headlineContent = {
                            Text(
                                text = engine.name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                                fontWeight = if (isSelected) FontWeight.Medium
                                             else FontWeight.Normal,
                            )
                        },
                        supportingContent = {
                            Text(
                                text = engine.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        leadingContent = {
                            if (isSelected) {
                                Icon(Icons.Outlined.Check, null,
                                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            } else {
                                Icon(engine.icon, null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                            }
                        },
                        modifier = Modifier.clickable {
                            onSelect(engine.name)
                            onDismiss()
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                            else Color.Transparent
                        ),
                    )
                }
            }
        }
    }
}
