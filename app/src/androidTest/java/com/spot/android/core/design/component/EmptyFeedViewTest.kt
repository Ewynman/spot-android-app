package com.spot.android.core.design.component

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.spot.android.core.design.theme.SpotTheme
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented tests for EmptyFeedView component.
 */
class EmptyFeedViewTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun emptyFeedView_displaysTitle() {
        composeTestRule.setContent {
            SpotTheme {
                EmptyFeedView(
                    title = "Test Title",
                    subtitle = "Test Subtitle"
                )
            }
        }
        
        composeTestRule
            .onNodeWithText("Test Title")
            .assertIsDisplayed()
    }
    
    @Test
    fun emptyFeedView_displaysSubtitle() {
        composeTestRule.setContent {
            SpotTheme {
                EmptyFeedView(
                    title = "Test Title",
                    subtitle = "Test Subtitle"
                )
            }
        }
        
        composeTestRule
            .onNodeWithText("Test Subtitle")
            .assertIsDisplayed()
    }
    
    @Test
    fun emptyFeedCaughtUp_displaysCorrectCopy() {
        composeTestRule.setContent {
            SpotTheme {
                EmptyFeedCaughtUp()
            }
        }
        
        composeTestRule
            .onNodeWithText("You're all caught up")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Pull to refresh or follow more people")
            .assertIsDisplayed()
    }
    
    @Test
    fun emptyFeedNoEligibleSpots_displaysCorrectCopy() {
        composeTestRule.setContent {
            SpotTheme {
                EmptyFeedNoEligibleSpots()
            }
        }
        
        composeTestRule
            .onNodeWithText("Nothing to show yet")
            .assertIsDisplayed()
    }
    
    @Test
    fun emptyFeedNoSpotsGlobal_displaysCorrectCopy() {
        composeTestRule.setContent {
            SpotTheme {
                EmptyFeedNoSpotsGlobal()
            }
        }
        
        composeTestRule
            .onNodeWithText("No Spots Yet")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Be the first to post")
            .assertIsDisplayed()
    }
}
