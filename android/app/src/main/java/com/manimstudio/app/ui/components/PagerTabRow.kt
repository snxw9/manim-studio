package com.manimstudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.manimstudio.app.viewmodel.StudioPhase
import com.manimstudio.app.ui.theme.OutlineVariant
import com.manimstudio.app.ui.theme.Primary
import com.manimstudio.app.ui.theme.OnSurfaceDim
import kotlinx.coroutines.launch

data class TabItem(val icon: ImageVector, val label: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagerTabRow(
    pagerState: PagerState,
    phase: StudioPhase,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val tabs = listOf(
        TabItem(Icons.Outlined.Home, "Home"),
        TabItem(Icons.Outlined.Code, "Editor"),
        TabItem(Icons.Outlined.PlayCircleOutline, "Preview"),
    )

    TabRow(
        selectedTabIndex = pagerState.currentPage,
        modifier = modifier.fillMaxWidth(),
        containerColor = Color.Transparent,
        contentColor = Primary,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                color = Primary,
                height = 2.dp,
            )
        },
        divider = { HorizontalDivider(color = OutlineVariant, thickness = 0.5.dp) },
    ) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = pagerState.currentPage == index,
                onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                icon = {
                    Box {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            modifier = Modifier.size(20.dp),
                        )
                        // Dot indicator for preview when video is ready
                        if (index == 2 && phase == StudioPhase.DONE) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .align(Alignment.TopEnd)
                                    .background(Primary, CircleShape)
                            )
                        }
                    }
                },
                selectedContentColor = Primary,
                unselectedContentColor = OnSurfaceDim,
            )
        }
    }
}
