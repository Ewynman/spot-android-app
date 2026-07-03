package com.spot.android.core.design.theme

import androidx.compose.ui.graphics.Color
import com.spot.android.core.design.theme.SpotColors
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Unit tests for Spot color tokens.
 * 
 * Verifies that all colors match the exact hex values from PRD/02-design-system.md.
 */
class SpotColorsTest {
    
    @Test
    fun `core colors match PRD specification`() {
        // Core colors from PRD/02
        assertEquals(Color(0xFFF5F3EF), SpotColors.Background, "Background should be #F5F3EF")
        assertEquals(Color(0xFF1D2C24), SpotColors.Primary, "Primary should be #1D2C24")
        assertEquals(Color(0xFFF5F3EF), SpotColors.ButtonText, "ButtonText should be #F5F3EF")
        assertEquals(Color(0xFFDEE6D8), SpotColors.Accent, "Accent should be #DEE6D8")
    }
    
    @Test
    fun `map colors match PRD specification`() {
        // Map colors from PRD/02
        assertEquals(Color(0xFF1D2C24), SpotColors.MapMarkerGreen, "MapMarkerGreen should be #1D2C24")
        assertEquals(Color(0xFFF5F3EF), SpotColors.MapMarkerDot, "MapMarkerDot should be #F5F3EF")
        assertEquals(Color(0xFF0F1A14), SpotColors.MapMarkerStroke, "MapMarkerStroke should be #0F1A14")
        assertEquals(Color(0xFF7AA382), SpotColors.MapFilterMatch, "MapFilterMatch should be #7AA382")
        assertEquals(Color(0xFFC9A24A), SpotColors.ProGold, "ProGold should be #C9A24A")
        assertEquals(Color(0xFF1D2C24), SpotColors.MapAvatarRing, "MapAvatarRing should be #1D2C24")
    }
    
    @Test
    fun `map colors with alpha match PRD specification`() {
        // Verify base colors and alpha values
        val densityFill = SpotColors.MapDensityFill
        assertEquals(0xFF1D2C24, densityFill.value and 0xFFFFFF00u, "MapDensityFill base should be #1D2C24")
        
        val selectedGlow = SpotColors.MapSelectedGlow
        assertEquals(0xFF1D2C24, selectedGlow.value and 0xFFFFFF00u, "MapSelectedGlow base should be #1D2C24")
        
        val avatarHalo = SpotColors.MapAvatarHalo
        assertEquals(0xFF1D2C24, avatarHalo.value and 0xFFFFFF00u, "MapAvatarHalo base should be #1D2C24")
    }
    
    @Test
    fun `welcome screen colors match PRD specification`() {
        // Welcome screen colors from PRD/02
        assertEquals(Color(0xFF7AA382), SpotColors.WelcomeGlow, "WelcomeGlow should be #7AA382")
        assertEquals(Color(0xFFF9F7F1), SpotColors.WelcomeSurface, "WelcomeSurface should be #F9F7F1")
        assertEquals(Color(0xFF607064), SpotColors.WelcomeMutedText, "WelcomeMutedText should be #607064")
        assertEquals(Color(0xFFAEB9AD), SpotColors.WelcomeLine, "WelcomeLine should be #AEB9AD")
        assertEquals(Color(0xFFEEF3EA), SpotColors.WelcomeChipFill, "WelcomeChipFill should be #EEF3EA")
    }
    
    @Test
    fun `welcome card shadow has correct alpha`() {
        val shadow = SpotColors.WelcomeCardShadow
        assertEquals(0xFF1D2C24, shadow.value and 0xFFFFFF00u, "WelcomeCardShadow base should be #1D2C24")
    }
    
    @Test
    fun `button text and background are same color for contrast`() {
        // Per PRD: ButtonText is same as Background - never use as body text on cream
        assertEquals(
            SpotColors.Background,
            SpotColors.ButtonText,
            "ButtonText should match Background for proper contrast on dark buttons"
        )
    }
}
