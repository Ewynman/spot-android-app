package com.spot.android.core.design.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Spot theme configuration.
 *
 * Light-theme only in v1 (parity with iOS). Full M3 roles map to Spot tokens so
 * screens that use MaterialTheme.colorScheme never leak default purple/gray.
 */
private val SpotLightColorScheme = lightColorScheme(
    primary = SpotColors.Primary,
    onPrimary = SpotColors.ButtonText,
    primaryContainer = SpotColors.WelcomeChipFill,
    onPrimaryContainer = SpotColors.Primary,
    secondary = SpotColors.Accent,
    onSecondary = SpotColors.Primary,
    secondaryContainer = SpotColors.WelcomeChipFill,
    onSecondaryContainer = SpotColors.Primary,
    tertiary = SpotColors.WelcomeGlow,
    onTertiary = SpotColors.ButtonText,
    tertiaryContainer = SpotColors.WelcomeChipFill,
    onTertiaryContainer = SpotColors.Primary,
    background = SpotColors.Background,
    onBackground = SpotColors.Primary,
    surface = SpotColors.Background,
    onSurface = SpotColors.Primary,
    surfaceVariant = SpotColors.WelcomeSurface,
    onSurfaceVariant = SpotColors.WelcomeMutedText,
    surfaceTint = SpotColors.Primary,
    inverseSurface = SpotColors.Primary,
    inverseOnSurface = SpotColors.ButtonText,
    inversePrimary = SpotColors.Accent,
    outline = SpotColors.WelcomeLine,
    outlineVariant = SpotColors.Accent,
    scrim = Color.Black.copy(alpha = 0.32f),
    error = Color(0xFFB3261E),
    onError = SpotColors.ButtonText,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
)

@Composable
fun SpotTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SpotLightColorScheme,
        typography = SpotTypography,
        content = content
    )
}
