package com.spot.android.core.design.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for Spot theme configuration.
 * 
 * Verifies that the Material 3 theme is properly configured with Spot design tokens.
 */
class SpotThemeTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun `theme uses Spot color scheme`() {
        var capturedScheme: ColorScheme? = null
        
        composeTestRule.setContent {
            // Enable inspection mode to allow composition
            CompositionLocalProvider(LocalInspectionMode provides true) {
                SpotTheme {
                    capturedScheme = androidx.compose.material3.MaterialTheme.colorScheme
                }
            }
        }
        
        composeTestRule.runOnIdle {
            val scheme = capturedScheme!!
            
            // Verify core color mappings from PRD/02
            assertEquals(SpotColors.Primary, scheme.primary, "Theme primary should use SpotColors.Primary")
            assertEquals(SpotColors.ButtonText, scheme.onPrimary, "Theme onPrimary should use SpotColors.ButtonText")
            assertEquals(SpotColors.Background, scheme.background, "Theme background should use SpotColors.Background")
            assertEquals(SpotColors.Primary, scheme.onBackground, "Theme onBackground should use SpotColors.Primary")
            assertEquals(SpotColors.Background, scheme.surface, "Theme surface should use SpotColors.Background")
            assertEquals(SpotColors.Primary, scheme.onSurface, "Theme onSurface should use SpotColors.Primary")
            assertEquals(SpotColors.Accent, scheme.secondary, "Theme secondary should use SpotColors.Accent")
        }
    }
    
    @Test
    fun `theme uses Spot typography`() {
        var typographyMatch = false
        
        composeTestRule.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                SpotTheme {
                    val currentTypography = androidx.compose.material3.MaterialTheme.typography
                    typographyMatch = currentTypography == SpotTypography
                }
            }
        }
        
        composeTestRule.runOnIdle {
            assertTrue("Theme should use SpotTypography", typographyMatch)
        }
    }
}
