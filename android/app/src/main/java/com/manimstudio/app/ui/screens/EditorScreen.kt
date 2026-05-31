package com.manimstudio.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manimstudio.app.ui.components.PythonSyntaxHighlighter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    initialCode: String = "",
    onBackClick: () -> Unit = {},
    onRenderClick: (String, String) -> Unit = { _, _ -> }
) {
    var code by remember { mutableStateOf(initialCode) }
    var selectedQuality by remember { mutableStateOf("720p") }
    var selectedFormat by remember { mutableStateOf("mp4") }
    
    val highlighter = remember { PythonSyntaxHighlighter() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Animation Code", color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Handle copy */ }) {
                        Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy Code", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
               .fillMaxSize()
               .padding(padding)
        ) {
            // THE CODE CANVAS (AMOLED BLACK background)
            Box(
                modifier = Modifier
                   .fillMaxWidth()
                   .weight(1f)
                   .background(Color.Black)
                   .verticalScroll(rememberScrollState())
                   .padding(16.dp)
            ) {
                BasicTextField(
                    value = code,
                    onValueChange = { code = it },
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    visualTransformation = highlighter, // Dynamic Syntax Coloring!
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxSize()
                )
            }

            // BOTTOM SHEET RENDER DRAWER
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Render Settings", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.titleMedium)

                    // Quality Selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Quality", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("480p", "720p", "1080p").forEach { q ->
                                FilterChip(
                                    selected = selectedQuality == q,
                                    onClick = { selectedQuality = q },
                                    label = { Text(q) }
                                )
                            }
                        }
                    }

                    // Format Selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Format", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("mp4", "gif").forEach { f ->
                                FilterChip(
                                    selected = selectedFormat == f,
                                    onClick = { selectedFormat = f },
                                    label = { Text(f.uppercase()) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // RENDER BUTTON
                    Button(
                        onClick = { onRenderClick(code, selectedQuality) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Outlined.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Render Animation",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
