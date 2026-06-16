package com.manimstudio.app.ui

import android.app.Application
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.manimstudio.app.engine.SetupViewModel
import com.manimstudio.app.ui.screens.BootstrapSetupScreen
import com.manimstudio.app.ui.screens.OnboardingScreen
import com.manimstudio.app.ui.screens.SettingsScreen
import com.manimstudio.app.ui.screens.StudioScreen
import com.manimstudio.app.ui.screens.TemplatesScreen
import com.manimstudio.app.ui.screens.ThemeSettingsScreen
import com.manimstudio.app.ui.screens.WelcomeRenderScreen
import com.manimstudio.app.viewmodel.SettingsViewModel
import com.manimstudio.app.viewmodel.StudioViewModel

@Composable
fun AppNavigation(
    settingsViewModel: SettingsViewModel,
    setupViewModel: SetupViewModel,
) {
    val navController = rememberNavController()
    val settings by settingsViewModel.settings.collectAsState()

    // Determine start destination once on launch
    val startDestination = remember {
        when {
            settings.userName.isEmpty() -> "onboarding"
            !setupViewModel.isBootstrapInstalled() -> "bootstrap_setup"
            else -> "studio"
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            fadeIn(tween(280)) + scaleIn(
                initialScale = 0.96f,
                animationSpec = spring(Spring.DampingRatioMediumBouncy,
                    Spring.StiffnessMediumLow),
            )
        },
        exitTransition = {
            fadeOut(tween(200)) + scaleOut(targetScale = 1.02f)
        },
        popEnterTransition = {
            fadeIn(tween(280))
        },
        popExitTransition = {
            fadeOut(tween(200)) + slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                tween(280),
            )
        },
    ) {
        // Step 1: Name collection only — no render, no network
        composable("onboarding") {
            OnboardingScreen(
                onNameSaved = { name ->
                    settingsViewModel.updateUserName(name)
                    // Go to bootstrap setup — never to studio directly
                    navController.navigate("bootstrap_setup") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                },
            )
        }

        // Step 2: Download and install the bootstrap
        composable("bootstrap_setup") {
            BootstrapSetupScreen(
                setupViewModel = setupViewModel,
                userName = settings.userName,
                onComplete = {
                    // Bootstrap installed — now show welcome render
                    navController.navigate("welcome_render") {
                        popUpTo("bootstrap_setup") { inclusive = true }
                    }
                },
            )
        }

        // Step 3: Welcome Manim animation (bootstrap is now ready)
        composable("welcome_render") {
            WelcomeRenderScreen(
                setupViewModel = setupViewModel,
                userName = settings.userName,
                onComplete = {
                    navController.navigate("studio") {
                        popUpTo("welcome_render") { inclusive = true }
                    }
                },
            )
        }

        // Main app
        composable("studio") {
            val application = LocalContext.current.applicationContext as Application
            val studioViewModel: StudioViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return StudioViewModel(
                            application = application,
                            prootEngine = setupViewModel.engine,
                        ) as T
                    }
                }
            )
            StudioScreen(
                viewModel = studioViewModel,
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToTemplates = { navController.navigate("templates") },
            )
        }

        composable(
            "settings",
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMediumLow),
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMedium),
                )
            },
        ) {
            SettingsScreen(
                settingsViewModel = settingsViewModel,
                setupViewModel = setupViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToTheme = { navController.navigate("theme_settings") },
                onReinstall = {
                    navController.navigate("bootstrap_setup") {
                        popUpTo("settings") { inclusive = false }
                    }
                },
            )
        }

        composable("theme_settings") {
            ThemeSettingsScreen(
                themeSettings = settings.themeSettings,
                onUpdate = { settingsViewModel.updateThemeSettings(it) },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            "templates",
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow),
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMedium),
                )
            },
        ) {
            val application = LocalContext.current.applicationContext as Application
            val studioViewModel: StudioViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return StudioViewModel(
                            application = application,
                            prootEngine = setupViewModel.engine,
                        ) as T
                    }
                }
            )
            val templates by studioViewModel.templates.collectAsState()
            TemplatesScreen(
                templates = templates,
                onSelectTemplate = { template ->
                    studioViewModel.onTemplateSelected(template.id)
                    navController.navigate("studio") {
                        popUpTo("templates") { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }
    }
}
