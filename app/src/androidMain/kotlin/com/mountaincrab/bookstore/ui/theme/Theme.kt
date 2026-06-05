package com.mountaincrab.bookstore.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

// ── Theme options (match the design system's colors_and_type.css) ────────────
enum class AppTheme(val displayName: String) {
    DEEP_NAVY("Deep Navy"),
    CHARCOAL("Charcoal"),
    RETRO("Retro");

    companion object {
        fun fromName(name: String?): AppTheme =
            entries.firstOrNull { it.name == name } ?: DEEP_NAVY
    }
}

// ── Brand-constant accent palette ────────────────────────────────────────────
val AccentBlue = Color(0xFF4F7CFF)
val AccentPurple = Color(0xFF8B5CF6)
val AccentCyan = Color(0xFF06B6D4)
val AccentRed = Color(0xFFEF4444)
val AccentGreen = Color(0xFF10B981)

/**
 * Extra semantic tokens not covered by Material's ColorScheme — the design
 * system's --surface-high, --accent-text, --success-text, etc.
 */
data class AppPalette(
    val surfaceRaised: Color,   // --surface-raised: cards, list rows
    val surfaceHigh: Color,     // --surface-high: chips, inputs, hover
    val accentText: Color,      // --accent-text: lighter inline accent
    val accentSoft: Color,      // --accent-soft: active pills
    val successText: Color,     // --success-text: Read badge text
    val successSoft: Color,     // soft green fill behind Read badge
    val cardBorder: Color,      // --border
    val fgMuted: Color,         // --fg-muted
    val fgFaint: Color,         // --fg-faint
)

val LocalAppPalette = compositionLocalOf {
    AppPalette(
        surfaceRaised = Color(0xFF1C2340),
        surfaceHigh = Color(0xFF2A3250),
        accentText = Color(0xFFA5B4FC),
        accentSoft = Color(0x2E4F7CFF),
        successText = Color(0xFF34D399),
        successSoft = Color(0x2910B981),
        cardBorder = Color(0x1AFFFFFF),
        fgMuted = Color(0xFF9CA3AF),
        fgFaint = Color(0xFF64748B),
    )
}

// ── Color schemes ────────────────────────────────────────────────────────────
private fun buildScheme(
    primary: Color,
    secondary: Color,
    background: Color,
    surface: Color,
    surfaceVariant: Color,
    outline: Color,
) = darkColorScheme(
    primary = primary,
    onPrimary = Color.White,
    primaryContainer = primary.copy(alpha = 0.18f),
    onPrimaryContainer = Color.White,
    secondary = secondary,
    onSecondary = Color.White,
    secondaryContainer = secondary.copy(alpha = 0.18f),
    onSecondaryContainer = Color.White,
    tertiary = AccentGreen,
    onTertiary = Color.White,
    error = AccentRed,
    onError = Color.White,
    background = background,
    onBackground = Color(0xFFF3F4F6),
    surface = surface,
    onSurface = Color(0xFFF3F4F6),
    surfaceVariant = surfaceVariant,
    onSurfaceVariant = Color(0xFF9CA3AF),
    outline = outline,
    outlineVariant = outline.copy(alpha = 0.4f),
)

// Deep Navy (default) — tokens straight from colors_and_type.css [data-theme="deep-navy"]
private val DeepNavyScheme = buildScheme(
    primary = AccentBlue,
    secondary = AccentPurple,
    background = Color(0xFF0A1020),
    surface = Color(0xFF131A2E),
    surfaceVariant = Color(0xFF1C2340),
    outline = Color(0xFF2A3250),
)
private val DeepNavyPalette = AppPalette(
    surfaceRaised = Color(0xFF1C2340),
    surfaceHigh = Color(0xFF2A3250),
    accentText = Color(0xFFA5B4FC),
    accentSoft = Color(0x2E4F7CFF),
    successText = Color(0xFF34D399),
    successSoft = Color(0x2910B981),
    cardBorder = Color(0x1AFFFFFF),
    fgMuted = Color(0xFF9CA3AF),
    fgFaint = Color(0xFF64748B),
)

// Charcoal — neutral dark, cyan accent
private val CharcoalScheme = buildScheme(
    primary = AccentCyan,
    secondary = Color(0xFF3B82F6),
    background = Color(0xFF0A0A0A),
    surface = Color(0xFF141414),
    surfaceVariant = Color(0xFF1E1E1E),
    outline = Color(0xFF2A2A2A),
)
private val CharcoalPalette = AppPalette(
    surfaceRaised = Color(0xFF1E1E1E),
    surfaceHigh = Color(0xFF2A2A2A),
    accentText = Color(0xFF67E8F9),
    accentSoft = Color(0x2E06B6D4),
    successText = Color(0xFF34D399),
    successSoft = Color(0x2910B981),
    cardBorder = Color(0x1AFFFFFF),
    fgMuted = Color(0xFFA1A1AA),
    fgFaint = Color(0xFF71717A),
)

// Retro — vaporwave magenta on deep purple
private val RetroMagenta = Color(0xFFFF00CC)
private val RetroScheme = darkColorScheme(
    primary = RetroMagenta,
    onPrimary = Color.Black,
    primaryContainer = RetroMagenta.copy(alpha = 0.20f),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF00FFEE),
    onSecondary = Color.Black,
    tertiary = Color(0xFFFFEE00),
    onTertiary = Color.Black,
    error = Color(0xFFFF4400),
    onError = Color.White,
    background = Color(0xFF1A0B1E),
    onBackground = Color.White,
    surface = Color(0xFF29153A),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2E1438),
    onSurfaceVariant = Color(0xFFDCC4EC),
    outline = Color(0x38FF00CC),
    outlineVariant = Color(0x1AFF00CC),
)
private val RetroPalette = AppPalette(
    surfaceRaised = Color(0xFF2E1438),
    surfaceHigh = Color(0xFF3F1F50),
    accentText = Color(0xFFFF66DD),
    accentSoft = Color(0x2EFF00CC),
    successText = Color(0xFF66FFF5),
    successSoft = Color(0x2900FFEE),
    cardBorder = Color(0x38FF00CC),
    fgMuted = Color(0xFFDCC4EC),
    fgFaint = Color(0xFFA584C0),
)

private fun paletteFor(theme: AppTheme): AppPalette = when (theme) {
    AppTheme.DEEP_NAVY -> DeepNavyPalette
    AppTheme.CHARCOAL -> CharcoalPalette
    AppTheme.RETRO -> RetroPalette
}

// ── Typography — bolder than Material defaults, per the design system ────────
private val BookTypography = Typography(
    headlineLarge = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp),
    headlineSmall = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
    titleSmall = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal),
    bodySmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal),
    labelLarge = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
    labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp),
)

@Composable
fun BookTheme(
    appTheme: AppTheme = AppTheme.DEEP_NAVY,
    content: @Composable () -> Unit,
) {
    val colorScheme = when (appTheme) {
        AppTheme.DEEP_NAVY -> DeepNavyScheme
        AppTheme.CHARCOAL -> CharcoalScheme
        AppTheme.RETRO -> RetroScheme
    }
    val palette = paletteFor(appTheme)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    CompositionLocalProvider(LocalAppPalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = BookTypography,
            content = content,
        )
    }
}
