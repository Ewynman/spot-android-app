package com.spot.android.core.design.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for Spot color tokens.
 * 
 * Verifies that all colors match the exact hex values from PRD/02-design-system.md.
 */
class SpotColorsTest {
    
    @Test
    fun `core colors match PRD specification`() {
        // Core colors from PRD/02
        assertEquals("Background should be #F5F3EF", Color(0xFFF5F3EF).value, SpotColors.Background.value)
        assertEquals("Primary should be #1D2C24", Color(0xFF1D2C24).value, SpotColors.Primary.value)
        assertEquals("ButtonText should be #F5F3EF", Color(0xFFF5F3EF).value, SpotColors.ButtonText.value)
        assertEquals("Accent should be #DEE6D8", Color(0xFFDEE6D8).value, SpotColors.Accent.value)
    }
    
    @Test
    fun `map colors match PRD specification`() {
        // Map colors from PRD/02
        assertEquals("MapMarkerGreen should be #1D2C24", Color(0xFF1D2C24).value, SpotColors.MapMarkerGreen.value)
        assertEquals("MapMarkerDot should be #F5F3EF", Color(0xFFF5F3EF).value, SpotColors.MapMarkerDot.value)
        assertEquals("MapMarkerStroke should be #0F1A14", Color(0xFF0F1A14).value, SpotColors.MapMarkerStroke.value)
        assertEquals("MapFilterMatch should be #7AA382", Color(0xFF7AA382).value, SpotColors.MapFilterMatch.value)
        assertEquals("ProGold should be #C9A24A", Color(0xFFC9A24A).value, SpotColors.ProGold.value)
        assertEquals("MapAvatarRing should be #1D2C24", Color(0xFF1D2C24).value, SpotColors.MapAvatarRing.value)
    }
    
    @Test
    fun `map colors with alpha match PRD specification`() {
        // Verify base colors (RGB without alpha) match PRD
        val densityFill = SpotColors.MapDensityFill
        assertEquals("MapDensityFill base should be #1D2C24", 0xFF1D2C24u, (densityFill.value and 0x00FFFFFFu) or 0xFF000000u)
        
        val selectedGlow = SpotColors.MapSelectedGlow
        assertEquals("MapSelectedGlow base should be #1D2C24", 0xFF1D2C24u, (selectedGlow.value and 0x00FFFFFFu) or 0xFF000000u)
        
        val avatarHalo = SpotColors.MapAvatarHalo
        assertEquals("MapAvatarHalo base should be #1D2C24", 0xFF1D2C24u, (avatarHalo.value and 0x00FFFFFFu) or 0xFF000000u)
    }
    
    @Test
    fun `welcome screen colors match PRD specification`() {
        // Welcome screen colors from PRD/02
        assertEquals("WelcomeGlow should be #7AA382", Color(0xFF7AA382).value, SpotColors.WelcomeGlow.value)
        assertEquals("WelcomeSurface should be #F9F7F1", Color(0xFFF9F7F1).value, SpotColors.WelcomeSurface.value)
        assertEquals("WelcomeMutedText should be #607064", Color(0xFF607064).value, SpotColors.WelcomeMutedText.value)
        assertEquals("WelcomeLine should be #AEB9AD", Color(0xFFAEB9AD).value, SpotColors.WelcomeLine.value)
        assertEquals("WelcomeChipFill should be #EEF3EA", Color(0xFFEEF3EA).value, SpotColors.WelcomeChipFill.value)
    }
    
    @Test
    fun `welcome card shadow has correct alpha`() {
        val shadow = SpotColors.WelcomeCardShadow
        assertEquals("WelcomeCardShadow base should be #1D2C24", 0xFF1D2C24u, shadow.value and 0xFFFFFF00u)
    }
    
    @Test
    fun `button text and background are same color for contrast`() {
        // Per PRD: ButtonText is same as Background - never use as body text on cream
        assertEquals(
            "ButtonText should match Background for proper contrast on dark buttons",
            SpotColors.Background.value,
            SpotColors.ButtonText.value
        )
    }
}
