package com.manimstudio.app.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.manimstudio.app.ui.theme.AccentOrange
import com.manimstudio.app.viewmodel.StudioPhase

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
        // Align to bottom so the icons stay anchored as the text box grows tall
        verticalAlignment = Alignment.Bottom, 
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp) // Increased
            .padding(bottom = 24.dp) // Lifted up
            .navigationBarsPadding()
            .animateContentSize() // MAGIC: Smoothly animates height changes
            .background(Color(0xFF1E1F22), RoundedCornerShape(32.dp)) // Updated shape
            .padding(all = 12.dp) // Increased internal padding
    ) {
        // The '+' Icon for templates/assets
        IconButton(onClick = onPlusClick) {
            Icon(Icons.Rounded.Add, contentDescription = "Add", tint = Color(0xFFAAAAAA))
        }

        // The actual text input
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp, vertical = 14.dp), // Internal padding for text
            contentAlignment = Alignment.CenterStart
        ) {
            if (text.isEmpty()) {
                Text(
                    "Describe your animation...", 
                    color = Color(0xFFAAAAAA),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            BasicTextField(
                value = text,
                onValueChange = onTextChanged,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                cursorBrush = SolidColor(Color(0xFFFF8C00)), // Orange Cursor
                maxLines = 6, // Allows it to expand up to 6 lines before scrolling internally
                modifier = Modifier.fillMaxWidth(),
                enabled = !isRendering
            )
        }

        // The Action Circle (Bottom-aligned due to Row alignment)
        Box(
            modifier = Modifier
                .padding(bottom = 4.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isRendering) Color.Transparent else Color(0xFF3E1E04)) // Deep Orange/Brown surface
                .clickable(enabled = !isRendering || phase == StudioPhase.RENDERING) {
                    if (isRendering) {
                        onStop()
                    } else if (text.isNotEmpty()) {
                        onSend()
                    } else {
                        // Handle voice
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (isRendering) {
                Icon(Icons.Rounded.Stop, contentDescription = "Stop", tint = AccentOrange, modifier = Modifier.size(20.dp))
            } else if (text.isEmpty()) {
                Icon(Icons.Rounded.Mic, contentDescription = "Mic", tint = Color(0xFFFF8C00), modifier = Modifier.size(20.dp))
            } else {
                Icon(Icons.Rounded.Send, contentDescription = "Send", tint = Color(0xFFFF8C00), modifier = Modifier.size(20.dp))
            }
        }
    }
}
