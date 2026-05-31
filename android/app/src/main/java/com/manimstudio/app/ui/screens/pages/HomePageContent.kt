package com.manimstudio.app.ui.screens.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manimstudio.app.data.models.ChatMessage
import com.manimstudio.app.data.models.SuggestionCard
import com.manimstudio.app.ui.components.ChatMessageItem
import com.manimstudio.app.ui.components.SuggestionChip
import com.manimstudio.app.ui.components.animations.SparkIcon
import com.manimstudio.app.ui.theme.*

@Composable
fun HomePageContent(
    userName: String,
    suggestions: List<SuggestionCard>,
    onSuggestionClick: (SuggestionCard) -> Unit,
    messages: List<ChatMessage>,
) {
    if (messages.isEmpty()) {
        // Empty state — centered welcome
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Animated spark icon
            SparkIcon(
                modifier = Modifier.size(56.dp),
                color = Primary,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Hi $userName,",
                style = MaterialTheme.typography.displayLarge,
                color = OnBackground,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "what's on your mind?",
                style = MaterialTheme.typography.displayLarge,
                color = OnSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp),
            )

            Spacer(modifier = Modifier.weight(1f))

            // Suggestions section
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SUGGESTIONS",
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary,
                        letterSpacing = 1.5.sp,
                    )
                }

                // Horizontal scrolling suggestion cards
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                ) {
                    items(suggestions) { suggestion ->
                        SuggestionChip(
                            suggestion = suggestion,
                            onClick = { onSuggestionClick(suggestion) },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp)) // space for input bar
        }
    } else {
        // Has messages — show chat history
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 16.dp, bottom = 120.dp,
                start = 16.dp, end = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(messages, key = { it.id }) { message ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically { it / 3 },
                ) {
                    ChatMessageItem(message = message)
                }
            }
        }
    }
}
