package com.spot.android.core.design.theme

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for Spot typography configuration.
 * 
 * Verifies that typography roles are properly configured for Material 3.
 */
class SpotTypographyTest {
    
    @Test
    fun `display large is configured for SPOT wordmark`() {
        val displayLarge = SpotTypography.displayLarge
        assertNotNull("Display large should be configured", displayLarge)
        assertEquals("Display large should be bold for wordmark", FontWeight.Bold.weight, displayLarge.fontWeight!!.weight)
        assertEquals("Display large should use appropriate size", 32f, displayLarge.fontSize.value, 0.001f)
    }
    
    @Test
    fun `title styles are configured for headers`() {
        assertNotNull("Title large should be configured", SpotTypography.titleLarge)
        assertNotNull("Title medium should be configured", SpotTypography.titleMedium)
        assertEquals(FontWeight.Bold.weight, SpotTypography.titleLarge.fontWeight!!.weight)
        assertEquals(FontWeight.SemiBold.weight, SpotTypography.titleMedium.fontWeight!!.weight)
    }
    
    @Test
    fun `body styles are configured for general text`() {
        assertNotNull("Body large should be configured", SpotTypography.bodyLarge)
        assertNotNull("Body medium should be configured", SpotTypography.bodyMedium)
        assertNotNull("Body small should be configured", SpotTypography.bodySmall)
        
        // Body text should be normal weight
        assertEquals(FontWeight.Normal.weight, SpotTypography.bodyLarge.fontWeight!!.weight)
        assertEquals(FontWeight.Normal.weight, SpotTypography.bodyMedium.fontWeight!!.weight)
        assertEquals(FontWeight.Normal.weight, SpotTypography.bodySmall.fontWeight!!.weight)
    }
    
    @Test
    fun `label styles are configured for vibe chips and buttons`() {
        assertNotNull("Label large should be configured", SpotTypography.labelLarge)
        assertNotNull("Label medium should be configured", SpotTypography.labelMedium)
        assertNotNull("Label small should be configured", SpotTypography.labelSmall)
        
        // Labels should be medium weight
        assertEquals(FontWeight.Medium.weight, SpotTypography.labelLarge.fontWeight!!.weight)
        assertEquals(FontWeight.Medium.weight, SpotTypography.labelMedium.fontWeight!!.weight)
        assertEquals(FontWeight.Medium.weight, SpotTypography.labelSmall.fontWeight!!.weight)
    }
    
    @Test
    fun `font sizes decrease from large to small`() {
        // Body sizes
        assertTrue(
            "Body large should be larger than body medium",
            SpotTypography.bodyLarge.fontSize > SpotTypography.bodyMedium.fontSize
        )
        assertTrue(
            "Body medium should be larger than body small",
            SpotTypography.bodyMedium.fontSize > SpotTypography.bodySmall.fontSize
        )
        
        // Label sizes
        assertTrue(
            "Label large should be larger than label medium",
            SpotTypography.labelLarge.fontSize > SpotTypography.labelMedium.fontSize
        )
        assertTrue(
            "Label medium should be larger than label small",
            SpotTypography.labelMedium.fontSize > SpotTypography.labelSmall.fontSize
        )
    }
    
    @Test
    fun `label medium is appropriate for vibe chips`() {
        // Per PRD, vibe chips use label medium
        val labelMedium = SpotTypography.labelMedium
        assertEquals("Label medium should be 12sp for vibe chips", 12f, labelMedium.fontSize.value, 0.001f)
        assertEquals(FontWeight.Medium.weight, labelMedium.fontWeight!!.weight)
    }
}
