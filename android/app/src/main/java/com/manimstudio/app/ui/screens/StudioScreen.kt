package com.manimstudio.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import com.manimstudio.app.data.models.StudioPhase
import com.manimstudio.app.ui.components.*
import com.manimstudio.app.ui.components.animations.AmbientGlow
import com.manimstudio.app.ui.components.animations.RenderingGradientBackground
import com.manimstudio.app.ui.screens.pages.EditorPageContent
import com.manimstudio.app.ui.screens.pages.HomePageContent
import com.manimstudio.app.ui.screens.pages.VideoPageContent
import com.manimstudio.app.viewmodel.StudioViewModel
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StudioScreen(
    viewModel: StudioViewModel,
    onNavigateToSettings: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 3 })
    
    val isRendering = uiState.phase == StudioPhase.RENDERING ||
                      uiState.phase == StudioPhase.GENERATING

    // Auto-switch to editor when generating starts
    LaunchedEffect(uiState.phase) {
        when (uiState.phase) {
            StudioPhase.GENERATING, StudioPhase.RENDERING -> {
                pagerState.animateScrollToPage(1)
            }
            StudioPhase.DONE -> {
                pagerState.animateScrollToPage(1)
            }
            else -> {}
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage == 1) {
            viewModel.collapseInput()
        } else {
            viewModel.expandInput()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            StudioDrawer(
                onNewChat = { viewModel.onNewChat(); scope.launch { drawerState.close() } },
                onNavigateToSettings = onNavigateToSettings,
                onGalleryClick = { /* navigate to gallery */ },
                onTemplatesClick = { viewModel.showTemplates() },
                recentChats = uiState.recentChats,
                onSelectChat = { /* load chat */ scope.launch { drawerState.close() } },
                userName = uiState.userName,
                onClose = { scope.launch { drawerState.close() } },
            )
        },
        scrimColor = Color.Black.copy(alpha = 0.6f),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            // ── LAYER 1: full-screen gradient (behind everything) ──
            AmbientGlow(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 0.dp) // sits at very bottom
            )
            AnimatedVisibility(
                visible = isRendering,
                enter = fadeIn(tween(800)),
                exit = fadeOut(tween(600)),
            ) {
                RenderingGradientBackground()
            }

            // ── LAYER 2: HorizontalPager (full screen including under status bar) ──
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 1,
            ) { page ->
                val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                val absOffset = abs(pageOffset).coerceIn(0f, 1f)
                val scale = 0.92f + (1f - 0.92f) * (1f - absOffset)
                val alpha = 0.5f + (1f - 0.5f) * (1f - absOffset)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                        }
                ) {
                    when (page) {
                        0 -> HomePageContent(
                            userName = uiState.userName,
                            suggestions = uiState.suggestions,
                            onSuggestionClick = { suggestion ->
                                viewModel.onInputChanged(suggestion.prompt)
                                viewModel.onSendPrompt()
                            },
                            messages = uiState.messages,
                            modifier = Modifier.fillMaxSize(),
                        )
                        1 -> EditorPageContent(
                            code = uiState.generatedCode,
                            onCodeChanged = viewModel::onCodeChanged,
                            phase = uiState.phase,
                            renderProgress = uiState.renderProgress,
                            elapsedSeconds = uiState.elapsedSeconds,
                            modifier = Modifier.fillMaxSize(),
                        )
                        2 -> VideoPageContent(
                            videoFile = uiState.lastVideoFile,
                            onExport = viewModel::onExportVideo,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }

            // ── LAYER 3: Top bar OVERLAYS content with fade gradient behind it ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart),
            ) {
                // Fade gradient so content behind top bar is legible
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.background,
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
                                    Color.Transparent,
                                )
                            )
                        )
                )
                StudioTopBar(
                    onMenuClick = { scope.launch { drawerState.open() } },
                    modifier = Modifier.statusBarsPadding(),
                )
            }

            // ── LAYER 4: Page tab dots (below top bar) ──
            AnimatedVisibility(
                visible = pagerState.currentPage > 0 || pagerState.currentPageOffsetFraction != 0f,
                enter = fadeIn(), 
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 80.dp),
            ) {
                PagerTabRow(pagerState = pagerState, phase = uiState.phase)
            }

            // ── LAYER 5: Bottom input + chips (always on top) ──
            val isEditorPage = pagerState.currentPage == 1
            val inputExpanded = uiState.inputExpanded

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                // Render chips — visible on editor page only
                AnimatedVisibility(
                    visible = isEditorPage && uiState.phase == StudioPhase.IDLE,
                    enter = fadeIn() + slideInVertically(
                        animationSpec = spring(Spring.DampingRatioMediumBouncy)
                    ) { it },
                    exit = fadeOut() + slideOutVertically { it },
                ) {
                    CompactRenderChips(
                        quality = uiState.renderQuality,
                        format = uiState.renderFormat,
                        onQualityChange = viewModel::onQualityChanged,
                        onFormatChange = viewModel::onFormatChanged,
                        onRender = viewModel::onRenderFromEditor,
                    )
                }

                // Input — collapsed bubble on editor, full on home/preview
                AnimatedContent(
                    targetState = isEditorPage && !inputExpanded,
                    transitionSpec = {
                        fadeIn(tween(250)) + scaleIn(
                            initialScale = if (targetState) 0.8f else 1.1f,
                            animationSpec = spring(Spring.DampingRatioMediumBouncy),
                        ) togetherWith fadeOut(tween(200)) + scaleOut(
                            targetScale = if (targetState) 1.1f else 0.8f,
                        )
                    },
                    label = "inputMorph",
                ) { isCollapsed ->
                    if (isCollapsed) {
                        // Collapsed pill — just an edit icon bubble
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            Surface(
                                onClick = { viewModel.expandInput() },
                                shape = RoundedCornerShape(percent = 50),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                                shadowElevation = 4.dp,
                                modifier = Modifier.height(44.dp),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(
                                        Icons.Outlined.Edit, "Expand input",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Text(
                                        "Describe...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    } else {
                        FloatingPromptInput(
                            text = uiState.inputText,
                            onTextChange = viewModel::onInputChanged,
                            onSend = viewModel::onSendPrompt,
                            onStop = viewModel::onStopRender,
                            onTemplateClick = { viewModel.showTemplates() },
                            isRendering = isRendering,
                            selectedEngine = uiState.selectedEngine,
                            onEngineClick = { viewModel.toggleEngineSelector() },
                        )
                    }
                }
            }

            // ── LAYER 6: Bottom Sheets for Engine & Templates ──
            EngineBottomSheet(
                visible = uiState.showEngineSelector,
                selected = uiState.selectedEngine,
                onSelect = viewModel::onEngineSelected,
                onDismiss = viewModel::toggleEngineSelector,
            )

            TemplatePickerSheet(
                visible = uiState.showTemplatePicker,
                templates = uiState.templates,
                onSelectTemplate = { template ->
                    viewModel.onTemplateSelected(template.id)
                    scope.launch { pagerState.animateScrollToPage(1) }
                },
                onDismiss = { viewModel.hideTemplatePicker() },
            )
        }
    }
}
