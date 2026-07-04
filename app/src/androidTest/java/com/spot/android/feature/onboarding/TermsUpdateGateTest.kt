package com.spot.android.feature.onboarding

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.spot.android.core.design.theme.SpotTheme
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented tests for the blocking terms update gate.
 */
class TermsUpdateGateTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun terms_update_screen_displays_with_accept_gate() {
        composeRule.setContent {
            SpotTheme {
                TermsUpdateScreen(
                    isLoading = false,
                    authError = null,
                    onAccept = {},
                )
            }
        }

        composeRule.onNodeWithTag("termsUpdate.screen").assertIsDisplayed()
        composeRule.onNodeWithTag("termsUpdate.termsCheckbox").assertIsDisplayed()
        composeRule.onNodeWithTag("termsUpdate.acceptButton").assertIsNotEnabled()
    }

    @Test
    fun terms_checked_enables_accept_button() {
        var accepted = false

        composeRule.setContent {
            SpotTheme {
                TermsUpdateScreen(
                    isLoading = false,
                    authError = null,
                    onAccept = { accepted = true },
                )
            }
        }

        composeRule.onNodeWithTag("termsUpdate.termsCheckbox").performClick()
        composeRule.onNodeWithTag("termsUpdate.acceptButton").assertIsEnabled()
        composeRule.onNodeWithTag("termsUpdate.acceptButton").performClick()
        assert(accepted)
    }
}
