package com.manimstudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioTopAppBar(
    currentEngine: String = "Local Engine",
    onEngineSelected: (String) -> Unit = {},
    onMenuClick: () -> Unit = {},
    onNewChatClick: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            actionIconContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground
        ),
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Outlined.Menu, contentDescription = "Menu")
            }
        },
        title = {
            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { expanded = true }
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = currentEngine,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Outlined.KeyboardArrowDown, 
                        contentDescription = "Expand",
                        modifier = Modifier.size(20.dp)
                    )
                }

                MaterialTheme(
                    shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(24.dp))
                ) {
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                            .width(280.dp)
                            .padding(vertical = 12.dp)
                    ) {
                        EngineMenuItem("Local Engine", "Private, offline rendering", currentEngine == "Local Engine") {
                            onEngineSelected("Local Engine")
                            expanded = false
                        }
                        EngineMenuItem("Gemini 1.5 Pro", "Advanced math & code", currentEngine == "Gemini 1.5 Pro") {
                            onEngineSelected("Gemini 1.5 Pro")
                            expanded = false
                        }
                        EngineMenuItem("OpenAI GPT-4o", "Fastest generation", currentEngine == "OpenAI GPT-4o") {
                            onEngineSelected("OpenAI GPT-4o")
                            expanded = false
                        }
                    }
                }
            }
        },
        actions = {
            IconButton(onClick = onNewChatClick) {
                Icon(Icons.Outlined.Edit, contentDescription = "New Chat")
            }
        }
    )
}

@Composable
fun EngineMenuItem(title: String, subtitle: String, isSelected: Boolean, onClick: () -> Unit) {
    DropdownMenuItem(
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.width(36.dp), contentAlignment = Alignment.CenterStart) {
                    if (isSelected) {
                        Icon(Icons.Outlined.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                }
                Column {
                    Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge)
                    Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    )
}
