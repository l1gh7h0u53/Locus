package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Standard light color scheme (Expressive)
private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceContainerLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    onBackground = OnBackgroundLight,
    onSurface = OnSurfaceLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight
)

// Standard dark color scheme (Expressive)
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceContainerDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    onBackground = OnBackgroundDark,
    onSurface = OnSurfaceDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark
)

// Senior High Contrast Light (Expressive Senior Mode)
private val SeniorLightColorScheme = lightColorScheme(
    primary = SeniorPrimaryLight,
    onPrimary = SeniorBackgroundLight,
    primaryContainer = SeniorSurfaceContainerLight,
    onPrimaryContainer = SeniorOnBackgroundLight,
    secondary = SeniorSecondaryLight,
    onSecondary = SeniorBackgroundLight,
    secondaryContainer = SeniorSurfaceContainerLight,
    onSecondaryContainer = SeniorOnBackgroundLight,
    tertiary = SeniorPrimaryLight,
    onTertiary = SeniorBackgroundLight,
    tertiaryContainer = SeniorSurfaceContainerLight,
    onTertiaryContainer = SeniorOnBackgroundLight,
    background = SeniorBackgroundLight,
    surface = SeniorSurfaceLight,
    surfaceVariant = SeniorSurfaceContainerLight,
    surfaceContainer = SeniorSurfaceContainerLight,
    onBackground = SeniorOnBackgroundLight,
    onSurface = SeniorOnBackgroundLight,
    onSurfaceVariant = SeniorOnBackgroundLight,
    outline = SeniorOutlineLight,
    outlineVariant = SeniorOutlineLight
)

// Senior High Contrast Dark (Expressive Senior Mode)
private val SeniorDarkColorScheme = darkColorScheme(
    primary = SeniorPrimaryDark,
    onPrimary = SeniorBackgroundDark,
    primaryContainer = SeniorSurfaceContainerDark,
    onPrimaryContainer = SeniorOnBackgroundDark,
    secondary = SeniorSecondaryDark,
    onSecondary = SeniorBackgroundDark,
    secondaryContainer = SeniorSurfaceContainerDark,
    onSecondaryContainer = SeniorOnBackgroundDark,
    tertiary = SeniorPrimaryDark,
    onTertiary = SeniorBackgroundDark,
    tertiaryContainer = SeniorSurfaceContainerDark,
    onTertiaryContainer = SeniorOnBackgroundDark,
    background = SeniorBackgroundDark,
    surface = SeniorSurfaceDark,
    surfaceVariant = SeniorSurfaceContainerDark,
    surfaceContainer = SeniorSurfaceContainerDark,
    onBackground = SeniorOnBackgroundDark,
    onSurface = SeniorOnSurfaceDark,
    onSurfaceVariant = SeniorOnBackgroundDark,
    outline = SeniorOutlineDark,
    outlineVariant = SeniorOutlineDark
)

// Helper extension to darken colors dynamically for lighter backgrounds to maintain crisp readability
private fun androidx.compose.ui.graphics.Color.darken(factor: Float = 0.7f): androidx.compose.ui.graphics.Color {
    return androidx.compose.ui.graphics.Color(
        red = this.red * factor,
        green = this.green * factor,
        blue = this.blue * factor,
        alpha = this.alpha
    )
}

/**
 * =========================================================================================
 * FEATURE: Material 3 Expressive Application Theme (TippyTheme)
 * =========================================================================================
 *
 * Description:
 * Central Material 3 theming composable applying colors, typography, and elevation scales.
 * Dynamically switches between four distinct color palettes:
 * 1. Senior High-Contrast Dark: Pure black background with stark white and vibrant accent colors.
 * 2. Senior High-Contrast Light: High-contrast black text on pure white surfaces.
 * 3. Dynamic M3 (Android 12+ / API 31+): Monet system wallpaper-extracted dynamic palette.
 * 4. Expressive M3 Static Fallback: Hand-tuned light and dark palettes with optimal tonal contrast.
 *
 * Typography Adaptation:
 * - When [isSeniorMode] is true, swaps standard typography for [SeniorTypography] featuring
 *   drastically increased font sizes (+20-30%) and bold visual weights for elder accessibility.
 *
 * @param darkTheme Whether dark theme should be rendered (defaults to system setting).
 * @param isSeniorMode Accessibility flag activating enlarged fonts and high-contrast colors.
 * @param dynamicColor Whether Android 12+ dynamic wallpaper color should be preferred.
 * @param content Composable UI tree wrapped within this theme.
 * =========================================================================================
 */
@Composable
fun TippyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isSeniorMode: Boolean = false,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        isSeniorMode -> {
            if (darkTheme) SeniorDarkColorScheme else SeniorLightColorScheme
        }
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            val baseScheme = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            if (darkTheme) {
                baseScheme.copy(
                    tertiary = TertiaryDark,
                    tertiaryContainer = TertiaryContainerDark,
                    onTertiaryContainer = OnTertiaryContainerDark,
                    onSurfaceVariant = OnSurfaceVariantDark,
                    onSurface = OnSurfaceDark
                )
            } else {
                baseScheme.copy(
                    primary = baseScheme.primary.darken(0.75f),
                    secondary = baseScheme.secondary.darken(0.75f),
                    outline = baseScheme.outline.darken(0.75f)
                )
            }
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Adapt typography scale based on senior state
    val chosenTypography = if (isSeniorMode) {
        androidx.compose.material3.Typography(
            displayLarge = SeniorTypography.displayLarge,
            displayMedium = SeniorTypography.displayMedium,
            displaySmall = SeniorTypography.displaySmall,
            headlineLarge = SeniorTypography.headlineLarge,
            headlineMedium = SeniorTypography.headlineMedium,
            headlineSmall = SeniorTypography.headlineSmall,
            titleLarge = SeniorTypography.titleLarge,
            titleMedium = SeniorTypography.titleMedium,
            titleSmall = SeniorTypography.titleSmall,
            bodyLarge = SeniorTypography.bodyLarge,
            bodyMedium = SeniorTypography.bodyMedium,
            bodySmall = SeniorTypography.bodySmall,
            labelLarge = SeniorTypography.labelLarge,
            labelMedium = SeniorTypography.labelMedium,
            labelSmall = SeniorTypography.labelSmall
        )
    } else {
        Typography
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = chosenTypography,
        content = content
    )
}

