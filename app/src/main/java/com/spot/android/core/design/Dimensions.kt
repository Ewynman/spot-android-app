package com.spot.android.core.design

import androidx.compose.ui.unit.dp

/**
 * Spot Design System Layout Tokens
 * 
 * Ported from iOS Constants.Layout (values in points → dp).
 * These values must remain consistent across both platforms.
 * 
 * Reference: PRD/02-design-system.md
 */
object Dimensions {
    
    // Padding
    object Padding {
        val horizontal = 32.dp
        val verticalSmall = 8.dp
        val verticalMedium = 12.dp
        val verticalLarge = 16.dp
        val verticalXL = 24.dp
    }
    
    // Spacing
    object Spacing {
        val small = 8.dp
        val medium = 12.dp
        val large = 16.dp
        val xl = 24.dp
    }
    
    // Corner Radius
    object Radius {
        val small = 10.dp
        val medium = 12.dp
        val large = 20.dp
    }
}
