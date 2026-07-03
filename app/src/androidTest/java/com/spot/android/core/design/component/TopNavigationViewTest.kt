package com.spot.android.core.design.component

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.spot.android.core.design.theme.SpotTheme
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented tests for TopNavigationView component.
 */
class TopNavigationViewTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun topNavigationView_displaysWordmark() {
        composeTestRule.setContent {
            SpotTheme {
                TopNavigationView()
            }
        }
        
        composeTestRule
            .onNodeWithTag("navigation.wordmark")
            .assertIsDisplayed()
    }
    
    @Test
    fun topNavigationView_showsBackButton_whenRequested() {
        composeTestRule.setContent {
            SpotTheme {
                TopNavigationView(
                    showBackButton = true,
                    onBackClick = {}
                )
            }
        }
        
        composeTestRule
            .onNodeWithTag("navigation.backButton")
            .assertIsDisplayed()
    }
    
    @Test
    fun topNavigationView_hidesBackButton_byDefault() {
        composeTestRule.setContent {
            SpotTheme {
                TopNavigationView()
            }
        }
        
        composeTestRule
            .onNodeWithTag("navigation.backButton")
            .assertDoesNotExist()
    }
    
    @Test
    fun topNavigationView_backButtonClick_triggersCallback() {
        var clicked = false
        
        composeTestRule.setContent {
            SpotTheme {
                TopNavigationView(
                    showBackButton = true,
                    onBackClick = { clicked = true }
                )
            }
        }
        
        composeTestRule
            .onNodeWithTag("navigation.backButton")
            .performClick()
        
        assert(clicked)
    }
}
