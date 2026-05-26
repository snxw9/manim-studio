package com.manimstudio.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBackClick: () -> Unit = {}) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            SettingsSectionTitle("AI Engine & Processing")
            SettingsCard {
                SettingsListItem("Default Provider", "Gemini 1.5 Pro")
                HorizontalDivider(color = Color(0xFF333333))
                SettingsListItem("Offline Fallback", "Local PRoot Engine")
                HorizontalDivider(color = Color(0xFF333333))
                SettingsListItem("Thinking Level", "Advanced (Complex Math)")
            }

            SettingsSectionTitle("Render Quality")
            SettingsCard {
                SettingsListItem("Resolution", "1080p")
                HorizontalDivider(color = Color(0xFF333333))
                SettingsListItem("Frame Rate", "60 FPS")
                HorizontalDivider(color = Color(0xFF333333))
                SettingsListItem("Aspect Ratio", "16:9 (Landscape)")
            }

            SettingsSectionTitle("Downloads & Storage")
            SettingsCard {
                SettingsListItem("Save Location", "External (Movies/Manim)")
                HorizontalDivider(color = Color(0xFF333333))
                SettingsListItem("Clear Render Cache", "1.2 GB used")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        color = Color(0xFFFF8C00),
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp, top = 24.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1F22)),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            content()
        }
    }
}

@Composable
fun SettingsListItem(title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = Color.White, style = MaterialTheme.typography.bodyLarge)
        Text(subtitle, color = Color(0xFFAAAAAA), style = MaterialTheme.typography.bodyMedium)
    }
}
