package com.spot.android.core.design.theme

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Unit tests for Spot typography configuration.
 * 
 * Verifies that typography roles are properly configured for Material 3.
 */
class SpotTypographyTest {
    
    @Test
    fun `display large is configured for SPOT wordmark`() {
        val displayLarge = SpotTypography.displayLarge
        assertNotNull(displayLarge, "Display large should be configured")
        assertEquals(FontWeight.Bold, displayLarge.fontWeight, "Display large should be bold for wordmark")
        assertEquals(32.sp, displayLarge.fontSize, "Display large should use appropriate size")
    }
    
    @Test
    fun `title styles are configured for headers`() {
        assertNotNull(SpotTypography.titleLarge, "Title large should be configured")
        assertNotNull(SpotTypography.titleMedium, "Title medium should be configured")
        assertEquals(FontWeight.Bold, SpotTypography.titleLarge.fontWeight)
        assertEquals(FontWeight.SemiBold, SpotTypography.titleMedium.fontWeight)
    }
    
    @Test
    fun `body styles are configured for general text`() {
        assertNotNull(SpotTypography.bodyLarge, "Body large should be configured")
        assertNotNull(SpotTypography.bodyMedium, "Body medium should be configured")
        assertNotNull(SpotTypography.bodySmall, "Body small should be configured")
        
        // Body text should be normal weight
        assertEquals(FontWeight.Normal, SpotTypography.bodyLarge.fontWeight)
        assertEquals(FontWeight.Normal, SpotTypography.bodyMedium.fontWeight)
        assertEquals(FontWeight.Normal, SpotTypography.bodySmall.fontWeight)
    }
    
    @Test
    fun `label styles are configured for vibe chips and buttons`() {
        assertNotNull(SpotTypography.labelLarge, "Label large should be configured")
        assertNotNull(SpotTypography.labelMedium, "Label medium should be configured")
        assertNotNull(SpotTypography.labelSmall, "Label small should be configured")
        
        // Labels should be medium weight
        assertEquals(FontWeight.Medium, SpotTypography.labelLarge.fontWeight)
        assertEquals(FontWeight.Medium, SpotTypography.labelMedium.fontWeight)
        assertEquals(FontWeight.Medium, SpotTypography.labelSmall.fontWeight)
    }
    
    @Test
    fun `font sizes decrease from large to small`() {
        // Body sizes
        assert(SpotTypography.bodyLarge.fontSize > SpotTypography.bodyMedium.fontSize)
        assert(SpotTypography.bodyMedium.fontSize > SpotTypography.bodySmall.fontSize)
        
        // Label sizes
        assert(SpotTypography.labelLarge.fontSize > SpotTypography.labelMedium.fontSize)
        assert(SpotTypography.labelMedium.fontSize > SpotTypography.labelSmall.fontSize)
    }
    
    @Test
    fun `label medium is appropriate for vibe chips`() {
        // Per PRD, vibe chips use label medium
        val labelMedium = SpotTypography.labelMedium
        assertEquals(12.sp, labelMedium.fontSize, "Label medium should be 12sp for vibe chips")
        assertEquals(FontWeight.Medium, labelMedium.fontWeight)
    }
}
