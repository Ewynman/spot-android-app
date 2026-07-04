package com.spot.android.feature.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.spot.android.core.design.theme.SpotTheme
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented tests for welcome auth gate behavior.
 *
 * Reference: PRD/18-build-order.md (Task 2.3)
 */
class WelcomeAuthGateTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun welcome_screen_displays_with_terms_gate() {
        composeRule.setContent {
            SpotTheme {
                WelcomeScreen(
                    isLoading = false,
                    onGetStarted = {},
                    onLogIn = {},
                    onGoogleSignIn = {},
                    onTermsAgreed = {},
                )
            }
        }

        composeRule.onNodeWithTag("welcome.screen").assertIsDisplayed()
        composeRule.onNodeWithTag("welcome.termsCheckbox").assertIsDisplayed()
        composeRule.onNodeWithTag("welcome.getStartedButton").assertIsNotEnabled()
        composeRule.onNodeWithTag("welcome.logInButton").assertIsNotEnabled()
        composeRule.onNodeWithTag("welcome.googleSignInButton").assertIsNotEnabled()
    }

    @Test
    fun terms_checked_enables_auth_actions() {
        var termsAgreed = false

        composeRule.setContent {
            SpotTheme {
                WelcomeScreen(
                    isLoading = false,
                    onGetStarted = {},
                    onLogIn = {},
                    onGoogleSignIn = {},
                    onTermsAgreed = { termsAgreed = it },
                )
            }
        }

        composeRule.onNodeWithTag("welcome.termsCheckbox").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("welcome.getStartedButton").assertIsDisplayed()
        assert(termsAgreed)
    }
}
