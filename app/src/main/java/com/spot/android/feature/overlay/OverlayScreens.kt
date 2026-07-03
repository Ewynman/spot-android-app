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
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SpotColors.Background)
            .testTag("overlay.proSuccess"),
    ) {
        TopNavigationView(
            showBackButton = true,
            onBackClick = onDismiss,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Welcome to Spot Pro",
                    style = MaterialTheme.typography.headlineSmall,
                    color = SpotColors.Primary,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Your subscription is active. Enjoy unlimited bookmarks, multi-photo posts, and more.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SpotColors.WelcomeMutedText,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/**
 * Pro upsell bottom sheet placeholder.
 *
 * Full paywall lands in Phase 4.1 (PRD/12).
 */
@Composable
fun PaywallSheet(
    entryPoint: String?,
    onDismiss: () -> Unit,
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
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Column(
                modifier = Modifier
                    .background(
                        color = SpotColors.Background,
                        shape = MaterialTheme.shapes.large,
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Upgrade to Spot Pro",
                    style = MaterialTheme.typography.titleLarge,
                    color = SpotColors.Primary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Unlock unlimited bookmarks, 5 photos per spot, custom vibes, and more.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SpotColors.WelcomeMutedText,
                    textAlign = TextAlign.Center,
                )
                if (entryPoint != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Entry: $entryPoint",
                        style = MaterialTheme.typography.labelSmall,
                        color = SpotColors.WelcomeMutedText,
                    )
                }
            }
        }
    }
}
