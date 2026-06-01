package com.manimstudio.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.manimstudio.app.ui.AppNavigation
import com.manimstudio.app.ui.theme.ManimStudioTheme
import com.manimstudio.app.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ManimApp(settingsViewModel = settingsViewModel)
        }
    }
}

@Composable
fun ManimApp(settingsViewModel: SettingsViewModel) {
    val settings by settingsViewModel.settings.collectAsState()

    ManimStudioTheme(
        themeSettings = settings.themeSettings
    ) {
        AppNavigation(settingsViewModel = settingsViewModel)
    }
}
