package com.manimstudio.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.manimstudio.app.data.models.SuggestionCard
import com.manimstudio.app.ui.theme.OnBackground
import com.manimstudio.app.ui.theme.OnSurfaceVariant
import com.manimstudio.app.ui.theme.SurfaceBright

@Composable
fun SuggestionChip(
    suggestion: SuggestionCard,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceBright),
        modifier = Modifier
            .width(180.dp)
            .height(96.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = suggestion.title,
                style = MaterialTheme.typography.titleSmall,
                color = OnBackground,
                maxLines = 2,
            )
            Text(
                text = suggestion.description,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
