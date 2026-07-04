package com.spot.android.feature.onboarding

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.spot.android.core.design.Dimensions
import com.spot.android.core.design.theme.SpotColors
import com.spot.android.data.auth.AuthError
import com.spot.android.feature.auth.AuthErrorMessages
import com.spot.android.feature.auth.component.AuthErrorBanner
import com.spot.android.feature.auth.component.AuthPrimaryButton
import com.spot.android.feature.auth.component.TermsAgreementCheckbox

/**
 * Blocking terms update gate shown when the active terms version has not been accepted.
 *
 * Reference: PRD/05-auth-onboarding.md
 */
@Composable
fun TermsUpdateScreen(
    isLoading: Boolean,
    authError: AuthError?,
    onAccept: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var termsChecked by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SpotColors.WelcomeSurface)
            .imePadding()
            .testTag("termsUpdate.screen"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimensions.Padding.horizontal)
                .padding(top = Dimensions.Padding.verticalXL, bottom = Dimensions.Padding.verticalLarge),
        ) {
            Text(
                text = "Updated Terms",
                style = MaterialTheme.typography.titleLarge,
                color = SpotColors.Primary,
                modifier = Modifier.testTag("termsUpdate.title"),
            )

            Spacer(modifier = Modifier.height(Dimensions.Spacing.small))

            Text(
                text = "We've updated our Terms of Service and Privacy Policy. Please review and accept to continue using Spot.",
                style = MaterialTheme.typography.bodyMedium,
                color = SpotColors.WelcomeMutedText,
                modifier = Modifier.testTag("termsUpdate.message"),
            )

            Spacer(modifier = Modifier.height(Dimensions.Spacing.large))

            if (authError != null) {
                AuthErrorBanner(message = AuthErrorMessages.messageFor(authError))
                Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))
            }

            TermsAgreementCheckbox(
                checked = termsChecked,
                onCheckedChange = { termsChecked = it },
                checkboxTestTag = "termsUpdate.termsCheckbox",
            )

            Spacer(modifier = Modifier.height(Dimensions.Spacing.large))

            AuthPrimaryButton(
                text = if (isLoading) "Saving…" else "Accept and Continue",
                onClick = onAccept,
                enabled = termsChecked && !isLoading,
                testTag = "termsUpdate.acceptButton",
            )

            if (isLoading) {
                CircularProgressIndicator(
                    color = SpotColors.Primary,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = Dimensions.Spacing.medium)
                        .testTag("termsUpdate.submitting"),
                )
            }
        }
    }
}
