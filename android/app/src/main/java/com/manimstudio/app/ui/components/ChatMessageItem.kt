package com.manimstudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manimstudio.app.data.models.ChatMessage
import com.manimstudio.app.data.models.MessageType
import com.manimstudio.app.ui.theme.*
import java.io.File

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    onRetry: () -> Unit = {},
    onExport: (File) -> Unit = {},
    modifier: Modifier = Modifier
) {
    when (message.type) {
        MessageType.USER_PROMPT -> UserMessage(message.content, modifier)
        MessageType.SYSTEM_STATUS -> SystemMessage(message.content, modifier)
        MessageType.VIDEO_RESULT -> VideoMessage(message, onExport, modifier)
        MessageType.ERROR -> ErrorMessage(message.content, onRetry, modifier)
        MessageType.CODE_PREVIEW -> CodePreviewMessage(message.content, modifier)
    }
}

@Composable
private fun UserMessage(content: String, modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = content, 
                color = MaterialTheme.colorScheme.onPrimary, 
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun SystemMessage(content: String, modifier: Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.AutoAwesome,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = content,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = FontStyle.Italic
        )
    }
}

@Composable
private fun VideoMessage(
    message: ChatMessage,
    onExport: (File) -> Unit,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        message.videoFile?.let { file ->
            VideoPlayerBubble(
                videoFile = file,
                onExport = { onExport(file) }
            )
            
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${file.name} • ${message.renderTimeMs?.let { "${it/1000}s" } ?: "unknown"}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun ErrorMessage(content: String, onRetry: () -> Unit, modifier: Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Error", 
                color = MaterialTheme.colorScheme.onBackground, 
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = content, 
            color = MaterialTheme.colorScheme.onSurfaceVariant, 
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Try Again", color = MaterialTheme.colorScheme.onError)
        }
    }
}

@Composable
private fun CodePreviewMessage(code: String, modifier: Modifier) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Generated Python Code", 
                color = MaterialTheme.colorScheme.onSurface, 
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = if (expanded) "Collapse" else "Expand", 
                color = MaterialTheme.colorScheme.primary, 
                style = MaterialTheme.typography.labelLarge
            )
        }
        
        if (expanded) {
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = code, 
                    color = MaterialTheme.colorScheme.onSurface, 
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
