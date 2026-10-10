package com.esheeq.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

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

@Composable
fun EsheeqTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = TerminalDarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = TerminalBg.toArgb()
                window.navigationBarColor = TerminalBg.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
