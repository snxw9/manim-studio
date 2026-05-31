package com.manimstudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manimstudio.app.data.TemplateRepository
import com.manimstudio.app.ui.theme.AccentOrange
import com.manimstudio.app.ui.theme.Surface
import com.manimstudio.app.ui.theme.White

@Composable
fun TemplateBottomSheetContent(
    onTemplateSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val repository = remember { TemplateRepository(context) }
    val templates = remember { repository.getTemplates() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(templates) { template ->
                ListItem(
                    headlineContent = { Text(template.name, color = White, fontWeight = FontWeight.Medium) },
                    supportingContent = { Text(template.description, color = Color.Gray) },
                    trailingContent = {
                        Box(
                            modifier = Modifier
                                .background(AccentOrange.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(template.category, color = AccentOrange, fontSize = 10.sp)
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable {
                        onTemplateSelected(template.id)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateBottomSheet(
    onTemplateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
    ) {
        Text(
            "Quick Templates",
            color = White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        )
        TemplateBottomSheetContent(onTemplateSelected = {
            onTemplateSelected(it)
            onDismiss()
        })
    }
}
