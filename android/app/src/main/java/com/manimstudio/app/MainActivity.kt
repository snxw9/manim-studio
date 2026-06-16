package com.manimstudio.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.manimstudio.app.ui.AppNavigation
import com.manimstudio.app.ui.theme.ManimStudioTheme
import com.manimstudio.app.viewmodel.SettingsViewModel
import com.manimstudio.app.engine.SetupViewModel

class MainActivity : ComponentActivity() {
    // ViewModels created once at activity level — shared across all screens
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val setupViewModel: SetupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val settings by settingsViewModel.settings.collectAsState()

            ManimStudioTheme(
                themeSettings = settings.themeSettings,
                fontOption = settings.themeSettings.fontOption,
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AppNavigation(
                        settingsViewModel = settingsViewModel,
                        setupViewModel = setupViewModel,
                    )
                }
            }
        }
    }
}
