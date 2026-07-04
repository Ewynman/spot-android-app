package com.spot.android.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
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

/**
 * Email / username log-in screen.
 *
 * Reference: PRD/05-auth-onboarding.md
 */
@Composable
fun LoginScreen(
    isLoading: Boolean,
    authError: AuthError?,
    onBack: () -> Unit,
    onSignIn: (identifier: String, password: String) -> Unit,
    onForgotPassword: (email: String) -> Unit,
    passwordResetSent: Boolean,
    onTermsAgreed: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var identifier by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var termsChecked by rememberSaveable { mutableStateOf(false) }

    val formValid = identifier.isNotBlank() &&
        AuthValidation.isValidPassword(password) &&
        termsChecked

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SpotColors.WelcomeSurface)
            .imePadding()
            .testTag("login.screen"),
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
                text = "Log in",
                style = MaterialTheme.typography.titleLarge,
                color = SpotColors.Primary,
                modifier = Modifier.testTag("login.title"),
            )

            Spacer(modifier = Modifier.height(Dimensions.Spacing.large))

            if (authError != null) {
                AuthErrorBanner(message = AuthErrorMessages.messageFor(authError))
                Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))
            }

            if (passwordResetSent) {
                AuthErrorBanner(
                    message = "Password reset email sent. Check your inbox.",
                    testTag = "login.resetSentBanner",
                )
                Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))
            }

            AuthTextField(
                value = identifier,
                onValueChange = { identifier = it },
                label = "Email or username",
                testTag = "login.identifierField",
                keyboardType = KeyboardType.Email,
            )

            Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))

            AuthTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                testTag = "login.passwordField",
                isPassword = true,
                imeAction = ImeAction.Done,
                onImeAction = {
                    if (formValid && !isLoading) {
                        onTermsAgreed(true)
                        onSignIn(identifier.trim(), password)
                    }
                },
            )

            TextButton(
                onClick = { onForgotPassword(identifier.trim()) },
                enabled = !isLoading,
                modifier = Modifier.testTag("login.forgotPasswordButton"),
            ) {
                Text(
                    text = "Forgot password?",
                    color = SpotColors.WelcomeGlow,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))

            TermsAgreementCheckbox(
                checked = termsChecked,
                onCheckedChange = { checked ->
                    termsChecked = checked
                    onTermsAgreed(checked)
                },
                checkboxTestTag = "login.termsCheckbox",
            )

            Spacer(modifier = Modifier.height(Dimensions.Spacing.large))

            AuthPrimaryButton(
                text = if (isLoading) "Signing in…" else "Log In",
                onClick = {
                    onTermsAgreed(true)
                    onSignIn(identifier.trim(), password)
                },
                enabled = formValid && !isLoading,
                testTag = "login.submitButton",
            )

            if (isLoading) {
                CircularProgressIndicator(
                    color = SpotColors.Primary,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = Dimensions.Spacing.medium)
                        .testTag("login.loading"),
                )
            }
        }
    }
}
