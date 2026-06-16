package com.manimstudio.app.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manimstudio.app.data.models.StudioPhase

@Composable
fun FloatingPromptInput(
    text: String = "",
    phase: StudioPhase = StudioPhase.IDLE,
    onTextChanged: (String) -> Unit = {},
    onSend: () -> Unit = {},
    onStop: () -> Unit = {},
    onPlusClick: () -> Unit = {}, // Renamed from onAddClick and added default
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
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
        
        // NOTE: The prompt code was partial ("... rest of the content remains the same").
        // I will implement the rest based on the original file's logic but adapted to the new style.
        
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            androidx.compose.foundation.text.BasicTextField(
                value = text,
                onValueChange = onTextChanged,
                modifier = Modifier.fillMaxWidth(),
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = Color.White,
                    fontSize = 17.sp,
                    lineHeight = 24.sp
                ),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFF8C00)),
                decorationBox = { inner ->
                    if (text.isEmpty()) {
                        Text(
                            "Ask anything...",
                            color = Color(0xFFAAAAAA),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    inner()
                }
            )
        }

        // Action Circle (Send or Stop)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(if (text.isNotBlank() || isRendering) Color.White else Color(0xFF333333))
                .clickable {
                    if (isRendering) onStop()
                    else if (text.isNotBlank()) onSend()
                }
        ) {
            Icon(
                imageVector = if (isRendering) Icons.Rounded.Stop
                             else Icons.Rounded.ArrowUpward,
                contentDescription = if (isRendering) "Stop" else "Send",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
