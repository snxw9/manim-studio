# Android UI/UX Refresh Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement a refined, "Gemini-style" UI refresh for the Android application, including a floating input pill, a cleaner sidebar, a new Settings screen, and updated navigation.

**Architecture:** Component-based UI updates using Jetpack Compose. Navigation is managed via a centralized `NavHost` in `MainActivity`.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Navigation Compose.

---

### Task 1: Refined Input Pill

**Files:**
- Modify: `android/app/src/main/java/com/manimstudio/app/ui/components/FloatingPromptInput.kt`

- [ ] **Step 1: Update `FloatingPromptInput` signature and layout**
Modify the `Row` modifier to increase padding and lift it up. Update parameters to match the new `StudioScreen` usage while maintaining compatibility.

```kotlin
// ... existing imports ...

@Composable
fun FloatingPromptInput(
    text: String = "",
    phase: StudioPhase = StudioPhase.IDLE,
    onTextChanged: (String) -> Unit = {},
    onSend: () -> Unit = {},
    onStop: () -> Unit = {},
    onPlusClick: () -> Unit = {}, // Renamed from onAddClick and added default
    modifier: Modifier = Modifier
) {
    val isRendering = phase == StudioPhase.RENDERING

    Row(
        verticalAlignment = Alignment.Bottom, 
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp) // Increased
            .padding(bottom = 24.dp) // Lifted up
            .navigationBarsPadding()
            .animateContentSize() 
            .background(Color(0xFF1E1F22), RoundedCornerShape(32.dp)) // Updated shape
            .padding(all = 12.dp) // Increased internal padding
    ) {
        // The '+' Icon for templates/assets
        IconButton(onClick = onPlusClick) {
            Icon(Icons.Rounded.Add, contentDescription = "Add", tint = Color(0xFFAAAAAA))
        }

        // ... rest of the content (Box and Action Circle) remains the same ...
        // Ensure onAddClick is replaced with onPlusClick in the IconButton
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add android/app/src/main/java/com/manimstudio/app/ui/components/FloatingPromptInput.kt
git commit -m "style: refine FloatingPromptInput padding and shape"
```

### Task 2: Create Settings Screen

**Files:**
- Create: `android/app/src/main/java/com/manimstudio/app/ui/screens/SettingsScreen.kt`

- [ ] **Step 1: Implement `SettingsScreen`**
Create the new file with the card-based layout and AMOLED black theme.

```kotlin
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
```

- [ ] **Step 2: Commit**

```bash
git add android/app/src/main/java/com/manimstudio/app/ui/screens/SettingsScreen.kt
git commit -m "feat: implement Gemini-style SettingsScreen"
```

### Task 3: Replace Studio Screen

**Files:**
- Modify: `android/app/src/main/java/com/manimstudio/app/ui/screens/StudioScreen.kt`

- [ ] **Step 1: Replace `StudioScreen.kt` contents**
Delete current content and replace with the updated, cleaner version provided by the user. Ensure imports for `StudioTopAppBar` and `FloatingPromptInput` are correct.

