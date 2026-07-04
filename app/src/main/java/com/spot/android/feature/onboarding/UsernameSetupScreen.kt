package com.spot.android.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.spot.android.core.design.Dimensions
import androidx.compose.ui.text.input.ImeAction
import com.spot.android.core.design.theme.SpotColors
import com.spot.android.core.util.Constants
import com.spot.android.data.auth.AuthError
import com.spot.android.feature.auth.AuthErrorMessages
import com.spot.android.feature.auth.AuthValidation
import com.spot.android.feature.auth.UsernameAvailability
import com.spot.android.feature.auth.component.AuthErrorBanner
import com.spot.android.feature.auth.component.AuthPrimaryButton
import com.spot.android.feature.auth.component.AuthTextField
import com.spot.android.feature.auth.component.TermsAgreementCheckbox
import kotlinx.coroutines.delay

/**
 * Post-auth username setup for OAuth accounts without a username.
 *
 * Reference: PRD/05-auth-onboarding.md
 */
@Composable
fun UsernameSetupScreen(
    isLoading: Boolean,
    authError: AuthError?,
    usernameAvailability: UsernameAvailability,
    existingUsername: String?,
    onContinue: (String) -> Unit,
    onCheckUsername: (String) -> Unit,
    onClearUsernameAvailability: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var username by rememberSaveable { mutableStateOf(existingUsername.orEmpty()) }
    var termsChecked by rememberSaveable { mutableStateOf(false) }
    var contentReady by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(Constants.Auth.USERNAME_SETUP_LOAD_DELAY_MS)
        contentReady = true
    }

    LaunchedEffect(existingUsername) {
        if (!existingUsername.isNullOrBlank() && username.isBlank()) {
            username = existingUsername
        }
    }

    LaunchedEffect(username) {
        onClearUsernameAvailability()
        if (username.length >= 3) {
            delay(400)
            onCheckUsername(username)
        }
    }

    val formValid = AuthValidation.isValidUsername(username) &&
        usernameAvailability != UsernameAvailability.Taken &&
        termsChecked

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SpotColors.WelcomeSurface)
            .imePadding()
            .testTag("usernameSetup.screen"),
    ) {
        if (!contentReady) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.weight(1f))
                CircularProgressIndicator(
                    color = SpotColors.Primary,
                    modifier = Modifier.testTag("usernameSetup.loading"),
                )
                Spacer(modifier = Modifier.weight(1f))
            }
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimensions.Padding.horizontal)
                .padding(top = Dimensions.Padding.verticalXL, bottom = Dimensions.Padding.verticalLarge),
        ) {
            Text(
                text = "Choose a username",
                style = MaterialTheme.typography.titleLarge,
                color = SpotColors.Primary,
                modifier = Modifier.testTag("usernameSetup.title"),
            )

            Spacer(modifier = Modifier.height(Dimensions.Spacing.small))

            Text(
                text = "Pick a unique username so others can find you on Spot.",
                style = MaterialTheme.typography.bodyMedium,
                color = SpotColors.WelcomeMutedText,
                modifier = Modifier.testTag("usernameSetup.subtitle"),
            )

            Spacer(modifier = Modifier.height(Dimensions.Spacing.large))

            if (authError != null) {
                AuthErrorBanner(message = AuthErrorMessages.messageFor(authError))
                Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))
            }

            AuthTextField(
                value = username,
                onValueChange = { username = it },
                label = "Username",
                testTag = "usernameSetup.usernameField",
                imeAction = ImeAction.Done,
            )

            UsernameAvailabilityHint(usernameAvailability)

            Spacer(modifier = Modifier.height(Dimensions.Spacing.large))

            TermsAgreementCheckbox(
                checked = termsChecked,
                onCheckedChange = { termsChecked = it },
                checkboxTestTag = "usernameSetup.termsCheckbox",
            )

            Spacer(modifier = Modifier.height(Dimensions.Spacing.large))

            AuthPrimaryButton(
                text = if (isLoading) "Saving…" else "Continue",
                onClick = { onContinue(username.trim()) },
                enabled = formValid && !isLoading,
                testTag = "usernameSetup.continueButton",
            )

            if (isLoading) {
                CircularProgressIndicator(
                    color = SpotColors.Primary,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = Dimensions.Spacing.medium)
                        .testTag("usernameSetup.submitting"),
                )
            }
        }
    }
}

@Composable
private fun UsernameAvailabilityHint(availability: UsernameAvailability) {
    val (text, tag) = when (availability) {
        UsernameAvailability.Checking -> "Checking availability…" to "usernameSetup.usernameChecking"
        UsernameAvailability.Available -> "Username is available" to "usernameSetup.usernameAvailable"
        UsernameAvailability.Taken -> "This username is taken" to "usernameSetup.usernameTaken"
        UsernameAvailability.Unknown -> return
    }
    Text(
        text = text,
        color = SpotColors.WelcomeMutedText,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Dimensions.Spacing.small)
            .testTag(tag),
    )
}
