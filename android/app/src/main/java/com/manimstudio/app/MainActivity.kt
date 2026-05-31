package com.manimstudio.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.manimstudio.app.data.PreferencesManager
import com.manimstudio.app.engine.SetupViewModel
import com.manimstudio.app.ui.screens.EditorScreen
import com.manimstudio.app.ui.screens.GalleryScreen
import com.manimstudio.app.ui.screens.SettingsScreen
import com.manimstudio.app.ui.screens.StudioScreen
import com.manimstudio.app.ui.screens.TemplatesScreen
import com.manimstudio.app.ui.setup.SetupScreen
import com.manimstudio.app.ui.theme.ManimStudioTheme
import java.io.File

import androidx.compose.animation.*
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import com.manimstudio.app.viewmodel.StudioViewModel

class MainActivity : ComponentActivity() {

    private val setupViewModel: SetupViewModel by viewModels()
    private val studioViewModel: StudioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val isInstalled = File(filesDir, ".installed").exists()
        val startScreen = if (isInstalled) "studio" else "setup"

        setContent {
            val context = LocalContext.current
            val themePreference by PreferencesManager.getTheme(context).collectAsState(initial = "System")

            ManimStudioTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background 
                ) {
                    val navController = rememberNavController()
                    val setupState by setupViewModel.state.collectAsState()

                    NavHost(navController = navController, startDestination = startScreen) {
                        // ROUTE 1: Setup
                        composable(
                            route = "setup",
                            enterTransition = { fadeIn(tween(300)) },
                            exitTransition = { fadeOut(tween(200)) }
                        ) {
                            SetupScreen(
                                state = setupState,
                                onStartSetup = { setupViewModel.startInstallation() },
                                onRetry = { setupViewModel.retrySetup() },
                                onTestRender = {
                                    navController.navigate("studio") {
                                        popUpTo("setup") { inclusive = true }
                                    }
                                },
                                onOpenSettings = {}
                            )
                        }

                        // ROUTE 2: Main Studio
                        composable(
                            route = "studio",
                            enterTransition = { fadeIn(tween(300)) },
                            exitTransition = { fadeOut(tween(200)) }
                        ) {
                            StudioScreen(
                                viewModel = studioViewModel,
                                onNavigateToSettings = { navController.navigate("settings") }
                            )
                        }

                        // ROUTE 3: Settings
                        composable(
                            route = "settings",
                            enterTransition = {
                                slideIntoContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                                    animationSpec = tween(350, easing = EaseOutCubic),
                                )
                            },
                            exitTransition = {
                                slideOutOfContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                                    animationSpec = tween(300, easing = EaseInCubic),
                                )
                            },
                        ) {
                            SettingsScreen(onBack = { navController.popBackStack() })
                        }

                        // ROUTE 4 & 5: Gallery & Templates placeholders
                        composable("gallery") { 
                            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                                GalleryScreen(onBackClick = { navController.popBackStack() })
                            }
                        }
                        composable("templates") { 
                            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                                TemplatesScreen(onBackClick = { navController.popBackStack() })
                            }
                        }
                    }
                }
            }
        }
    }
}
