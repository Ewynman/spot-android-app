package com.spot.android.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import com.spot.android.core.design.Dimensions
import com.spot.android.core.design.theme.SpotColors
import com.spot.android.feature.auth.component.AuthPrimaryButton
import com.spot.android.feature.auth.component.AuthSecondaryButton
import com.spot.android.feature.auth.component.TermsAgreementCheckbox

/**
 * Welcome screen with Google sign-in, sign up, and log in entry points.
 *
 * Reference: PRD/05-auth-onboarding.md
 */
@Composable
fun WelcomeScreen(
    isLoading: Boolean,
    onGetStarted: () -> Unit,
    onLogIn: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onTermsAgreed: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var termsChecked by rememberSaveable { mutableStateOf(false) }
    val actionsEnabled = termsChecked && !isLoading

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SpotColors.WelcomeSurface)
            .testTag("welcome.screen"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimensions.Padding.horizontal)
                .padding(top = Dimensions.Padding.verticalXL, bottom = Dimensions.Padding.verticalLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Spacer(modifier = Modifier.height(Dimensions.Spacing.xl * 2))

                Text(
                    text = "SPOT",
                    style = MaterialTheme.typography.displayLarge,
                    color = SpotColors.Primary,
                    modifier = Modifier.testTag("welcome.wordmark"),
                )

                Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))

                Text(
                    text = "Discover places worth sharing",
                    style = MaterialTheme.typography.bodyLarge,
                    color = SpotColors.WelcomeMutedText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("welcome.tagline"),
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium),
            ) {
                TermsAgreementCheckbox(
                    checked = termsChecked,
                    onCheckedChange = { checked ->
                        termsChecked = checked
                        onTermsAgreed(checked)
                    },
                    checkboxTestTag = "welcome.termsCheckbox",
                )

                AuthSecondaryButton(
                    text = "Continue with Google",
                    onClick = {
                        onTermsAgreed(true)
                        onGoogleSignIn()
                    },
                    enabled = actionsEnabled,
                    testTag = "welcome.googleSignInButton",
                )

                AuthPrimaryButton(
                    text = "Get Started",
                    onClick = {
                        onTermsAgreed(true)
                        onGetStarted()
                    },
                    enabled = actionsEnabled,
                    testTag = "welcome.getStartedButton",
                )

                TextButton(
                    onClick = {
                        onTermsAgreed(true)
                        onLogIn()
                    },
                    enabled = actionsEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("welcome.logInButton"),
                ) {
                    Text(
                        text = "Log In",
                        color = if (actionsEnabled) SpotColors.Primary else SpotColors.WelcomeMutedText,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    color = SpotColors.Primary,
                    modifier = Modifier.testTag("welcome.loading"),
                )
            }
        }
    }
}
