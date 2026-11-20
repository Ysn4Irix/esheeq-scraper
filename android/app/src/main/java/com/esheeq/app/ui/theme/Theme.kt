package com.esheeq.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.esheeq.app.data.repository.AppThemeMode

private val TerminalDarkColorScheme = darkColorScheme(
    primary = TerminalBlue,
    onPrimary = TerminalBg,
    primaryContainer = TerminalBlueDim,
    onPrimaryContainer = TerminalBlue,
    secondary = TerminalEmerald,
    onSecondary = TerminalBg,
    secondaryContainer = TerminalEmeraldDim,
    onSecondaryContainer = TerminalEmerald,
    background = TerminalBg,
    onBackground = TerminalTextPrimary,
    surface = TerminalSurface,
    onSurface = TerminalTextPrimary,
    surfaceVariant = TerminalSurfaceElevated,
    onSurfaceVariant = TerminalTextMuted,
    outline = TerminalBorder,
    outlineVariant = TerminalBorderSubtle,
    error = TerminalRed,
    onError = TerminalBg
)

private val TerminalLightColorScheme = lightColorScheme(
    primary = LightBlue,
    onPrimary = Color.White,
    primaryContainer = LightBlueDim,
    onPrimaryContainer = LightBlue,
    secondary = LightEmerald,
    onSecondary = Color.White,
    secondaryContainer = LightEmeraldDim,
    onSecondaryContainer = LightEmerald,
    background = LightBg,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = LightTextMuted,
    outline = LightBorder,
    outlineVariant = LightBorderSubtle,
    error = LightRed,
    onError = Color.White
)

val LocalEsheeqColors = staticCompositionLocalOf { DarkEsheeqColors }

object EsheeqTheme {
    val colors: EsheeqColors
        @Composable
        @ReadOnlyComposable
        get() = LocalEsheeqColors.current
}

@Composable
fun EsheeqTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isDarkTheme = when (themeMode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val esheeqColors = if (isDarkTheme) DarkEsheeqColors else LightEsheeqColors
    val colorScheme = if (isDarkTheme) TerminalDarkColorScheme else TerminalLightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                @Suppress("DEPRECATION")
                window.statusBarColor = esheeqColors.surface.toArgb()
                @Suppress("DEPRECATION")
                window.navigationBarColor = esheeqColors.bg.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !isDarkTheme
                controller.isAppearanceLightNavigationBars = !isDarkTheme
            }
        }
    }

    CompositionLocalProvider(LocalEsheeqColors provides esheeqColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
