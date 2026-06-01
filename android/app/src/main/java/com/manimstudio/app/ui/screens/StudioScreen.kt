package com.manimstudio.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
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
            AmbientGlow(modifier = Modifier.align(Alignment.BottomCenter))
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
                            messages = uiState.messages,
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
                    onNewChatClick = { viewModel.onNewChat() },
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
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 12.dp)
                    .navigationBarsPadding(),
            ) {
                AnimatedVisibility(
                    visible = pagerState.currentPage == 1 && uiState.phase == StudioPhase.IDLE,
                    enter = fadeIn() + slideInVertically { it },
                    exit = fadeOut() + slideOutVertically { it },
                ) {
                    CompactRenderChips(
                        quality = uiState.renderQuality,
                        format = uiState.renderFormat,
                        onQualityChange = viewModel::onQualityChanged,
                        onFormatChange = viewModel::onFormatChanged,
                        onRender = viewModel::onRenderFromEditor,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                }
                
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

            // ── LAYER 6: Engine dropdown (above input, below status bar) ──
            AnimatedVisibility(
                visible = uiState.showEngineSelector,
                enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom) +
                        scaleIn(
                            initialScale = 0.95f,
                            transformOrigin = TransformOrigin(0.2f, 1f)
                        ),
                exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom) +
                       scaleOut(
                           targetScale = 0.95f,
                           transformOrigin = TransformOrigin(0.2f, 1f)
                       ),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 130.dp) // above input bar
                    .widthIn(max = 300.dp)
                    .zIndex(10f),
            ) {
                EngineSelectorDropdown(
                    selected = uiState.selectedEngine,
                    onSelect = viewModel::onEngineSelected,
                    onDismiss = viewModel::toggleEngineSelector,
                )
            }
        }
    }
}
