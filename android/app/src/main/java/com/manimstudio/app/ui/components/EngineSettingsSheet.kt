package com.manimstudio.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manimstudio.app.data.models.RenderQuality

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EngineSettingsSheet(
    currentQuality: RenderQuality,
    onQualitySelected: (RenderQuality) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 40.dp)
                .padding(horizontal = 20.dp)
        ) {
            Text(
                "Engine Settings",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Text("Rendering Quality", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(12.dp))
            
            RenderQuality.entries.forEach { quality ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(quality.label, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                        Text(quality.estimatedSeconds, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                    RadioButton(
                        selected = quality == currentQuality,
                        onClick = { onQualitySelected(quality) },
                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("AI Intelligence", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(12.dp))
            
            // Mocking API providers for now
            listOf("Auto (Recommended)", "Groq (Fastest)", "Gemini 1.5 Pro", "OpenAI GPT-4o").forEachIndexed { index, provider ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(provider, color = if (index == 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
                    if (index == 0) {
                        RadioButton(selected = true, onClick = {}, colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary))
                    } else {
                        RadioButton(selected = false, onClick = {}, colors = RadioButtonDefaults.colors(unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant))
                    }
                }
            }
        }
    }
}
