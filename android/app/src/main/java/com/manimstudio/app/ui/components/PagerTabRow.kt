package com.manimstudio.app.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.manimstudio.app.data.models.StudioPhase
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PagerTabRow(
    pagerState: PagerState,
    phase: StudioPhase,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    // Only show when user is not on home page
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            listOf(
                Pair(Icons.Outlined.Home, "Home"),
                Pair(Icons.Outlined.Code, "Editor"),
                Pair(Icons.Outlined.PlayCircleOutline, "Preview"),
            ).forEachIndexed { index, (icon, label) ->
                val selected = pagerState.currentPage == index
                val size by animateDpAsState(
                    targetValue = if (selected) 6.dp else 4.dp,
                    animationSpec = tween(200),
                    label = "dotSize",
                )
                val alpha by animateFloatAsState(
                    targetValue = if (selected) 1f else 0.3f,
                    animationSpec = tween(200),
                    label = "dotAlpha",
                )

                Box(
                    modifier = Modifier
                        .size(size)
                        .alpha(alpha)
                        .background(
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            shape = CircleShape,
                        )
                        .clickable { scope.launch { pagerState.animateScrollToPage(index) } }
                )
            }
        }
    }
}
