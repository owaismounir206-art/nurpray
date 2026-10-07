package com.nurpray.app.core.designsystem

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = EmeraldDeep,
    onPrimary = Color.White,
    primaryContainer = EmeraldContainerLight,
    onPrimaryContainer = OnEmeraldContainerLight,
    secondary = AmberGold,
    onSecondary = Color.White,
    secondaryContainer = AmberLight,
    onSecondaryContainer = OnAmberContainerLight,
    background = SurfaceLight,
    onBackground = Color(0xFF191C1A),
    surface = SurfaceLight,
    onSurface = Color(0xFF191C1A),
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    surfaceContainerLowest = SurfaceContainerLowestLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight
)

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldNight,
    onPrimary = Color(0xFF003820),
    primaryContainer = EmeraldDarkContainer,
    onPrimaryContainer = OnEmeraldDarkContainer,
    secondary = AmberDark,
    onSecondary = Color(0xFF432C00),
    secondaryContainer = AmberDarkContainer,
    onSecondaryContainer = OnAmberDarkContainer,
    background = SurfaceDark,
    onBackground = Color(0xFFE1E5E1),
    surface = SurfaceDark,
    onSurface = Color(0xFFE1E5E1),
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark
)

@Composable
fun NurPrayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep signature Islamic Emerald/Gold brand identity consistent
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
