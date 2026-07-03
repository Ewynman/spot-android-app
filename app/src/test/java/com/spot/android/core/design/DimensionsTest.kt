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
        assertEquals(32.dp, Dimensions.Padding.horizontal, "Horizontal padding should be 32dp")
        assertEquals(8.dp, Dimensions.Padding.verticalSmall, "Vertical small padding should be 8dp")
        assertEquals(12.dp, Dimensions.Padding.verticalMedium, "Vertical medium padding should be 12dp")
        assertEquals(16.dp, Dimensions.Padding.verticalLarge, "Vertical large padding should be 16dp")
        assertEquals(24.dp, Dimensions.Padding.verticalXL, "Vertical XL padding should be 24dp")
    }
    
    @Test
    fun `spacing values match PRD specification`() {
        // Spacing from PRD/02
        assertEquals(8.dp, Dimensions.Spacing.small, "Small spacing should be 8dp")
        assertEquals(12.dp, Dimensions.Spacing.medium, "Medium spacing should be 12dp")
        assertEquals(16.dp, Dimensions.Spacing.large, "Large spacing should be 16dp")
        assertEquals(24.dp, Dimensions.Spacing.xl, "XL spacing should be 24dp")
    }
    
    @Test
    fun `radius values match PRD specification`() {
        // Corner radius from PRD/02
        assertEquals(10.dp, Dimensions.Radius.small, "Small radius should be 10dp")
        assertEquals(12.dp, Dimensions.Radius.medium, "Medium radius should be 12dp")
        assertEquals(20.dp, Dimensions.Radius.large, "Large radius (vibe chips) should be 20dp")
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
        assertEquals(Dimensions.Padding.verticalSmall, Dimensions.Spacing.small)
        assertEquals(Dimensions.Padding.verticalMedium, Dimensions.Spacing.medium)
        assertEquals(Dimensions.Padding.verticalLarge, Dimensions.Spacing.large)
        assertEquals(Dimensions.Padding.verticalXL, Dimensions.Spacing.xl)
    }
}
