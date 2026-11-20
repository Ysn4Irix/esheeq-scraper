package com.esheeq.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Dark palette tokens (also used directly by cinema PlayerScreen overlays)
val TerminalBg = Color(0xFF0D1117)
val TerminalSurface = Color(0xFF161B22)
val TerminalSurfaceElevated = Color(0xFF21262D)
val TerminalBorder = Color(0xFF30363D)
val TerminalBorderSubtle = Color(0xFF21262D)

val TerminalTextPrimary = Color(0xFFE6EDF3)
val TerminalTextMuted = Color(0xFF8B949E)

val TerminalBlue = Color(0xFF58A6FF)
val TerminalBlueDim = Color(0x2658A6FF)

val TerminalEmerald = Color(0xFF2EA043)
val TerminalEmeraldDim = Color(0x262EA043)

val TerminalRed = Color(0xFFF85149)
val TerminalRedDim = Color(0x26F85149)

val TerminalWarning = Color(0xFFD29922)
val TerminalWarningDim = Color(0x26D29922)
val TerminalWarningText = Color(0xFFE3B341)

// GitHub Light palette tokens (WCAG AA verified >= 4.5:1 text contrast)
val LightBg = Color(0xFFF6F8FA)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceElevated = Color(0xFFEAEEF2)
val LightBorder = Color(0xFFD0D7DE)
val LightBorderSubtle = Color(0xFFE1E4E8)

val LightTextPrimary = Color(0xFF1F2328)
val LightTextMuted = Color(0xFF59636E)

val LightBlue = Color(0xFF075CC4)
val LightBlueDim = Color(0x14075CC4)

val LightEmerald = Color(0xFF167432)
val LightEmeraldDim = Color(0x14167432)

val LightRed = Color(0xFFCF222E)
val LightRedDim = Color(0x14CF222E)

val LightWarning = Color(0xFF9A6700)
val LightWarningDim = Color(0x1A9A6700)
val LightWarningText = Color(0xFF7D4E00)

/**
 * Semantic theme palette resolved via [EsheeqTheme.colors].
 */
@Immutable
data class EsheeqColors(
    val isDark: Boolean,
    val bg: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val border: Color,
    val borderSubtle: Color,
    val textPrimary: Color,
    val textMuted: Color,
    val blue: Color,
    val blueDim: Color,
    val emerald: Color,
    val emeraldDim: Color,
    val red: Color,
    val redDim: Color,
    val warning: Color,
    val warningDim: Color,
    val warningText: Color,
    val onAccent: Color
)

val DarkEsheeqColors = EsheeqColors(
    isDark = true,
    bg = TerminalBg,
    surface = TerminalSurface,
    surfaceElevated = TerminalSurfaceElevated,
    border = TerminalBorder,
    borderSubtle = TerminalBorderSubtle,
    textPrimary = TerminalTextPrimary,
    textMuted = TerminalTextMuted,
    blue = TerminalBlue,
    blueDim = TerminalBlueDim,
    emerald = TerminalEmerald,
    emeraldDim = TerminalEmeraldDim,
    red = TerminalRed,
    redDim = TerminalRedDim,
    warning = TerminalWarning,
    warningDim = TerminalWarningDim,
    warningText = TerminalWarningText,
    onAccent = Color.White
)

val LightEsheeqColors = EsheeqColors(
    isDark = false,
    bg = LightBg,
    surface = LightSurface,
    surfaceElevated = LightSurfaceElevated,
    border = LightBorder,
    borderSubtle = LightBorderSubtle,
    textPrimary = LightTextPrimary,
    textMuted = LightTextMuted,
    blue = LightBlue,
    blueDim = LightBlueDim,
    emerald = LightEmerald,
    emeraldDim = LightEmeraldDim,
    red = LightRed,
    redDim = LightRedDim,
    warning = LightWarning,
    warningDim = LightWarningDim,
    warningText = LightWarningText,
    onAccent = Color.White
)
