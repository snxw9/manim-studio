package com.manimstudio.app.ui.screens

import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import com.manimstudio.app.ui.components.animations.GlobalGradientBackground
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.manimstudio.app.data.models.ThemeColor
import com.manimstudio.app.data.models.ThemeMode
import com.manimstudio.app.data.models.ThemeSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
    themeSettings: ThemeSettings,
    onUpdate: (ThemeSettings) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Theme and Color") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
                modifier = Modifier.statusBarsPadding(),
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            GlobalGradientBackground(intensity = 0.35f, animate = true)
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
            // Material You toggle
            item {
                SettingsToggleRow(
                    title = "Material You",
                    description = "Uses your wallpaper colors throughout the app. " +
                        "Overrides Theme Color when enabled.",
                    checked = themeSettings.useMaterialYou,
                    onCheckedChange = { onUpdate(themeSettings.copy(useMaterialYou = it)) },
                    enabled = Build.VERSION.SDK_INT >= 31,
                    disabledHint = if (Build.VERSION.SDK_INT < 31)
                        "Requires Android 12 or higher" else null,
                )
            }

            // Theme Color — only interactive when Material You is OFF
            item {
                AnimatedContent(
                    targetState = themeSettings.useMaterialYou,
                    label = "colorSection",
                ) { materialYouOn ->
                    Column {
                        Text(
                            "Theme Color",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (materialYouOn)
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 16.dp),
                        )
                        Text(
                            "Change the color theme of the app",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(12.dp))
                        // Color swatches
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(ThemeColor.entries) { colorOption ->
                                ColorSwatch(
                                    themeColor = colorOption,
                                    selected = themeSettings.themeColor == colorOption,
                                    enabled = !materialYouOn,
                                    onClick = {
                                        if (!materialYouOn) {
                                            onUpdate(themeSettings.copy(themeColor = colorOption))
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }

            // Theme Mode
            item {
                Spacer(Modifier.height(24.dp))
                Text("Theme Mode", style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.entries.forEach { mode ->
                        val selected = themeSettings.themeMode == mode
                        FilterChip(
                            selected = selected,
                            onClick = { onUpdate(themeSettings.copy(themeMode = mode)) },
                            label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            leadingIcon = if (selected) {{
                                Icon(Icons.Outlined.Check, null,
                                    modifier = Modifier.size(16.dp))
                            }} else null,
                        )
                    }
                }
            }

            // Black Backgrounds
            item {
                Spacer(Modifier.height(24.dp))
                SettingsToggleRow(
                    title = "Black Backgrounds",
                    description = "Use pure black backgrounds in dark mode " +
                        "instead of dark grey.",
                    checked = themeSettings.pureBlackBackground,
                    onCheckedChange = { onUpdate(themeSettings.copy(pureBlackBackground = it)) },
                )
            }
        }
    }
}
}

@Composable
fun ColorSwatch(
    themeColor: ThemeColor,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "swatchScale",
    )
    Box(
        modifier = Modifier
            .size(44.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(CircleShape)
            .background(if (enabled) themeColor.color else themeColor.color.copy(alpha = 0.3f))
            .clickable(enabled = enabled) { onClick() }
            .border(
                width = if (selected) 3.dp else 0.dp,
                color = MaterialTheme.colorScheme.onBackground,
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(Icons.Outlined.Check, null,
                tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun SettingsToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    disabledHint: String? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(title,
                style = MaterialTheme.typography.titleMedium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            Spacer(Modifier.height(4.dp))
            Text(description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (!enabled && disabledHint != null) {
                Text(disabledHint,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp))
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}
