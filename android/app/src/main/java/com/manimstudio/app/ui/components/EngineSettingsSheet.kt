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
import com.manimstudio.app.ui.theme.AccentOrange
import com.manimstudio.app.ui.theme.Surface
import com.manimstudio.app.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EngineSettingsSheet(
    currentQuality: RenderQuality,
    onQualitySelected: (RenderQuality) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 40.dp)
                .padding(horizontal = 20.dp)
        ) {
            Text(
                "Engine Settings",
                color = White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Text("Rendering Quality", color = White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(12.dp))
            
            RenderQuality.entries.forEach { quality ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(quality.label, color = White, fontSize = 16.sp)
                        Text(quality.estimatedSeconds, color = Color.Gray, fontSize = 12.sp)
                    }
                    RadioButton(
                        selected = quality == currentQuality,
                        onClick = { onQualitySelected(quality) },
                        colors = RadioButtonDefaults.colors(selectedColor = AccentOrange)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("AI Intelligence", color = White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(12.dp))
            
            // Mocking API providers for now
            listOf("Auto (Recommended)", "Groq (Fastest)", "Gemini 1.5 Pro", "OpenAI GPT-4o").forEachIndexed { index, provider ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(provider, color = if (index == 0) White else Color.Gray, fontSize = 16.sp)
                    if (index == 0) {
                        RadioButton(selected = true, onClick = {}, colors = RadioButtonDefaults.colors(selectedColor = AccentOrange))
                    } else {
                        RadioButton(selected = false, onClick = {}, colors = RadioButtonDefaults.colors(unselectedColor = Color.Gray))
                    }
                }
            }
        }
    }
}
