package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = AgriXGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = AgriXGreenContainer,
    onPrimaryContainer = AgriXGreenDark,
    secondary = AgriXGreenLight,
    onSecondary = AgriXGreenDark,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF111827),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF4B5563),
    outline = Color(0xFFE2E8F0)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF22C55E), // Saturated but not neon
    onPrimary = Color(0xFF062013),
    primaryContainer = Color(0xFF166534),
    onPrimaryContainer = Color(0xFFBBF7D0),
    secondary = Color(0xFF171F22), // Primary Surface
    onSecondary = Color(0xFFF1F5F9),
    background = Color(0xFF0B1215), // Layer 1: App Background
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF171F22), // Layer 2: Primary Surface
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF242E32), // Layer 3: Secondary Surface
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF334139), // Green-tinted border
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A)
)

data class AgriXCustomColors(
    val cardGreenBg: Color,
    val cardGreenText: Color,
    val cardOrangeBg: Color,
    val cardOrangeText: Color,
    val cardBlueBg: Color,
    val cardBlueText: Color,
    val cardPurpleBg: Color,
    val cardPurpleText: Color,
    val badgeGrowingBg: Color,
    val badgeGrowingText: Color,
    val badgeSownBg: Color,
    val badgeSownText: Color,
    val badgePreparingBg: Color,
    val badgePreparingText: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val borderSubtle: Color,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color
)

val LightCustomColors = AgriXCustomColors(
    cardGreenBg = Color(0xFFDCFCE7),
    cardGreenText = Color(0xFF15803D),
    cardOrangeBg = Color(0xFFFEF3C7),
    cardOrangeText = Color(0xFFB45309),
    cardBlueBg = Color(0xFFE0F2FE),
    cardBlueText = Color(0xFF1D4ED8),
    cardPurpleBg = Color(0xFFF3E8FF),
    cardPurpleText = Color(0xFF7C3AED),
    badgeGrowingBg = Color(0xFFDCFCE7),
    badgeGrowingText = Color(0xFF15803D),
    badgeSownBg = Color(0xFFE0F2FE),
    badgeSownText = Color(0xFF1D4ED8),
    badgePreparingBg = Color(0xFFFEF3C7),
    badgePreparingText = Color(0xFFB45309),
    textPrimary = Color(0xFF111827),
    textSecondary = Color(0xFF4B5563),
    textMuted = Color(0xFF9CA3AF),
    borderSubtle = Color(0xFFE2E8F0),
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF1F5F9)
)

val DarkCustomColors = AgriXCustomColors(
    cardGreenBg = Color(0xFF0D2A1C),
    cardGreenText = Color(0xFF4ADE80),
    cardOrangeBg = Color(0xFF2A1E0D),
    cardOrangeText = Color(0xFFFB923C),
    cardBlueBg = Color(0xFF0D1E2A),
    cardBlueText = Color(0xFF60A5FA),
    cardPurpleBg = Color(0xFF1E0D2A),
    cardPurpleText = Color(0xFFC084FC),
    badgeGrowingBg = Color(0xFF064E3B),
    badgeGrowingText = Color(0xFF86EFAC),
    badgeSownBg = Color(0xFF1E3A8A),
    badgeSownText = Color(0xFF93C5FD),
    badgePreparingBg = Color(0xFF78350F),
    badgePreparingText = Color(0xFFFDBA74),
    textPrimary = Color(0xFFF8FAFC),
    textSecondary = Color(0xFF94A3B8),
    textMuted = Color(0xFF64748B),
    borderSubtle = Color(0xFF242E32),
    background = Color(0xFF0B1215),
    surface = Color(0xFF171F22),
    surfaceVariant = Color(0xFF242E32)
)

val LocalAgriXCustomColors = staticCompositionLocalOf { LightCustomColors }

@Composable
fun AgriXTheme(
    themeMode: String = "System",
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode.uppercase()) {
        "DARK" -> true
        "LIGHT" -> false
        else -> systemDark
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val customColors = if (darkTheme) DarkCustomColors else LightCustomColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalAgriXCustomColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
