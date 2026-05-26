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
