package com.spot.android.core.design.theme

import androidx.compose.ui.graphics.Color

/**
 * Spot Design System Colors
 * 
 * Color palette ported 1:1 from iOS. Spot's look is premium, minimal, calm — cream
 * background, deep forest-green primary, one soft-green accent reserved for vibe tags.
 * 
 * Reference: PRD/02-design-system.md
 */
object SpotColors {
    
    // Core Colors
    val Background = Color(0xFFF5F3EF)  // Cream - main app background
    val Primary = Color(0xFF1D2C24)     // Deep forest green - buttons, icons, body text
    val ButtonText = Color(0xFFF5F3EF)  // Label on dark/primary fills (same as background)
    val Accent = Color(0xFFDEE6D8)      // Soft green - vibe-tag surfaces ONLY
    
    // Map Colors
    val MapMarkerGreen = Color(0xFF1D2C24)
    val MapMarkerDot = Color(0xFFF5F3EF)
    val MapMarkerStroke = Color(0xFF0F1A14)
    val MapDensityFill = Color(0xFF1D2C24).copy(alpha = 0.85f)
    val MapFilterMatch = Color(0xFF7AA382)
    val MapSelectedGlow = Color(0xFF1D2C24).copy(alpha = 0.20f)
    val ProGold = Color(0xFFC9A24A)
    val MapAvatarRing = Color(0xFF1D2C24)
    val MapAvatarHalo = Color(0xFF1D2C24).copy(alpha = 0.18f)
    
    // Welcome Screen Colors
    val WelcomeGlow = Color(0xFF7AA382)
    val WelcomeSurface = Color(0xFFF9F7F1)
    val WelcomeMutedText = Color(0xFF607064)
    val WelcomeLine = Color(0xFFAEB9AD)
    val WelcomeChipFill = Color(0xFFEEF3EA)
    val WelcomeCardShadow = Color(0xFF1D2C24).copy(alpha = 0.12f)
}
