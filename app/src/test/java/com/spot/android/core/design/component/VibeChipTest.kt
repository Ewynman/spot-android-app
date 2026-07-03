package com.spot.android.core.design.component

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.spot.android.core.design.theme.SpotTheme
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * UI tests for VibeChip component.
 * 
 * Verifies visual rendering and interaction behavior.
 */
class VibeChipTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun `VibeChipDisplay renders text correctly`() {
        composeTestRule.setContent {
            SpotTheme {
                VibeChipDisplay(text = "Chill Spot", testTag = "test.chip")
            }
        }
        
        composeTestRule
            .onNodeWithTag("test.chip")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Chill Spot")
            .assertIsDisplayed()
    }
    
    @Test
    fun `VibeChipDisplay is not clickable`() {
        composeTestRule.setContent {
            SpotTheme {
                VibeChipDisplay(text = "Hidden Gem", testTag = "test.chip")
            }
        }
        
        composeTestRule
            .onNodeWithTag("test.chip")
            .assertHasNoClickAction()
    }
    
    @Test
    fun `VibeChipSelectable renders text correctly`() {
        composeTestRule.setContent {
            SpotTheme {
                VibeChipSelectable(
                    text = "Scenic View",
                    selected = false,
                    onSelectedChange = {},
                    testTag = "test.chip"
                )
            }
        }
        
        composeTestRule
            .onNodeWithTag("test.chip")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Scenic View")
            .assertIsDisplayed()
    }
    
    @Test
    fun `VibeChipSelectable is clickable`() {
        composeTestRule.setContent {
            SpotTheme {
                VibeChipSelectable(
                    text = "Romantic",
                    selected = false,
                    onSelectedChange = {},
                    testTag = "test.chip"
                )
            }
        }
        
        composeTestRule
            .onNodeWithTag("test.chip")
            .assertHasClickAction()
    }
    
    @Test
    fun `VibeChipSelectable toggles selection on click`() {
        var isSelected = false
        
        composeTestRule.setContent {
            SpotTheme {
                VibeChipSelectable(
                    text = "Great For Photos",
                    selected = isSelected,
                    onSelectedChange = { isSelected = it },
                    testTag = "test.chip"
                )
            }
        }
        
        assertFalse(isSelected, "Should start unselected")
        
        composeTestRule
            .onNodeWithTag("test.chip")
            .performClick()
        
        composeTestRule.runOnIdle {
            assertTrue(isSelected, "Should be selected after click")
        }
    }
    
    @Test
    fun `VibeChipSelectable can be unselected`() {
        var isSelected = true
        
        composeTestRule.setContent {
            SpotTheme {
                VibeChipSelectable(
                    text = "Family Friendly",
                    selected = isSelected,
                    onSelectedChange = { isSelected = it },
                    testTag = "test.chip"
                )
            }
        }
        
        assertTrue(isSelected, "Should start selected")
        
        composeTestRule
            .onNodeWithTag("test.chip")
            .performClick()
        
        composeTestRule.runOnIdle {
            assertFalse(isSelected, "Should be unselected after click")
        }
    }
    
    @Test
    fun `VibeChip handles long text with ellipsis`() {
        val longText = "This is a very long vibe tag that should be truncated with ellipsis"
        
        composeTestRule.setContent {
            SpotTheme {
                VibeChipDisplay(
                    text = longText,
                    testTag = "test.chip"
                )
            }
        }
        
        // Chip should still render without crashing
        composeTestRule
            .onNodeWithTag("test.chip")
            .assertIsDisplayed()
    }
    
    @Test
    fun `VibeChip respects custom test tags`() {
        composeTestRule.setContent {
            SpotTheme {
                VibeChipDisplay(
                    text = "Nature Escape",
                    testTag = "custom.test.tag"
                )
            }
        }
        
        composeTestRule
            .onNodeWithTag("custom.test.tag")
            .assertIsDisplayed()
    }
    
    @Test
    fun `multiple VibeChips can be rendered together`() {
        composeTestRule.setContent {
            SpotTheme {
                androidx.compose.foundation.layout.Row {
                    VibeChipDisplay(text = "Chill Spot", testTag = "chip1")
                    VibeChipDisplay(text = "Hidden Gem", testTag = "chip2")
                    VibeChipDisplay(text = "Scenic View", testTag = "chip3")
                }
            }
        }
        
        composeTestRule.onNodeWithTag("chip1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("chip2").assertIsDisplayed()
        composeTestRule.onNodeWithTag("chip3").assertIsDisplayed()
    }
}
