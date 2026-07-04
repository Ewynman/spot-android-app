package com.spot.android.feature.onboarding

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.spot.android.core.design.theme.SpotTheme
import com.spot.android.feature.auth.UsernameAvailability
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented tests for post-auth username setup gate behavior.
 */
class UsernameSetupGateTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun username_setup_screen_displays_with_terms_gate() {
        composeRule.setContent {
            SpotTheme {
                UsernameSetupScreen(
                    isLoading = false,
                    authError = null,
                    usernameAvailability = UsernameAvailability.Unknown,
                    existingUsername = null,
                    onContinue = {},
                    onCheckUsername = {},
                    onClearUsernameAvailability = {},
                )
            }
        }

        composeRule.waitUntil(timeoutMillis = 2_000) {
            composeRule.onAllNodesWithTag("usernameSetup.title").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag("usernameSetup.screen").assertIsDisplayed()
        composeRule.onNodeWithTag("usernameSetup.usernameField").assertIsDisplayed()
        composeRule.onNodeWithTag("usernameSetup.termsCheckbox").assertIsDisplayed()
        composeRule.onNodeWithTag("usernameSetup.continueButton").assertIsNotEnabled()
    }

    @Test
    fun terms_checked_and_valid_username_enables_continue() {
        var submittedUsername: String? = null

        composeRule.setContent {
            SpotTheme {
                UsernameSetupScreen(
                    isLoading = false,
                    authError = null,
                    usernameAvailability = UsernameAvailability.Available,
                    existingUsername = null,
                    onContinue = { submittedUsername = it },
                    onCheckUsername = {},
                    onClearUsernameAvailability = {},
                )
            }
        }

        composeRule.waitUntil(timeoutMillis = 2_000) {
            composeRule.onAllNodesWithTag("usernameSetup.usernameField").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag("usernameSetup.usernameField").performClick()
        composeRule.onNodeWithTag("usernameSetup.usernameField").performTextInput("spotuser123")
        composeRule.onNodeWithTag("usernameSetup.termsCheckbox").performClick()
        composeRule.onNodeWithTag("usernameSetup.continueButton").assertIsEnabled()
        composeRule.onNodeWithTag("usernameSetup.continueButton").performClick()
        assert(submittedUsername == "spotuser123")
    }
}
