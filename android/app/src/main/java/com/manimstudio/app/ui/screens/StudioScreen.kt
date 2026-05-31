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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.dp
import com.manimstudio.app.data.models.StudioPhase
import com.manimstudio.app.ui.components.*
import com.manimstudio.app.ui.components.animations.AmbientGlow
import com.manimstudio.app.ui.components.animations.RenderingGradientBackground
import com.manimstudio.app.ui.screens.pages.EditorPageContent
import com.manimstudio.app.ui.screens.pages.HomePageContent
import com.manimstudio.app.ui.screens.pages.VideoPageContent
import com.manimstudio.app.ui.theme.Background
import com.manimstudio.app.viewmodel.StudioViewModel
import kotlinx.coroutines.launch

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
        Box(modifier = Modifier.fillMaxSize().background(Background)) {

            // Ambient orange glow at bottom — always present, subtle
            AmbientGlow(modifier = Modifier.align(Alignment.BottomCenter))

            // Pulsing mesh gradient during rendering
            AnimatedVisibility(
                visible = uiState.phase == StudioPhase.RENDERING ||
                          uiState.phase == StudioPhase.GENERATING,
                enter = fadeIn(tween(800)),
                exit = fadeOut(tween(600)),
            ) {
                RenderingGradientBackground()
            }

            Column(modifier = Modifier.fillMaxSize()) {
                // Top bar
                StudioTopBar(
                    currentEngine = uiState.selectedEngine,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onEngineClick = { viewModel.toggleEngineSelector() },
                    onNewChatClick = { viewModel.onNewChat() },
                )

                // Tab indicator — only visible when not on home page
                AnimatedVisibility(
                    visible = pagerState.currentPage > 0 ||
                              pagerState.currentPageOffsetFraction != 0f,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically(),
                ) {
                    PagerTabRow(pagerState = pagerState, phase = uiState.phase)
                }

                // Main content — swipeable pages
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f),
                    beyondViewportPageCount = 1,
                ) { page ->
                    when (page) {
                        0 -> HomePageContent(
                            userName = uiState.userName,
                            suggestions = uiState.suggestions,
                            onSuggestionClick = { suggestion ->
                                viewModel.onInputChanged(suggestion.prompt)
                                viewModel.onSendPrompt()
                            },
                            messages = uiState.messages,
                        )
                        1 -> EditorPageContent(
                            code = uiState.generatedCode,
                            onCodeChanged = viewModel::onCodeChanged,
                            phase = uiState.phase,
                            renderProgress = uiState.renderProgress,
                            elapsedSeconds = uiState.elapsedSeconds,
                            messages = uiState.messages,
                        )
                        2 -> VideoPageContent(
                            videoFile = uiState.lastVideoFile,
                            onExport = viewModel::onExportVideo,
                        )
                    }
                }
            }

            // Engine selector dropdown overlay
            AnimatedVisibility(
                visible = uiState.showEngineSelector,
                enter = fadeIn() + scaleIn(initialScale = 0.9f,
                    transformOrigin = TransformOrigin(0.5f, 0f)),
                exit = fadeOut() + scaleOut(targetScale = 0.9f,
                    transformOrigin = TransformOrigin(0.5f, 0f)),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 56.dp)
            ) {
                EngineSelectorDropdown(
                    selected = uiState.selectedEngine,
                    onSelect = { viewModel.onEngineSelected(it) },
                    onDismiss = { viewModel.toggleEngineSelector() },
                )
            }

            // Floating input bar at bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
                    .navigationBarsPadding(),
            ) {
                // Render setting chips — float above input when on editor page
                AnimatedVisibility(
                    visible = pagerState.currentPage == 1 &&
                              uiState.phase == StudioPhase.IDLE,
                    enter = fadeIn() + slideInVertically { it / 2 },
                    exit = fadeOut() + slideOutVertically { it / 2 },
                ) {
                    RenderSettingChips(
                        quality = uiState.renderQuality,
                        format = uiState.renderFormat,
                        onQualityChange = viewModel::onQualityChanged,
                        onFormatChange = viewModel::onFormatChanged,
                        onRender = viewModel::onRenderFromEditor,
                        estimatedTime = uiState.renderQuality.estimatedSeconds,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }

                FloatingPromptInput(
                    text = uiState.inputText,
                    onTextChange = viewModel::onInputChanged,
                    onSend = viewModel::onSendPrompt,
                    onStop = viewModel::onStopRender,
                    onTemplateClick = { viewModel.showTemplates() },
                    isRendering = uiState.phase == StudioPhase.RENDERING ||
                                  uiState.phase == StudioPhase.GENERATING,
                    currentPage = pagerState.currentPage,
                )
            }
        }
    }
}
