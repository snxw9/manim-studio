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
