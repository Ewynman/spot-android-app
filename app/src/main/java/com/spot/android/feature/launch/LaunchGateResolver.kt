package com.spot.android.feature.launch

import com.spot.android.feature.auth.AuthUiState

/**
 * Ordered launch decision table for routing the root screen.
 *
 * Priority (first match wins after splash):
 * loading → OTP → welcome → username gate → terms gate → shell
 *
 * Reference: PRD/05-auth-onboarding.md
 */
object LaunchGateResolver {

    fun resolve(state: AuthUiState, minSplashElapsed: Boolean): LaunchDestination {
        if (!minSplashElapsed || state.isLoading || state.isResolvingLaunchGates) {
            return LaunchDestination.Splash
        }

        return when {
            state.awaitingEmailVerification -> LaunchDestination.ConfirmEmail
            !state.isAuthenticated -> LaunchDestination.Welcome
            state.needsUsernameSetup -> LaunchDestination.UsernameSetup
            state.needsTermsAcceptance -> LaunchDestination.TermsUpdate
            state.isAuthenticated && state.isEmailVerified -> LaunchDestination.MainShell
            else -> LaunchDestination.ConfirmEmail
        }
    }
}
