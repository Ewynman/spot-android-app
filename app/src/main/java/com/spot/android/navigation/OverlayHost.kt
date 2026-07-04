package com.spot.android.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spot.android.feature.overlay.PaywallSheet
import com.spot.android.feature.overlay.ProOnboardingOverlay
import com.spot.android.feature.overlay.ProSuccessOverlay
import com.spot.android.feature.overlay.SpotDetailOverlay
import com.spot.android.feature.overlay.SpotLoadingOverlay
import com.spot.android.feature.overlay.SpotUnavailableOverlay

/**
 * Renders the active top-level overlay above the tab shell.
 *
 * Overlays are full-screen (or sheet) and block interaction with tabs underneath.
 * Tap-outside dismisses the paywall sheet; other overlays use explicit back/dismiss.
 */
@Composable
fun OverlayHost(
    overlay: AppOverlay,
    onDismiss: () -> Unit,
    onShowProSuccess: () -> Unit = {},
    onShowProOnboarding: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    when (overlay) {
        AppOverlay.None -> Unit

        is AppOverlay.SpotDetail -> {
            SpotDetailOverlay(
                spotId = overlay.spotId,
                onBack = onDismiss,
                modifier = modifier,
            )
        }

        is AppOverlay.SpotLoading -> {
            SpotLoadingOverlay(
                spotId = overlay.spotId,
                modifier = modifier,
            )
        }

        is AppOverlay.SpotUnavailable -> {
            SpotUnavailableOverlay(
                spotId = overlay.spotId,
                modifier = modifier,
            )
        }

        AppOverlay.ProSuccess -> {
            ProSuccessOverlay(
                onDismiss = onDismiss,
                modifier = modifier,
            )
        }

        AppOverlay.ProOnboarding -> {
            ProOnboardingOverlay(
                onDismiss = onDismiss,
                modifier = modifier,
            )
        }

        is AppOverlay.Paywall -> {
            PaywallSheet(
                entryPoint = overlay.entryPoint,
                onDismiss = onDismiss,
                onShowProSuccess = onShowProSuccess,
                onShowProOnboarding = onShowProOnboarding,
                modifier = modifier,
            )
        }
    }
}

/**
 * Wrapper that observes overlay state from [OverlayHostViewModel] and renders [OverlayHost].
 */
@Composable
fun OverlayHostLayer(
    viewModel: OverlayHostViewModel,
    modifier: Modifier = Modifier,
) {
    val overlay by viewModel.overlayState.collectAsStateWithLifecycle()

    if (overlay != AppOverlay.None) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .testTag("navigation.overlayHost"),
        ) {
            OverlayHost(
                overlay = overlay,
                onDismiss = viewModel::dismissOverlay,
                onShowProSuccess = viewModel::showProSuccess,
                onShowProOnboarding = viewModel::showProOnboarding,
            )
        }
    }
}
