package com.spot.android.feature.launch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import com.spot.android.core.design.Dimensions
import com.spot.android.core.design.theme.SpotColors

/**
 * Minimal placeholder screens for auth gates not yet implemented in Phase 2.3/2.4.
 * Each exposes the iOS-compatible test tag for routing verification.
 */
@Composable
fun WelcomePlaceholderScreen(modifier: Modifier = Modifier) {
    AuthGatePlaceholder(
        message = "Welcome",
        testTag = "welcome.screen",
        modifier = modifier,
    )
}

@Composable
fun ConfirmEmailPlaceholderScreen(modifier: Modifier = Modifier) {
    AuthGatePlaceholder(
        message = "Confirm your email",
        testTag = "confirmEmail.screen",
        modifier = modifier,
    )
}

@Composable
fun UsernameSetupPlaceholderScreen(modifier: Modifier = Modifier) {
    AuthGatePlaceholder(
        message = "Set up your username",
        testTag = "usernameSetup.screen",
        modifier = modifier,
    )
}

@Composable
fun TermsUpdatePlaceholderScreen(modifier: Modifier = Modifier) {
    AuthGatePlaceholder(
        message = "Accept updated terms",
        testTag = "termsUpdate.screen",
        modifier = modifier,
    )
}

@Composable
private fun AuthGatePlaceholder(
    message: String,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SpotColors.Background)
            .testTag(testTag),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.titleLarge,
            color = SpotColors.Primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(Dimensions.Spacing.large),
        )
    }
}
