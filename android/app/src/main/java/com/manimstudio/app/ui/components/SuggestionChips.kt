package com.manimstudio.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Suggestion(
    val title: String,
    val description: String,
    val prompt: String
)

@Composable
fun SuggestionChips(
    onSuggestionClick: (String) -> Unit
) {
    val suggestions = listOf(
        Suggestion(
            "Pythagorean Proof",
            "Animate squares on a right triangle",
            "Show a visual proof of the Pythagorean theorem with animated squares on each side of a right triangle, labeling each area as a^2, b^2, and c^2"
        ),
        Suggestion(
            "Sieve of Eratosthenes",
            "Visualize prime number selection",
            "Create a grid of numbers from 2 to 30, and show the Sieve of Eratosthenes algorithm crossing out composites step by step in red"
        ),
        Suggestion(
            "Sine Wave Generation",
            "Trace sin(x) from a unit circle",
            "Draw a rolling circle on the left, a coordinate grid on the right, and trace a red sine wave as a dot moves around the circle"
        ),
        Suggestion(
            "Matrix Transformation",
            "Apply a 2x2 matrix to a grid",
            "Draw a coordinate grid with two basis vectors, then animate the grid morphing as we apply the transformation matrix [[1, 2], [3, 4]]"
        )
    )

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint = Color(0xFFFF8C00),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "SUGGESTIONS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(suggestions.size) { index ->
                val s = suggestions[index]
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                       .width(220.dp)
                       .height(96.dp)
                       .clickable { onSuggestionClick(s.prompt) }
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = s.title,
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                        )
                        Text(
                            text = s.description,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}
