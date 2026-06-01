package com.manimstudio.app.ui.screens.pages

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manimstudio.app.data.models.ChatMessage
import com.manimstudio.app.data.models.SuggestionCard
import com.manimstudio.app.ui.components.ChatMessageItem
import com.manimstudio.app.ui.components.animations.SparkIcon

@Composable
fun HomePageContent(
    userName: String,
    suggestions: List<SuggestionCard>,
    onSuggestionClick: (SuggestionCard) -> Unit,
    messages: List<ChatMessage>,
    modifier: Modifier = Modifier,
) {
    if (messages.isEmpty()) {
        // Empty state — centered welcome
        Column(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 60.dp) // space for top bar overlay
                .padding(bottom = 140.dp) // space for floating input + chips
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Animated spark icon
            SparkIcon(
                modifier = Modifier.size(56.dp),
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Hi $userName,",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "what's on your mind?",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp),
            )

            Spacer(modifier = Modifier.weight(1f))

            // Suggestions section
            CyclingSuggestions(
                suggestions = if (suggestions.isEmpty()) defaultSuggestions else suggestions,
                onSuggestionClick = onSuggestionClick,
            )
        }
    } else {
        // Has messages — show chat history
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 60.dp), // space for top bar overlay
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

@Composable
fun CyclingSuggestions(
    suggestions: List<SuggestionCard>,
    onSuggestionClick: (SuggestionCard) -> Unit,
) {
    if (suggestions.isEmpty()) return

    val pageSize = 2
    val pageCount = (suggestions.size + pageSize - 1) / pageSize
    var currentPage by remember { mutableIntStateOf(0) }
    var isPaused by remember { mutableStateOf(false) }

    LaunchedEffect(isPaused) {
        if (!isPaused) {
            while (true) {
                delay(4500)
                currentPage = (currentPage + 1) % pageCount
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 10.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "SUGGESTIONS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.5.sp,
            )
        }

        // Pure cross-fade — no sliding
        AnimatedContent(
            targetState = currentPage,
            transitionSpec = {
                fadeIn(tween(500)) togetherWith fadeOut(tween(350))
            },
            label = "suggestionFade",
        ) { page ->
            val start = page * pageSize
            val pageSuggestions = suggestions.drop(start).take(pageSize)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                pageSuggestions.forEach { suggestion ->
                    CompactSuggestionChip(
                        suggestion = suggestion,
                        onClick = {
                            isPaused = true
                            onSuggestionClick(suggestion)
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (pageSuggestions.size < pageSize) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // Compact dot indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            repeat(pageCount) { i ->
                val active = i == currentPage
                val w by animateDpAsState(
                    targetValue = if (active) 14.dp else 4.dp,
                    animationSpec = tween(250),
                    label = "dotW$i",
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 2.dp)
                        .height(4.dp)
                        .width(w)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (active) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { currentPage = i }
                )
            }
        }
    }
}

@Composable
fun CompactSuggestionChip(
    suggestion: SuggestionCard,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = modifier.height(72.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = suggestion.title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = suggestion.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp,
            )
        }
    }
}

val defaultSuggestions = listOf(
    SuggestionCard("Pythagorean Theorem", "Visual proof with labeled squares", "Create a visual proof of the Pythagorean Theorem with labeled squares"),
    SuggestionCard("Bubble Sort", "Step by step sorting animation", "Animate a step-by-step bubble sort algorithm"),
    SuggestionCard("Sine Wave", "Unit circle and wave generation", "Show the unit circle and sine wave generation"),
    SuggestionCard("Fibonacci Spiral", "Golden ratio and nature pattern", "Animate the construction of a Fibonacci Spiral"),
    SuggestionCard("Binary Search", "Divide and conquer search", "Explain binary search visually"),
    SuggestionCard("Koch Snowflake", "Recursive fractal subdivision", "Create a Koch Snowflake fractal animation"),
    SuggestionCard("Riemann Sums", "Rectangles converging to integral", "Animate Riemann Sums with rectangles converging to an integral"),
    SuggestionCard("Matrix Multiply", "2x2 multiplication steps", "Visualize 2x2 matrix multiplication"),
)