```kotlin
package com.manimstudio.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.manimstudio.app.ui.components.StudioTopAppBar
import com.manimstudio.app.ui.components.FloatingPromptInput

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioScreen(
    onNavigateToSettings: () -> Unit = {},
    onNavigateToGallery: () -> Unit = {},
    onNavigateToTemplates: () -> Unit = {}
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowRadius by infiniteTransition.animateFloat(
        initialValue = 800f, targetValue = 1600f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 3500, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "glowRadius"
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.Black,
                drawerShape = RoundedCornerShape(topEnd = 0.dp, bottomEnd = 0.dp),
                modifier = Modifier.width(320.dp)
            ) {
                SidebarContent(
                    onNavigateToSettings = { scope.launch { drawerState.close() }; onNavigateToSettings() },
                    onNavigateToGallery = { scope.launch { drawerState.close() }; onNavigateToGallery() },
                    onNavigateToTemplates = { scope.launch { drawerState.close() }; onNavigateToTemplates() }
                )
            }
        }
    ) {
        Scaffold(
            topBar = { StudioTopAppBar(onMenuClick = { scope.launch { drawerState.open() } }) },
            bottomBar = { FloatingPromptInput(onPlusClick = { showBottomSheet = true }) },
            containerColor = Color.Black
        ) { innerPadding ->
            Box(
                modifier = Modifier.fillMaxSize()
                    .background(Brush.radialGradient(colors = listOf(Color(0xFF3E1E04), Color.Black), radius = glowRadius, center = Offset(x = Float.POSITIVE_INFINITY / 2, y = Float.POSITIVE_INFINITY)))
                    .padding(innerPadding)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = Color(0xFFFF8C00), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Hi Abdulfatai, what's on your mind?", style = MaterialTheme.typography.headlineMedium, color = Color.White, textAlign = TextAlign.Center)
                }
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(onDismissRequest = { showBottomSheet = false }, sheetState = sheetState, containerColor = Color.Black) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp).height(200.dp)) {
                Text("Templates & Assets", color = Color.White, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
fun SidebarContent(
    onNavigateToSettings: () -> Unit,
    onNavigateToGallery: () -> Unit,
    onNavigateToTemplates: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Manim Studio", color = Color.White, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 24.dp, start = 12.dp))
        
        SidebarItem(Icons.Rounded.Edit, "New chat") {}
        SidebarItem(Icons.Rounded.Search, "Search chats") {}
        SidebarItem(Icons.Rounded.VideoLibrary, "Gallery", onClick = onNavigateToGallery)
        SidebarItem(Icons.Rounded.GridView, "Templates", onClick = onNavigateToTemplates)
        
        Spacer(modifier = Modifier.height(32.dp))
        Text("Recent", color = Color(0xFFAAAAAA), style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(start = 12.dp, bottom = 8.dp))
        
        SidebarRecentItem("No-code manim animation buil...")
        SidebarRecentItem("Naruto Fandom Character Criti...")
        SidebarRecentItem("Multi-platform audio processin...")
        SidebarRecentItem("GUI for Manim animations")
        
        Spacer(modifier = Modifier.weight(1f))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable { onNavigateToSettings() }.padding(12.dp)
        ) {
            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFFF8C00)), contentAlignment = Alignment.Center) {
                Text("A", color = Color.Black, fontWeight = FontWeight.Bold)
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text("Abdulfatai", color = Color.White, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("PRO", color = Color(0xFFAAAAAA), style = MaterialTheme.typography.labelSmall)
            }
            Icon(Icons.Rounded.Settings, contentDescription = "Settings", tint = Color(0xFFAAAAAA))
        }
    }
}

@Composable
fun SidebarItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(50)).clickable { onClick() }.padding(horizontal = 12.dp, vertical = 12.dp)) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, color = Color.White, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun SidebarRecentItem(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(50)).clickable {}.padding(horizontal = 12.dp, vertical = 12.dp)) {
        Text(text, color = Color.White, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add android/app/src/main/java/com/manimstudio/app/ui/screens/StudioScreen.kt
git commit -m "feat: update StudioScreen with cleaner sidebar and navigation"
```

### Task 4: Update Navigation in MainActivity

**Files:**
- Modify: `android/app/src/main/java/com/manimstudio/app/MainActivity.kt`

- [ ] **Step 1: Update `NavHost`**
Add the new routes for Settings, Gallery, and Templates. Pass navigation callbacks to `StudioScreen`.

```kotlin
// ... existing imports ...
import com.manimstudio.app.ui.screens.SettingsScreen
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color

// ... inside setContent ...
ManimStudioTheme { // Keep original theme name
    Surface(modifier = Modifier.fillMaxSize()) {
        
        val navController = rememberNavController()
        val setupState by viewModel.state.collectAsState()

        NavHost(navController = navController, startDestination = startScreen) {
            
            // ROUTE 1: Setup
            composable("setup") {
                SetupScreen(
                    state = setupState,
                    onStartSetup = { viewModel.startInstallation() },
                    onRetry = { viewModel.retrySetup() },
                    onTestRender = {
                        navController.navigate("studio") {
                            popUpTo("setup") { inclusive = true }
                        }
                    },
                    onOpenSettings = {}
                )
            }

            // ROUTE 2: Main Studio
            composable("studio") {
                StudioScreen(
                    onNavigateToSettings = { navController.navigate("settings") },
                    onNavigateToGallery = { navController.navigate("gallery") },
                    onNavigateToTemplates = { navController.navigate("templates") }
                )
            }

            // ROUTE 3: Settings Screen
            composable("settings") {
                SettingsScreen(
                    onBackClick = { navController.popBackStack() } 
                )
            }

            // ROUTE 4: Gallery Placeholder
            composable("gallery") {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black))
            }

            // ROUTE 5: Templates Placeholder
            composable("templates") {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black))
            }
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add android/app/src/main/java/com/manimstudio/app/MainActivity.kt
git commit -m "feat: add navigation routes for Settings, Gallery, and Templates"
```

---
