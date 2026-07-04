package com.spot.android.feature.launch

import com.spot.android.feature.auth.AuthUiState
import org.junit.Assert.assertEquals
import org.junit.Test

class LaunchGateResolverTest {

    @Test
    fun `shows splash while session is loading`() {
        val state = AuthUiState(isLoading = true, isResolvingLaunchGates = true)
        assertEquals(LaunchDestination.Splash, LaunchGateResolver.resolve(state, minSplashElapsed = true))
    }

    @Test
    fun `shows splash until minimum duration elapses`() {
        val state = AuthUiState(isLoading = false, isResolvingLaunchGates = false)
        assertEquals(LaunchDestination.Splash, LaunchGateResolver.resolve(state, minSplashElapsed = false))
    }

    @Test
    fun `shows splash while launch gates are resolving`() {
        val state = AuthUiState(
            isLoading = false,
            isAuthenticated = true,
            isEmailVerified = true,
            isResolvingLaunchGates = true,
        )
        assertEquals(LaunchDestination.Splash, LaunchGateResolver.resolve(state, minSplashElapsed = true))
    }

    @Test
    fun `routes to confirm email when awaiting verification`() {
        val state = AuthUiState(
            isLoading = false,
            isResolvingLaunchGates = false,
            awaitingEmailVerification = true,
            pendingVerificationEmail = "user@example.com",
        )
        assertEquals(LaunchDestination.ConfirmEmail, LaunchGateResolver.resolve(state, minSplashElapsed = true))
    }

    @Test
    fun `routes to welcome when unauthenticated`() {
        val state = AuthUiState(
            isLoading = false,
            isResolvingLaunchGates = false,
            isAuthenticated = false,
            awaitingEmailVerification = false,
        )
        assertEquals(LaunchDestination.Welcome, LaunchGateResolver.resolve(state, minSplashElapsed = true))
    }

    @Test
    fun `routes to username setup for oauth accounts missing username`() {
        val state = AuthUiState(
            isLoading = false,
            isResolvingLaunchGates = false,
            isAuthenticated = true,
            isEmailVerified = true,
            needsUsernameSetup = true,
        )
        assertEquals(LaunchDestination.UsernameSetup, LaunchGateResolver.resolve(state, minSplashElapsed = true))
    }

    @Test
    fun `routes to terms update when terms not accepted`() {
        val state = AuthUiState(
            isLoading = false,
            isResolvingLaunchGates = false,
            isAuthenticated = true,
            isEmailVerified = true,
            needsTermsAcceptance = true,
        )
        assertEquals(LaunchDestination.TermsUpdate, LaunchGateResolver.resolve(state, minSplashElapsed = true))
    }

    @Test
    fun `username gate takes priority over terms gate`() {
        val state = AuthUiState(
            isLoading = false,
            isResolvingLaunchGates = false,
            isAuthenticated = true,
            isEmailVerified = true,
            needsUsernameSetup = true,
            needsTermsAcceptance = true,
        )
        assertEquals(LaunchDestination.UsernameSetup, LaunchGateResolver.resolve(state, minSplashElapsed = true))
    }

    @Test
    fun `routes to main shell for authenticated verified user`() {
        val state = AuthUiState(
            isLoading = false,
            isResolvingLaunchGates = false,
            isAuthenticated = true,
            isEmailVerified = true,
            userId = "user-123",
        )
        assertEquals(LaunchDestination.MainShell, LaunchGateResolver.resolve(state, minSplashElapsed = true))
    }

    @Test
    fun `routes authenticated unverified user to confirm email`() {
        val state = AuthUiState(
            isLoading = false,
            isResolvingLaunchGates = false,
            isAuthenticated = true,
            isEmailVerified = false,
            awaitingEmailVerification = false,
        )
        assertEquals(LaunchDestination.ConfirmEmail, LaunchGateResolver.resolve(state, minSplashElapsed = true))
    }
}
