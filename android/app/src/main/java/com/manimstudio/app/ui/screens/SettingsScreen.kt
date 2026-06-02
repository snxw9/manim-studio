package com.manimstudio.app.ui.screens

import android.os.Build
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manimstudio.app.data.models.RenderQuality
import com.manimstudio.app.data.models.FontOption
import com.manimstudio.app.ui.components.animations.GlobalGradientBackground
import com.manimstudio.app.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    onBack: () -> Unit,
    onNavigateToTheme: () -> Unit,
) {
    val settings by settingsViewModel.settings.collectAsState()

    var showQualityDialog by remember { mutableStateOf(false) }
    var showProviderDialog by remember { mutableStateOf(false) }
    var showGroqDialog by remember { mutableStateOf(false) }
    var showGeminiDialog by remember { mutableStateOf(false) }
    var showNameDialog by remember { mutableStateOf(false) }
    var showReinstallDialog by remember { mutableStateOf(false) }
    var showSaveLocationDialog by remember { mutableStateOf(false) }
    var showFontDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Normal,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                ),
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            GlobalGradientBackground(intensity = 0.35f, animate = true)
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
                            Text(settings.userName.ifBlank { "User" }, style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground)
                        },
                        leadingContent = {
                            Box(
                                modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(settings.userName.firstOrNull()?.toString()?.uppercase() ?: "U", color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold)
                            }
                        },
                        trailingContent = {
                            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        },
                        modifier = Modifier.clickable { showNameDialog = true },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )
                }
            }

            item { SettingsSectionHeader("Appearance") }
            item {
                SettingsGroup {
                    ListItem(
                        headlineContent = { Text("Theme", color = MaterialTheme.colorScheme.onBackground) },
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(settings.themeSettings.themeMode.name.lowercase().replaceFirstChar { it.uppercase() }, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium)
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                    null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                            }
                        },
                        modifier = Modifier.clickable { onNavigateToTheme() },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(horizontal = 16.dp))
                    ListItem(
                        headlineContent = { Text("Font", color = MaterialTheme.colorScheme.onSurface) },
                        supportingContent = {
                            Text("App interface font",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    if (settings.themeSettings.fontOption == FontOption.INTER) "Inter"
                                    else "System default",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    modifier = Modifier.size(18.dp))
                            }
                        },
                        modifier = Modifier.clickable { showFontDialog = true },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )
                }
            }

            item { SettingsSectionHeader("AI & Generation") }
            item {
                SettingsGroup {
                    ListItem(
                        headlineContent = { Text("Default Provider", color = MaterialTheme.colorScheme.onBackground) },
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(settings.apiProvider.replaceFirstChar { it.uppercase() }, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium)
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                    null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                            }
                        },
                        modifier = Modifier.clickable { showProviderDialog = true },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(horizontal = 16.dp))
                    ListItem(
                        headlineContent = { Text("Groq API Key", color = MaterialTheme.colorScheme.onBackground) },
                        supportingContent = { Text("Get a free key at console.groq.com",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall) },
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(if (settings.groqApiKey.isEmpty()) "Not set" else "••••••••", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    style = MaterialTheme.typography.bodyMedium)
                                Spacer(Modifier.width(4.dp))
                                Icon(if (settings.groqApiKey.isEmpty()) Icons.Outlined.Add else Icons.Outlined.Edit, null,
                                    tint = if (settings.groqApiKey.isEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                            }
                        },
                        modifier = Modifier.clickable { showGroqDialog = true },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(horizontal = 16.dp))
                    ListItem(
                        headlineContent = { Text("Gemini API Key", color = MaterialTheme.colorScheme.onBackground) },
                        supportingContent = { Text("Get your key from Google AI Studio",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall) },
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(if (settings.geminiApiKey.isEmpty()) "Not set" else "••••••••", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    style = MaterialTheme.typography.bodyMedium)
                                Spacer(Modifier.width(4.dp))
                                Icon(if (settings.geminiApiKey.isEmpty()) Icons.Outlined.Add else Icons.Outlined.Edit, null,
                                    tint = if (settings.geminiApiKey.isEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                            }
                        },
                        modifier = Modifier.clickable { showGeminiDialog = true },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )
                }
            }

            item { SettingsSectionHeader("Rendering") }
            item {
                SettingsGroup {
                    ListItem(
                        headlineContent = { Text("Default Quality", color = MaterialTheme.colorScheme.onBackground) },
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(settings.renderQuality.label, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                    null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                            }
                        },
                        modifier = Modifier.clickable { showQualityDialog = true },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(horizontal = 16.dp))
                    ListItem(
                        headlineContent = { Text("Save Location", color = MaterialTheme.colorScheme.onBackground) },
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Internal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                    null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                            }
                        },
                        modifier = Modifier.clickable { showSaveLocationDialog = true },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )
                }
            }

            item { SettingsSectionHeader("Engine") }
            item {
                SettingsGroup {
                    ListItem(
                        headlineContent = { Text("Engine Status", color = MaterialTheme.colorScheme.onBackground) },
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp)
                                    .background(Color(0xFF4CAF50), CircleShape))
                                Spacer(Modifier.width(6.dp))
                                Text("Ready", color = Color(0xFF4CAF50),
                                    style = MaterialTheme.typography.bodyMedium)
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(horizontal = 16.dp))
                    ListItem(
                        headlineContent = {
                            Text("Reinstall Engine", color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge)
                        },
                        supportingContent = {
                            Text("Re-downloads and reinstalls Alpine Linux + Manim",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall)
                        },
                        modifier = Modifier.clickable { showReinstallDialog = true },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )
                }
            }
        }
    }
}

    // Dialogs
    if (showQualityDialog) {
        AlertDialog(
            onDismissRequest = { showQualityDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Default Quality", color = MaterialTheme.colorScheme.onBackground) },
            text = {
                Column {
                    listOf(
                        RenderQuality.LOW to "480p (Fast)",
                        RenderQuality.MID to "720p (Balanced)",
                        RenderQuality.HIGH to "1080p (High quality)",
                    ).forEach { (quality, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    settingsViewModel.updateQuality(quality)
                                    showQualityDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = settings.renderQuality == quality,
                                onClick = {
                                    settingsViewModel.updateQuality(quality)
                                    showQualityDialog = false
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.primary
                                ),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(label, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {},
        )
    }

    if (showProviderDialog) {
        AlertDialog(
            onDismissRequest = { showProviderDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Default Provider", color = MaterialTheme.colorScheme.onBackground) },
            text = {
                Column {
                    listOf("auto", "groq", "gemini", "openai").forEach { provider ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    settingsViewModel.updateProvider(provider)
                                    showProviderDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = settings.apiProvider == provider,
                                onClick = {
                                    settingsViewModel.updateProvider(provider)
                                    showProviderDialog = false
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.primary
                                ),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(provider.replaceFirstChar { it.uppercase() }, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {},
        )
    }

    if (showGroqDialog) {
        var keyInput by remember { mutableStateOf(settings.groqApiKey) }
        AlertDialog(
            onDismissRequest = { showGroqDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Groq API Key", color = MaterialTheme.colorScheme.onBackground) },
            text = {
                Column {
                    Text(
                        "Get a free key at console.groq.com",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        label = { Text("API Key") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            cursorColor = MaterialTheme.colorScheme.primary,
                        ),
                        trailingIcon = {
                            IconButton(onClick = { /* toggle visibility */ }) {
                                Icon(Icons.Outlined.Visibility, null)
                            }
                        },
                    )
                    if (keyInput.isNotEmpty()) {
                        Text(
                            "Key will be stored securely on your device",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    settingsViewModel.updateGroqKey(keyInput)
                    showGroqDialog = false
                }) {
                    Text("Save", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGroqDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
        )
    }

    if (showGeminiDialog) {
        var keyInput by remember { mutableStateOf(settings.geminiApiKey) }
        AlertDialog(
            onDismissRequest = { showGeminiDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Gemini API Key", color = MaterialTheme.colorScheme.onBackground) },
            text = {
                Column {
                    Text(
                        "Get your API key from Google AI Studio",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        label = { Text("API Key") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            cursorColor = MaterialTheme.colorScheme.primary,
                        ),
                        trailingIcon = {
                            IconButton(onClick = { /* toggle visibility */ }) {
                                Icon(Icons.Outlined.Visibility, null)
                            }
                        },
                    )
                    if (keyInput.isNotEmpty()) {
                        Text(
                            "Key will be stored securely on your device",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    settingsViewModel.updateGeminiKey(keyInput)
                    showGeminiDialog = false
                }) {
                    Text("Save", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGeminiDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
        )
    }

    if (showNameDialog) {
        var nameInput by remember { mutableStateOf(settings.userName) }
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Your name", color = MaterialTheme.colorScheme.onBackground) },
            text = {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Name or nickname") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    settingsViewModel.updateUserName(nameInput)
                    showNameDialog = false
                }) { Text("Save", color = MaterialTheme.colorScheme.primary) }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    if (showReinstallDialog) {
        AlertDialog(
            onDismissRequest = { showReinstallDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Reinstall Engine?", color = MaterialTheme.colorScheme.error) },
            text = {
                Text(
                    "This will delete the Alpine Linux environment and " +
                    "re-download everything (~300MB). This takes 5-10 minutes.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            confirmButton = {
                TextButton(onClick = { 
                    // Trigger reinstall logic
                    showReinstallDialog = false
                }) {
                    Text("Reinstall", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReinstallDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
        )
    }

    if (showSaveLocationDialog) {
        AlertDialog(
            onDismissRequest = { showSaveLocationDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Save Location", color = MaterialTheme.colorScheme.onBackground) },
            text = {
                Column {
                    listOf("Internal", "External Movies folder").forEach { location ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showSaveLocationDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = (location == "Internal"),
                                onClick = {
                                    showSaveLocationDialog = false
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.primary
                                ),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(location, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {},
        )
    }

    if (showFontDialog) {
        AlertDialog(
            onDismissRequest = { showFontDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Font", color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Column {
                    listOf(
                        FontOption.INTER to "Inter" to "Clean, modern sans-serif",
                        FontOption.SYSTEM to "System default" to "Uses your device font",
                    ).forEach { (pair, desc) ->
                        val (option, label) = pair
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    settingsViewModel.updateFontOption(option)
                                    showFontDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = settings.themeSettings.fontOption == option,
                                onClick = {
                                    settingsViewModel.updateFontOption(option)
                                    showFontDialog = false
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.primary,
                                ),
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(label, color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodyLarge)
                                Text(desc, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
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
            .background(MaterialTheme.colorScheme.surface),
        content = content,
    )
}
