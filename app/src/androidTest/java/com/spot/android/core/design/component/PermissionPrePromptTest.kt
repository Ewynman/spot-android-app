package com.spot.android.core.design.component

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.spot.android.core.design.theme.SpotTheme
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented tests for PermissionPrePrompt component.
 */
class PermissionPrePromptTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun permissionPrePrompt_displaysTitle() {
        composeTestRule.setContent {
            SpotTheme {
                PermissionPrePrompt(
                    type = PermissionType.LOCATION,
                    title = "Enable Location",
                    message = "We need your location",
                    onContinue = {},
                    onSkip = {}
                )
            }
        }
        
        composeTestRule
            .onNodeWithTag("permissionPrePrompt.title")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Enable Location")
            .assertIsDisplayed()
    }
    
    @Test
    fun permissionPrePrompt_displaysMessage() {
        composeTestRule.setContent {
            SpotTheme {
                PermissionPrePrompt(
                    type = PermissionType.LOCATION,
                    title = "Enable Location",
                    message = "We need your location",
                    onContinue = {},
                    onSkip = {}
                )
            }
        }
        
        composeTestRule
            .onNodeWithTag("permissionPrePrompt.message")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("We need your location")
            .assertIsDisplayed()
    }
    
    @Test
    fun permissionPrePrompt_displaysIcon() {
        composeTestRule.setContent {
            SpotTheme {
                PermissionPrePrompt(
                    type = PermissionType.LOCATION,
                    title = "Enable Location",
                    message = "We need your location",
                    onContinue = {},
                    onSkip = {}
                )
            }
        }
        
        composeTestRule
            .onNodeWithTag("permissionPrePrompt.icon")
            .assertIsDisplayed()
    }
    
    @Test
    fun permissionPrePrompt_continueButtonClick_triggersCallback() {
        var continueClicked = false
        
        composeTestRule.setContent {
            SpotTheme {
                PermissionPrePrompt(
                    type = PermissionType.LOCATION,
                    title = "Enable Location",
                    message = "We need your location",
                    onContinue = { continueClicked = true },
                    onSkip = {}
                )
            }
        }
        
        composeTestRule
            .onNodeWithTag("permissionPrePrompt.continueButton")
            .performClick()
        
        assert(continueClicked)
    }
    
    @Test
    fun permissionPrePrompt_skipButtonClick_triggersCallback() {
        var skipClicked = false
        
        composeTestRule.setContent {
            SpotTheme {
                PermissionPrePrompt(
                    type = PermissionType.LOCATION,
                    title = "Enable Location",
                    message = "We need your location",
                    onContinue = {},
                    onSkip = { skipClicked = true }
                )
            }
        }
        
        composeTestRule
            .onNodeWithTag("permissionPrePrompt.skipButton")
            .performClick()
        
        assert(skipClicked)
    }
}
