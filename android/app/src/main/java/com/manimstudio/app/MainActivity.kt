package com.manimstudio.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.manimstudio.app.engine.SetupViewModel
import com.manimstudio.app.ui.screens.SettingsScreen
import com.manimstudio.app.ui.screens.StudioScreen
import com.manimstudio.app.ui.setup.SetupScreen
import com.manimstudio.app.ui.theme.ManimStudioTheme
import java.io.File

class MainActivity : ComponentActivity() {

    private val viewModel: SetupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // FAST CHECK: Does the installation VIP pass exist?
        val isInstalled = File(filesDir, ".installed").exists()
        
        // The traffic cop decides where to start
        val startScreen = if (isInstalled) "studio" else "setup"

        setContent {
            ManimStudioTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    
                    val navController = rememberNavController()
                    val setupState by viewModel.state.collectAsState()

                    NavHost(navController = navController, startDestination = startScreen) {
                        
                        // ROUTE 1: Setup
                        composable("setup") {
                            SetupScreen(
                                state = setupState,
                                onStartSetup = { viewModel.startInstallation() },
                                onRetry = { viewModel.retrySetup() },
                                onTestRender = {
                                    navController.navigate("studio") {
                                        popUpTo("setup") { inclusive = true }
                                    }
                                },
                                onOpenSettings = {}
                            )
                        }

                        // ROUTE 2: Main Studio
                        composable("studio") {
                            StudioScreen(
                                onNavigateToSettings = { navController.navigate("settings") },
                                onNavigateToGallery = { navController.navigate("gallery") },
                                onNavigateToTemplates = { navController.navigate("templates") }
                            )
                        }

                        // ROUTE 3: Settings Screen
                        composable("settings") {
                            SettingsScreen(
                                onBackClick = { navController.popBackStack() } 
                            )
                        }

                        // ROUTE 4: Gallery Placeholder
                        composable("gallery") {
                            Box(modifier = Modifier.fillMaxSize().background(Color.Black))
                        }

                        // ROUTE 5: Templates Placeholder
                        composable("templates") {
                            Box(modifier = Modifier.fillMaxSize().background(Color.Black))
                        }
                    }
                }
            }
        }
    }
}
