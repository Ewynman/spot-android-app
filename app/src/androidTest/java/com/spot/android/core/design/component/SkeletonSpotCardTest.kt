package com.spot.android.core.design.component

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.spot.android.core.design.theme.SpotTheme
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented tests for SkeletonSpotCard component.
 */
class SkeletonSpotCardTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun skeletonSpotCard_displays() {
        composeTestRule.setContent {
            SpotTheme {
                SkeletonSpotCard()
            }
        }
        
        composeTestRule
            .onNodeWithTag("skeletonSpotCard")
            .assertIsDisplayed()
    }
    
    @Test
    fun skeletonFeed_displaysThreeCards() {
        composeTestRule.setContent {
            SpotTheme {
                SkeletonFeed()
            }
        }
        
        composeTestRule
            .onNodeWithTag("skeletonFeed")
            .assertIsDisplayed()
    }
}
