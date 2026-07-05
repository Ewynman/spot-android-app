package com.spot.android.feature.launch

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spot.android.core.util.Constants
import com.spot.android.feature.auth.AuthFlowHost
import com.spot.android.feature.auth.AuthViewModel
import com.spot.android.feature.auth.ConfirmEmailScreen
import com.spot.android.feature.onboarding.TermsUpdateScreen
import com.spot.android.feature.onboarding.UsernameSetupScreen
import com.spot.android.navigation.ProfileNavigationBus
import com.spot.android.navigation.OverlayHostViewModel
import com.spot.android.navigation.ShellNavigationBus
import com.spot.android.navigation.SpotShell
import com.spot.android.navigation.TabReselectBus
import kotlinx.coroutines.delay

/**
 * Root composable that resolves the launch gate and routes to the correct destination.
 *
 * Reference: PRD/05-auth-onboarding.md, PRD/18-build-order.md (Task 2.2)
 */
@Composable
fun SpotAppRoot(
    tabReselectBus: TabReselectBus,
    shellNavigationBus: ShellNavigationBus,
    profileNavigationBus: ProfileNavigationBus,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel(),
    overlayViewModel: OverlayHostViewModel = hiltViewModel(),
) {
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()

    var minSplashElapsed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(Constants.Launch.SPLASH_MIN_DURATION_MS)
        minSplashElapsed = true
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, authViewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                authViewModel.refreshAuthGates()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val destination = LaunchGateResolver.resolve(authState, minSplashElapsed)
    val showSplash = destination == LaunchDestination.Splash

    Crossfade(
        targetState = destination,
        animationSpec = tween(durationMillis = Constants.Launch.SPLASH_FADE_DURATION_MS.toInt()),
        label = "launch_gate",
        modifier = modifier.fillMaxSize(),
    ) { target ->
        when (target) {
            LaunchDestination.Splash -> LaunchSplashScreen(visible = showSplash)
            LaunchDestination.Welcome -> AuthFlowHost(authViewModel = authViewModel)
            LaunchDestination.ConfirmEmail -> ConfirmEmailScreen(
                email = authState.pendingVerificationEmail.orEmpty(),
                isLoading = authState.isLoading,
                authError = authState.authError,
                onBack = {
                    authViewModel.clearPendingVerification()
                    authViewModel.clearAuthError()
                },
                onVerify = { token ->
                    val email = authState.pendingVerificationEmail.orEmpty()
                    authViewModel.verifyEmailOtp(email, token)
                },
                onResend = {
                    authState.pendingVerificationEmail?.let(authViewModel::resendEmailOtp)
                },
            )
            LaunchDestination.UsernameSetup -> UsernameSetupScreen(
                isLoading = authState.isLoading,
                authError = authState.authError,
                usernameAvailability = authState.usernameAvailability,
                existingUsername = authState.currentUserUsername,
                onContinue = authViewModel::completeUsernameSetup,
                onCheckUsername = authViewModel::checkUsernameAvailability,
                onClearUsernameAvailability = authViewModel::clearUsernameAvailability,
            )
            LaunchDestination.TermsUpdate -> TermsUpdateScreen(
                isLoading = authState.isLoading,
                authError = authState.authError,
                onAccept = authViewModel::acceptTermsUpdate,
            )
            LaunchDestination.MainShell -> SpotShell(
                tabReselectBus = tabReselectBus,
                shellNavigationBus = shellNavigationBus,
                profileNavigationBus = profileNavigationBus,
                overlayViewModel = overlayViewModel,
            )
        }
    }
}
