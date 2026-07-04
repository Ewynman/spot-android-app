package com.spot.android.feature.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.spot.android.core.design.component.TopNavigationView
import com.spot.android.core.design.theme.SpotColors

/**
 * Full-screen overlay while fetching a deep-linked spot.
 *
 * Full implementation wired in Phase 4.4 (PRD/15).
 */
@Composable
fun SpotLoadingOverlay(
    spotId: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SpotColors.Background)
            .testTag("overlay.spotLoading"),
    ) {
        TopNavigationView()
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = SpotColors.Primary)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Loading spot…",
                    style = MaterialTheme.typography.bodyLarge,
                    color = SpotColors.Primary,
                )
            }
        }
    }
}

/**
 * Full-screen overlay when a spot cannot be displayed.
 */
@Composable
fun SpotUnavailableOverlay(
    spotId: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SpotColors.Background)
            .testTag("overlay.spotUnavailable"),
    ) {
        TopNavigationView()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Spot unavailable",
                    style = MaterialTheme.typography.headlineSmall,
                    color = SpotColors.Primary,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "This spot may have been removed or is not accessible.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SpotColors.WelcomeMutedText,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/**
 * Full-screen spot detail overlay (deep link / in-app).
 *
 * Will render [SpotCard] once spot data is available (Phase 4.4).
 */
@Composable
fun SpotDetailOverlay(
    spotId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SpotColors.Background)
            .testTag("overlay.spotDetail"),
    ) {
        TopNavigationView(
            showBackButton = true,
            onBackClick = onBack,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Spot detail\n($spotId)",
                style = MaterialTheme.typography.bodyLarge,
                color = SpotColors.Primary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * Subscription success screen shown after purchase or deep-link return.
 *
 * Reference: PRD/12-pro-subscription.md
 */
@Composable
fun ProSuccessOverlay(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    com.spot.android.feature.billing.ProSuccessScreen(
        onDone = onDismiss,
        modifier = modifier,
    )
}

/**
 * Post-purchase Pro onboarding tour.
 *
 * Reference: PRD/12-pro-subscription.md
 */
@Composable
fun ProOnboardingOverlay(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    com.spot.android.feature.billing.ProOnboardingTour(
        onComplete = onDismiss,
        modifier = modifier,
    )
}

/**
 * Pro upsell paywall with purchase/restore actions.
 *
 * Reference: PRD/12-pro-subscription.md
 */
@Composable
fun PaywallSheet(
    entryPoint: String?,
    onDismiss: () -> Unit,
    onShowProSuccess: () -> Unit,
    onShowProOnboarding: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SpotColors.Primary.copy(alpha = 0.4f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            )
            .testTag("overlay.paywall"),
        contentAlignment = Alignment.Center,
    ) {
        com.spot.android.feature.billing.PaywallScreen(
            entryPoint = entryPoint,
            onDismiss = onDismiss,
            onNavigateToTerms = { /* TODO: navigate to terms */ },
            onNavigateToPrivacy = { /* TODO: navigate to privacy */ },
            onShowProSuccess = onShowProSuccess,
            onShowProOnboarding = onShowProOnboarding,
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { /* Prevent dismiss on content click */ }
                ),
        )
    }
}
