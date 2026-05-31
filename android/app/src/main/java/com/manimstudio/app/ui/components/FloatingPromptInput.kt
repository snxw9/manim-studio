package com.manimstudio.app.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.manimstudio.app.ui.theme.*

@Composable
fun FloatingPromptInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
    onTemplateClick: () -> Unit,
    isRendering: Boolean,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.Bottom, 
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .background(SurfaceVariant, RoundedCornerShape(32.dp))
            .padding(all = 12.dp)
    ) {
        // The '+' Icon for templates/assets
        IconButton(onClick = onTemplateClick) {
            Icon(
                imageVector = Icons.Outlined.Add, 
                contentDescription = "Templates", 
                tint = OnSurfaceVariant
            )
        }

        // The actual text input
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp, vertical = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (text.isEmpty()) {
                Text(
                    "Describe your animation...", 
                    color = OnSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            BasicTextField(
                value = text,
                onValueChange = onTextChange,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = OnBackground),
                cursorBrush = SolidColor(Primary),
                maxLines = 6,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isRendering
            )
        }

        // The Action Circle
        Box(
            modifier = Modifier
                .padding(bottom = 4.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isRendering) Color.Transparent else Primary.copy(alpha = 0.1f))
                .clickable(enabled = !isRendering || isRendering) {
                    if (isRendering) {
                        onStop()
                    } else if (text.isNotEmpty()) {
                        onSend()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (isRendering) {
                Icon(
                    imageVector = Icons.Outlined.Stop, 
                    contentDescription = "Stop", 
                    tint = Primary, 
                    modifier = Modifier.size(20.dp)
                )
            } else if (text.isEmpty()) {
                Icon(
                    imageVector = Icons.Outlined.Mic, 
                    contentDescription = "Mic", 
                    tint = Primary, 
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Send, 
                    contentDescription = "Send", 
                    tint = Primary, 
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
