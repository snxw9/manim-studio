package com.manimstudio.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

            // SECTION: Profile
            SettingsSectionTitle("Account")
            SettingsCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* Handle Profile Click */ }
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFFF8C00)), contentAlignment = Alignment.Center) {       
                        Text("A", color = Color.Black, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    }
                    Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                        Text("Abdulfatai", color = Color.White, style = MaterialTheme.typography.titleMedium)
                        Text("Manim Studio PRO", color = Color(0xFFFF8C00), style = MaterialTheme.typography.labelMedium)
                    }
                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Color(0xFFAAAAAA))
                }
            }

            // SECTION: AI & Engine
            SettingsSectionTitle("AI Engine & Processing")
            SettingsCard {
                SettingsListItem("Default Provider", "Gemini 1.5 Pro") { /* Show Dropdown */ }
                HorizontalDivider(color = Color(0xFF333333))
                SettingsListItem("Offline Fallback", "Local PRoot Engine") { /* Show Dropdown */ }
                HorizontalDivider(color = Color(0xFF333333))
                SettingsListItem("Thinking Level", "Advanced (Complex Math)") { /* Show Dropdown */ }
            }

            // SECTION: Render Quality
            SettingsSectionTitle("Render Quality")
            SettingsCard {
                SettingsListItem("Resolution", "1080p") { /* Show Options */ }
                HorizontalDivider(color = Color(0xFF333333))
                SettingsListItem("Frame Rate", "60 FPS") { /* Show Options */ }
                HorizontalDivider(color = Color(0xFF333333))
                SettingsListItem("Aspect Ratio", "16:9 (Landscape)") { /* Show Options */ }
            }

            // SECTION: Storage
            SettingsSectionTitle("Downloads & Storage")
            SettingsCard {
                SettingsListItem("Save Location", "External (Movies/Manim)") { /* Show Toggle */ }
                HorizontalDivider(color = Color(0xFF333333))
                SettingsListItem("Clear Render Cache", "1.2 GB used") { /* Trigger Clear */ }
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
fun SettingsListItem(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = Color.White, style = MaterialTheme.typography.bodyLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(subtitle, color = Color(0xFFAAAAAA), style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Color(0xFFAAAAAA))
        }
    }
}
