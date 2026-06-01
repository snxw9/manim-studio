package com.manimstudio.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.core.view.WindowCompat
import com.manimstudio.app.data.models.ThemeSettings
import com.manimstudio.app.data.models.ThemeMode
import com.manimstudio.app.data.models.ThemeColor
import com.manimstudio.app.data.models.FontOption

@Composable
fun ManimStudioTheme(
    themeSettings: ThemeSettings = ThemeSettings(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val darkTheme = when (themeSettings.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val baseColorScheme = when {
        themeSettings.useMaterialYou && Build.VERSION.SDK_INT >= 31 -> {
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        else -> {
            // Build color scheme from selected ThemeColor
            val seed = themeSettings.themeColor.color
            if (darkTheme) {
                darkColorScheme(
                    primary = seed,
                    onPrimary = Color.White,
                    primaryContainer = seed.copy(alpha = 0.2f),
                    onPrimaryContainer = seed.copy(alpha = 0.9f),
                )
            } else {
                lightColorScheme(primary = seed)
            }
        }
    }

    // Apply pure black override when enabled
    val colorScheme = if (themeSettings.pureBlackBackground && darkTheme) {
        baseColorScheme.copy(
            background = Color.Black,
            surface = Color(0xFF0D0D0D),
        )
    } else {
        baseColorScheme
    }

    // Edge to edge
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    val typography = if (themeSettings.fontOption == FontOption.SYSTEM) {
        // Build typography using Default fonts
        Typography(
            displayLarge = ManimTypography.displayLarge.copy(fontFamily = FontFamily.Default),
            headlineLarge = ManimTypography.headlineLarge.copy(fontFamily = FontFamily.Default),
            headlineMedium = ManimTypography.headlineMedium.copy(fontFamily = FontFamily.Default),
            titleLarge = ManimTypography.titleLarge.copy(fontFamily = FontFamily.Default),
            titleMedium = ManimTypography.titleMedium.copy(fontFamily = FontFamily.Default),
            titleSmall = ManimTypography.titleSmall.copy(fontFamily = FontFamily.Default),
            bodyLarge = ManimTypography.bodyLarge.copy(fontFamily = FontFamily.Default),
            bodyMedium = ManimTypography.bodyMedium.copy(fontFamily = FontFamily.Default),
            bodySmall = ManimTypography.bodySmall.copy(fontFamily = FontFamily.Default),
            labelLarge = ManimTypography.labelLarge, // keep monospace for code
            labelMedium = ManimTypography.labelMedium.copy(fontFamily = FontFamily.Default),
            labelSmall = ManimTypography.labelSmall.copy(fontFamily = FontFamily.Default),
        )
    } else {
        ManimTypography
    }

    MaterialTheme(colorScheme = colorScheme, typography = typography, content = content)
}
