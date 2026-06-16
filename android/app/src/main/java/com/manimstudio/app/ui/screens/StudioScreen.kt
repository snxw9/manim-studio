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
import com.manimstudio.app.viewmodel.StudioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioScreen(
    viewModel: StudioViewModel,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToGallery: () -> Unit = {},
    onNavigateToTemplates: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
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
            bottomBar = { 
                FloatingPromptInput(
                    text = uiState.inputText,
                    phase = uiState.phase,
                    onTextChanged = { viewModel.onInputChanged(it) },
                    onSend = { viewModel.onSendPrompt() },
                    onStop = { viewModel.onStopRender() },
                    onPlusClick = { showBottomSheet = true }
                ) 
            },
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
                    Text("Hi ${uiState.userName}, what's on your mind?", style = MaterialTheme.typography.headlineMedium, color = Color.White, textAlign = TextAlign.Center)
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
