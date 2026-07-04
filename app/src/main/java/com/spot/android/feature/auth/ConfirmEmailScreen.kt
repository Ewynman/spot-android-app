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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.spot.android.core.design.Dimensions
import com.spot.android.core.design.component.TopNavigationView
import com.spot.android.core.design.theme.SpotColors
import com.spot.android.core.util.Constants
import com.spot.android.data.auth.AuthError
import com.spot.android.feature.auth.component.AuthErrorBanner
import com.spot.android.feature.auth.component.AuthPrimaryButton
import com.spot.android.feature.auth.component.OtpInputRow
import kotlinx.coroutines.delay

/**
 * Email OTP confirmation screen.
 *
 * Reference: PRD/05-auth-onboarding.md
 */
@Composable
fun ConfirmEmailScreen(
    email: String,
    isLoading: Boolean,
    authError: AuthError?,
    onBack: () -> Unit,
    onVerify: (token: String) -> Unit,
    onResend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var otp by rememberSaveable { mutableStateOf("") }
    var cooldownSeconds by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(cooldownSeconds) {
        if (cooldownSeconds > 0) {
            delay(1000)
            cooldownSeconds -= 1
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SpotColors.WelcomeSurface)
            .imePadding()
            .testTag("confirmEmail.screen"),
    ) {
        TopNavigationView(showBackButton = true, onBackClick = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimensions.Padding.horizontal)
                .padding(bottom = Dimensions.Padding.verticalLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Confirm your email",
                style = MaterialTheme.typography.titleLarge,
                color = SpotColors.Primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("confirmEmail.title"),
            )

            Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))

            Text(
                text = "Enter the 6-digit code sent to ${maskEmail(email)}",
                style = MaterialTheme.typography.bodyMedium,
                color = SpotColors.WelcomeMutedText,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("confirmEmail.subtitle"),
            )

            Spacer(modifier = Modifier.height(Dimensions.Spacing.xl))

            if (authError != null) {
                AuthErrorBanner(message = AuthErrorMessages.messageFor(authError))
                Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))
            }

            OtpInputRow(
                otp = otp,
                onOtpChange = { otp = it },
                enabled = !isLoading,
            )

            Spacer(modifier = Modifier.height(Dimensions.Spacing.xl))

            AuthPrimaryButton(
                text = if (isLoading) "Verifying…" else "Verify",
                onClick = { onVerify(otp) },
                enabled = otp.length == Constants.Auth.OTP_LENGTH && !isLoading,
                testTag = "confirmEmail.verifyButton",
            )

            Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))

            val resendEnabled = cooldownSeconds == 0 && !isLoading
            TextButton(
                onClick = {
                    onResend()
                    cooldownSeconds = Constants.Timeouts.OTP_RESEND_COOLDOWN_SECONDS
                },
                enabled = resendEnabled,
                modifier = Modifier.testTag("confirmEmail.resendButton"),
            ) {
                Text(
                    text = if (cooldownSeconds > 0) {
                        "Resend code in ${cooldownSeconds}s"
                    } else {
                        "Resend code"
                    },
                    color = if (resendEnabled) SpotColors.WelcomeGlow else SpotColors.WelcomeMutedText,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            if (isLoading) {
                CircularProgressIndicator(
                    color = SpotColors.Primary,
                    modifier = Modifier
                        .padding(top = Dimensions.Spacing.medium)
                        .testTag("confirmEmail.loading"),
                )
            }
        }
    }
}
