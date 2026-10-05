package com.nurpray.app.core.designsystem

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = EmeraldDeep,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = OnEmeraldContainer,
    secondary = AmberGold,
    onSecondary = androidx.compose.ui.graphics.Color.Black,
    secondaryContainer = AmberLight,
    onSecondaryContainer = OnAmberContainer,
    background = SurfaceLight,
    surface = SurfaceLight,
    surfaceContainerLowest = androidx.compose.ui.graphics.Color.White,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    onSurface = androidx.compose.ui.graphics.Color(0xFF1C1B1F)
)

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldNight,
    onPrimary = androidx.compose.ui.graphics.Color(0xFF00391C),
    primaryContainer = EmeraldDarkContainer,
    onPrimaryContainer = EmeraldNight,
    secondary = AmberDark,
    onSecondary = androidx.compose.ui.graphics.Color(0xFF432C00),
    secondaryContainer = OnAmberContainer,
    onSecondaryContainer = AmberDark,
    background = SurfaceDark,
    surface = SurfaceDark,
    surfaceContainerLowest = androidx.compose.ui.graphics.Color(0xFF0C0E0D),
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    onSurface = androidx.compose.ui.graphics.Color(0xFFE2E3DF)
)

@Composable
fun NurPrayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
