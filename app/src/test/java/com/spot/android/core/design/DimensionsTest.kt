package com.spot.android.core.design

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for Spot layout dimensions.
 * 
 * Verifies that all spacing, padding, and radius values match PRD/02-design-system.md.
 */
class DimensionsTest {
    
    @Test
    fun `padding values match PRD specification`() {
        // Padding from PRD/02
        assertEquals("Horizontal padding should be 32dp", 32f, Dimensions.Padding.horizontal.value, 0.001f)
        assertEquals("Vertical small padding should be 8dp", 8f, Dimensions.Padding.verticalSmall.value, 0.001f)
        assertEquals("Vertical medium padding should be 12dp", 12f, Dimensions.Padding.verticalMedium.value, 0.001f)
        assertEquals("Vertical large padding should be 16dp", 16f, Dimensions.Padding.verticalLarge.value, 0.001f)
        assertEquals("Vertical XL padding should be 24dp", 24f, Dimensions.Padding.verticalXL.value, 0.001f)
    }
    
    @Test
    fun `spacing values match PRD specification`() {
        // Spacing from PRD/02
        assertEquals("Small spacing should be 8dp", 8f, Dimensions.Spacing.small.value, 0.001f)
        assertEquals("Medium spacing should be 12dp", 12f, Dimensions.Spacing.medium.value, 0.001f)
        assertEquals("Large spacing should be 16dp", 16f, Dimensions.Spacing.large.value, 0.001f)
        assertEquals("XL spacing should be 24dp", 24f, Dimensions.Spacing.xl.value, 0.001f)
    }
    
    @Test
    fun `radius values match PRD specification`() {
        // Corner radius from PRD/02
        assertEquals("Small radius should be 10dp", 10f, Dimensions.Radius.small.value, 0.001f)
        assertEquals("Medium radius should be 12dp", 12f, Dimensions.Radius.medium.value, 0.001f)
        assertEquals("Large radius (vibe chips) should be 20dp", 20f, Dimensions.Radius.large.value, 0.001f)
    }
    
    @Test
    fun `vertical padding scales consistently`() {
        // Verify the progression: 8, 12, 16, 24
        assertTrue(
            "Vertical small should be less than vertical medium",
            Dimensions.Padding.verticalSmall < Dimensions.Padding.verticalMedium
        )
        assertTrue(
            "Vertical medium should be less than vertical large",
            Dimensions.Padding.verticalMedium < Dimensions.Padding.verticalLarge
        )
        assertTrue(
            "Vertical large should be less than vertical XL",
            Dimensions.Padding.verticalLarge < Dimensions.Padding.verticalXL
        )
    }
    
    @Test
    fun `spacing and vertical padding use same values`() {
        // Per PRD, these should be aligned
        assertEquals("Small values should match", 
            Dimensions.Padding.verticalSmall.value, 
            Dimensions.Spacing.small.value, 
            0.001f)
        assertEquals("Medium values should match",
            Dimensions.Padding.verticalMedium.value, 
            Dimensions.Spacing.medium.value, 
            0.001f)
        assertEquals("Large values should match",
            Dimensions.Padding.verticalLarge.value, 
            Dimensions.Spacing.large.value, 
            0.001f)
        assertEquals("XL values should match",
            Dimensions.Padding.verticalXL.value, 
            Dimensions.Spacing.xl.value, 
            0.001f)
    }
}
