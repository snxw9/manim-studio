package com.manimstudio.app.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.manimstudio.app.ui.screens.OnboardingScreen
import com.manimstudio.app.ui.screens.SettingsScreen
import com.manimstudio.app.ui.screens.StudioScreen
import com.manimstudio.app.ui.screens.ThemeSettingsScreen
import com.manimstudio.app.ui.screens.TemplatesScreen
import com.manimstudio.app.viewmodel.SettingsViewModel
import com.manimstudio.app.viewmodel.StudioViewModel

@Composable
fun AppNavigation(settingsViewModel: SettingsViewModel) {
    val settings by settingsViewModel.settings.collectAsState()
    val navController = rememberNavController()

    // Determine start destination from saved name
    val startDestination = if (settings.userName.isEmpty()) "onboarding" else "studio"

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            fadeIn(tween(300)) + scaleIn(
                initialScale = 0.95f,
                animationSpec = spring(Spring.DampingRatioMediumBouncy),
            )
        },
        exitTransition = {
            fadeOut(tween(250)) + scaleOut(
                targetScale = 1.02f,
                animationSpec = spring(Spring.DampingRatioNoBouncy),
            )
        },
        popEnterTransition = {
            fadeIn(tween(300)) + scaleIn(
                initialScale = 1.02f,
                animationSpec = spring(Spring.DampingRatioMediumBouncy),
            )
        },
        popExitTransition = {
            fadeOut(tween(250)) + scaleOut(
                targetScale = 0.95f,
                animationSpec = spring(Spring.DampingRatioNoBouncy),
            )
        }
    ) {
        composable("onboarding") {
            OnboardingScreen(
                onComplete = { name ->
                    settingsViewModel.updateUserName(name)
                    navController.navigate("studio") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                },
            )
        }

        composable("studio") {
            StudioScreen(
                viewModel = viewModel(),
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToTemplates = { navController.navigate("templates") },
            )
        }

        composable("settings") {
            SettingsScreen(
                settingsViewModel = settingsViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToTheme = { navController.navigate("theme_settings") }
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
                    spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMediumLow),
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMedium),
                )
            },
        ) {
            val viewModel: StudioViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            val templates = uiState.templates
            TemplatesScreen(
                templates = templates,
                onSelectTemplate = { template ->
                    viewModel.onTemplateSelected(template.id)
                    navController.navigate("studio") {
                        popUpTo("studio") { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }
    }
}
