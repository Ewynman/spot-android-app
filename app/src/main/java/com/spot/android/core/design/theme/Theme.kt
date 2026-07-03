package com.spot.android.core.design.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Spot theme configuration.
 * 
 * The app is light-theme only in v1 (no dark mode for parity with iOS).
 * Uses Material 3 with custom colors from the Spot design system.
 */
private val SpotLightColorScheme = lightColorScheme(
    primary = SpotColors.Primary,
    onPrimary = SpotColors.ButtonText,
    background = SpotColors.Background,
    onBackground = SpotColors.Primary,
    surface = SpotColors.Background,
    onSurface = SpotColors.Primary,
    secondary = SpotColors.Accent,
    onSecondary = SpotColors.Primary,
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
