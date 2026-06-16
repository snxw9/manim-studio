# Android Interactive Screens Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement interactive Settings, Gallery, and Templates screens for the Android application, featuring grid layouts and modern UI patterns.

**Architecture:** Component-based UI using Jetpack Compose. Navigation is managed via a centralized `NavHost`. State-driven UI for the Gallery (empty vs. populated).

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Navigation Compose.

---

### Task 1: Interactive Settings & Profile

**Files:**
- Modify: `android/app/src/main/java/com/manimstudio/app/ui/screens/SettingsScreen.kt`

- [ ] **Step 1: Update `SettingsScreen` with Profile and Interactivity**
Replace the file content to add the Account section, chevrons, and clickable modifiers.

```kotlin
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
```

- [ ] **Step 2: Commit**

```bash
git add android/app/src/main/java/com/manimstudio/app/ui/screens/SettingsScreen.kt
git commit -m "style: make Settings items interactive and add profile section"
```

### Task 2: Implement Templates Screen

**Files:**
- Create: `android/app/src/main/java/com/manimstudio/app/ui/screens/TemplatesScreen.kt`

- [ ] **Step 1: Create `TemplatesScreen.kt`**
Implement the 2-column grid layout for animation templates.

```kotlin
package com.manimstudio.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(onBackClick: () -> Unit = {}) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Templates", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            val templates = listOf(
                "Pythagorean Theorem" to "Visual proof with labeled sides",
                "Sine & Cosine" to "Unit circle and wave generation",
                "Shape Morph" to "Geometric transformations",
                "Matrix Multiply" to "2x2 matrix multiplication steps",
                "Derivative" to "Tangent line and slope visualization",
                "Neural Network" to "Layer activation and data flow"
            )

            items(templates.size) { index ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1F22)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.aspectRatio(0.85f)
                ) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(16.dp)).background(Color(0xFF2A2A2A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Category, contentDescription = null, tint = Color(0xFFFF8C00), modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(templates[index].first, color = Color.White, style = MaterialTheme.typography.titleMedium)
                        Text(templates[index].second, color = Color(0xFFAAAAAA), style = MaterialTheme.typography.bodySmall, maxLines = 2)
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add android/app/src/main/java/com/manimstudio/app/ui/screens/TemplatesScreen.kt
git commit -m "feat: implement TemplatesScreen grid"
```

### Task 3: Implement Gallery Screen

**Files:**
- Create: `android/app/src/main/java/com/manimstudio/app/ui/screens/GalleryScreen.kt`

- [ ] **Step 1: Create `GalleryScreen.kt` with Hover Metadata**
Implement the grid, empty state, and the reveal-on-tap metadata overlay.

```kotlin
package com.manimstudio.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(onBackClick: () -> Unit = {}) {
    val mockVideos = listOf(
        VideoItem("TestScene_480p.mp4", "480p • 1.2MB • 5s"),
        VideoItem("CircleTransform.mp4", "1080p • 12MB • 8s"),
        VideoItem("VectorField.mp4", "720p • 8.4MB • 15s")
    )
    val hasVideos = mockVideos.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gallery", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        if (!hasVideos) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = Color(0xFF333333), modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("No animations yet", color = Color.White, style = MaterialTheme.typography.titleMedium)
                Text("Create your first one in the Studio", color = Color(0xFFAAAAAA), style = MaterialTheme.typography.bodySmall)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                items(mockVideos.size) { index ->
                    GalleryCard(mockVideos[index])
                }
            }
        }
    }
}

data class VideoItem(val name: String, val metadata: String)

@Composable
fun GalleryCard(video: VideoItem) {
    var showMetadata by remember { mutableStateOf(false) }

    Column(modifier = Modifier.clickable { showMetadata = !showMetadata }) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1E1F22)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.PlayCircle, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(40.dp))
            
            // Metadata Overlay (Bottom Reveal)
            if (showMetadata) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                                startY = 50f
                            )
                        ),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Text(
                        text = video.metadata,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(video.name, color = Color.White, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
        Text("Today", color = Color(0xFFAAAAAA), style = MaterialTheme.typography.bodySmall)
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add android/app/src/main/java/com/manimstudio/app/ui/screens/GalleryScreen.kt
git commit -m "feat: implement GalleryScreen with empty state and metadata overlay"
```

### Task 4: Update Navigation in MainActivity

**Files:**
- Modify: `android/app/src/main/java/com/manimstudio/app/MainActivity.kt`

- [ ] **Step 1: Replace placeholders with real screens**
Update the `NavHost` composables for `gallery` and `templates`.

```kotlin
// ... inside NavHost ...

            // ROUTE 4: Gallery
            composable("gallery") {
                GalleryScreen(onBackClick = { navController.popBackStack() })
            }

            // ROUTE 5: Templates
            composable("templates") {
                TemplatesScreen(onBackClick = { navController.popBackStack() })
            }
```

- [ ] **Step 2: Commit**

```bash
git add android/app/src/main/java/com/manimstudio/app/MainActivity.kt
git commit -m "feat: wire real Gallery and Templates screens in NavHost"
```

---
