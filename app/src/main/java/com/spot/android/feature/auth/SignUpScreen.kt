package com.spot.android.feature.auth

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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.spot.android.core.design.Dimensions
import com.spot.android.core.design.component.TopNavigationView
import com.spot.android.core.design.theme.SpotColors
import com.spot.android.data.auth.AuthError
import com.spot.android.feature.auth.component.AuthErrorBanner
import com.spot.android.feature.auth.component.AuthPrimaryButton
import com.spot.android.feature.auth.component.AuthTextField
import com.spot.android.feature.auth.component.TermsAgreementCheckbox
import kotlinx.coroutines.delay

/**
 * Email sign-up screen.
 *
 * Reference: PRD/05-auth-onboarding.md
 */
@Composable
fun SignUpScreen(
    isLoading: Boolean,
    authError: AuthError?,
    usernameAvailability: UsernameAvailability,
    onBack: () -> Unit,
    onSignUp: (email: String, username: String, password: String, isPrivate: Boolean) -> Unit,
    onTermsAgreed: (Boolean) -> Unit,
    onCheckUsername: (String) -> Unit,
    onClearUsernameAvailability: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var isPrivate by rememberSaveable { mutableStateOf(false) }
    var termsChecked by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(username) {
        onClearUsernameAvailability()
        if (username.length >= 3) {
            delay(400)
            onCheckUsername(username)
        }
    }

    val formValid = AuthValidation.isValidEmail(email) &&
        AuthValidation.isValidUsername(username) &&
        AuthValidation.isValidPassword(password) &&
        AuthValidation.passwordsMatch(password, confirmPassword) &&
        usernameAvailability != UsernameAvailability.Taken &&
        termsChecked

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SpotColors.WelcomeSurface)
            .imePadding()
            .testTag("signUp.screen"),
    ) {
        TopNavigationView(showBackButton = true, onBackClick = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimensions.Padding.horizontal)
                .padding(bottom = Dimensions.Padding.verticalLarge),
        ) {
            Text(
                text = "Create account",
                style = MaterialTheme.typography.titleLarge,
                color = SpotColors.Primary,
                modifier = Modifier.testTag("signUp.title"),
            )

            Spacer(modifier = Modifier.height(Dimensions.Spacing.large))

            if (authError != null) {
                AuthErrorBanner(message = AuthErrorMessages.messageFor(authError))
                Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))
            }

            AuthTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                testTag = "signUp.emailField",
                keyboardType = KeyboardType.Email,
            )

            Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))

            AuthTextField(
                value = username,
                onValueChange = { username = it },
                label = "Username",
                testTag = "signUp.usernameField",
            )

            UsernameAvailabilityHint(usernameAvailability)

            Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))

            AuthTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                testTag = "signUp.passwordField",
                isPassword = true,
            )

            Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))

            AuthTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Confirm password",
                testTag = "signUp.confirmPasswordField",
                isPassword = true,
                imeAction = ImeAction.Done,
            )

            if (confirmPassword.isNotEmpty() && !AuthValidation.passwordsMatch(password, confirmPassword)) {
                Text(
                    text = "Passwords do not match",
                    color = SpotColors.WelcomeMutedText,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .padding(top = Dimensions.Spacing.small)
                        .testTag("signUp.passwordMismatch"),
                )
            }

            Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))

            androidx.compose.foundation.layout.Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.testTag("signUp.privateAccountRow"),
            ) {
                Checkbox(
                    checked = isPrivate,
                    onCheckedChange = { isPrivate = it },
                    modifier = Modifier.testTag("signUp.privateAccountCheckbox"),
                    colors = CheckboxDefaults.colors(
                        checkedColor = SpotColors.Primary,
                        uncheckedColor = SpotColors.WelcomeLine,
                        checkmarkColor = SpotColors.ButtonText,
                    ),
                )
                Text(
                    text = "Private account",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SpotColors.WelcomeMutedText,
                )
            }

            Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))

            TermsAgreementCheckbox(
                checked = termsChecked,
                onCheckedChange = { checked ->
                    termsChecked = checked
                    onTermsAgreed(checked)
                },
                checkboxTestTag = "signUp.termsCheckbox",
            )

            Spacer(modifier = Modifier.height(Dimensions.Spacing.large))

            AuthPrimaryButton(
                text = if (isLoading) "Creating account…" else "Sign Up",
                onClick = {
                    onTermsAgreed(true)
                    onSignUp(email.trim(), username.trim(), password, isPrivate)
                },
                enabled = formValid && !isLoading,
                testTag = "signUp.submitButton",
            )

            if (isLoading) {
                CircularProgressIndicator(
                    color = SpotColors.Primary,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = Dimensions.Spacing.medium)
                        .testTag("signUp.loading"),
                )
            }
        }
    }
}

@Composable
private fun UsernameAvailabilityHint(availability: UsernameAvailability) {
    val (text, tag) = when (availability) {
        UsernameAvailability.Checking -> "Checking availability…" to "signUp.usernameChecking"
        UsernameAvailability.Available -> "Username is available" to "signUp.usernameAvailable"
        UsernameAvailability.Taken -> "This username is taken" to "signUp.usernameTaken"
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
