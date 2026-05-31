package com.manimstudio.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manimstudio.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        color = OnBackground,
                        fontWeight = FontWeight.Normal,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = OnBackground,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    scrolledContainerColor = Background,
                ),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            // Account section
            item { SettingsSectionHeader("Account") }
            item {
                SettingsGroup {
                    ListItem(
                        headlineContent = {
                            Text("Abdulfatai", style = MaterialTheme.typography.bodyLarge,
                                color = OnBackground)
                        },
                        supportingContent = {
                            Text("Manim Studio PRO", style = MaterialTheme.typography.bodySmall,
                                color = Primary)
                        },
                        leadingContent = {
                            Box(
                                modifier = Modifier.size(40.dp).background(Primary, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("A", color = OnPrimary,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold)
                            }
                        },
                        trailingContent = {
                            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                null, tint = OnSurfaceDim)
                        },
                        modifier = Modifier.clickable { },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )
                }
            }

            item { SettingsSectionHeader("Appearance") }
            item {
                SettingsGroup {
                    ListItem(
                        headlineContent = { Text("Theme", color = OnBackground) },
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("System", color = OnSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium)
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                    null, tint = OnSurfaceDim)
                            }
                        },
                        modifier = Modifier.clickable { },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )
                }
            }

            item { SettingsSectionHeader("AI & Generation") }
            item {
                SettingsGroup {
                    ListItem(
                        headlineContent = { Text("Default Provider", color = OnBackground) },
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Auto", color = OnSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium)
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                    null, tint = OnSurfaceDim)
                            }
                        },
                        modifier = Modifier.clickable { },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )
                    HorizontalDivider(color = OutlineVariant,
                        modifier = Modifier.padding(horizontal = 16.dp))
                    ListItem(
                        headlineContent = { Text("Groq API Key", color = OnBackground) },
                        supportingContent = { Text("Free tier · No billing required",
                            color = OnSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall) },
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("••••••••", color = OnSurfaceDim,
                                    style = MaterialTheme.typography.bodyMedium)
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.Outlined.Edit, null,
                                    tint = OnSurfaceDim, modifier = Modifier.size(18.dp))
                            }
                        },
                        modifier = Modifier.clickable { },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )
                    HorizontalDivider(color = OutlineVariant,
                        modifier = Modifier.padding(horizontal = 16.dp))
                    ListItem(
                        headlineContent = { Text("Gemini API Key", color = OnBackground) },
                        supportingContent = { Text("Free tier available",
                            color = OnSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall) },
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Not set", color = OnSurfaceDim,
                                    style = MaterialTheme.typography.bodyMedium)
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.Outlined.Add, null,
                                    tint = Primary, modifier = Modifier.size(18.dp))
                            }
                        },
                        modifier = Modifier.clickable { },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )
                }
            }

            item { SettingsSectionHeader("Rendering") }
            item {
                SettingsGroup {
                    ListItem(
                        headlineContent = { Text("Default Quality", color = OnBackground) },
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("720p", color = OnSurfaceVariant)
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                    null, tint = OnSurfaceDim)
                            }
                        },
                        modifier = Modifier.clickable { },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )
                    HorizontalDivider(color = OutlineVariant,
                        modifier = Modifier.padding(horizontal = 16.dp))
                    ListItem(
                        headlineContent = { Text("Save Location", color = OnBackground) },
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Internal", color = OnSurfaceVariant)
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                    null, tint = OnSurfaceDim)
                            }
                        },
                        modifier = Modifier.clickable { },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )
                }
            }

            item { SettingsSectionHeader("Engine") }
            item {
                SettingsGroup {
                    ListItem(
                        headlineContent = { Text("Engine Status", color = OnBackground) },
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp)
                                    .background(Success, CircleShape))
                                Spacer(Modifier.width(6.dp))
                                Text("Ready", color = Success,
                                    style = MaterialTheme.typography.bodyMedium)
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )
                    HorizontalDivider(color = OutlineVariant,
                        modifier = Modifier.padding(horizontal = 16.dp))
                    ListItem(
                        headlineContent = {
                            Text("Reinstall Engine", color = Error,
                                style = MaterialTheme.typography.bodyLarge)
                        },
                        supportingContent = {
                            Text("Re-downloads and reinstalls Alpine Linux + Manim",
                                color = OnSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall)
                        },
                        modifier = Modifier.clickable { },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )
                }
            }

            item { SettingsSectionHeader("About") }
            item {
                SettingsGroup {
                    ListItem(
                        headlineContent = { Text("Version", color = OnBackground) },
                        trailingContent = {
                            Text("1.0.0-alpha", color = OnSurfaceDim,
                                style = MaterialTheme.typography.bodyMedium)
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )
                    HorizontalDivider(color = OutlineVariant,
                        modifier = Modifier.padding(horizontal = 16.dp))
                    ListItem(
                        headlineContent = { Text("GitHub", color = OnBackground) },
                        trailingContent = {
                            Icon(Icons.Outlined.OpenInNew, null,
                                tint = OnSurfaceDim, modifier = Modifier.size(18.dp))
                        },
                        modifier = Modifier.clickable { /* open github */ },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = Primary,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(start = 28.dp, top = 24.dp, bottom = 8.dp),
    )
}

@Composable
fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Surface),
        content = content,
    )
}
