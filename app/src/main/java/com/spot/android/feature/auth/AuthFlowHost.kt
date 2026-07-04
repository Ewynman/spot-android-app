package com.spot.android.feature.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Hosts navigation between welcome, sign-up, and login screens.
 */
@Composable
fun AuthFlowHost(
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier,
) {
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    var route by rememberSaveable { mutableStateOf(AuthFlowRoute.Welcome) }

    when (route) {
        AuthFlowRoute.Welcome -> WelcomeScreen(
            isLoading = authState.isLoading,
            onGetStarted = {
                authViewModel.clearAuthError()
                route = AuthFlowRoute.SignUp
            },
            onLogIn = {
                authViewModel.clearAuthError()
                route = AuthFlowRoute.Login
            },
            onGoogleSignIn = authViewModel::signInWithGoogle,
            onTermsAgreed = authViewModel::savePreAuthTermsAgreement,
            modifier = modifier,
        )

        AuthFlowRoute.SignUp -> SignUpScreen(
            isLoading = authState.isLoading,
            authError = authState.authError,
            usernameAvailability = authState.usernameAvailability,
            onBack = {
                authViewModel.clearAuthError()
                authViewModel.clearUsernameAvailability()
                route = AuthFlowRoute.Welcome
            },
            onSignUp = authViewModel::signUpWithEmail,
            onTermsAgreed = authViewModel::savePreAuthTermsAgreement,
            onCheckUsername = authViewModel::checkUsernameAvailability,
            onClearUsernameAvailability = authViewModel::clearUsernameAvailability,
            modifier = modifier,
        )

        AuthFlowRoute.Login -> LoginScreen(
            isLoading = authState.isLoading,
            authError = authState.authError,
            onBack = {
                authViewModel.clearAuthError()
                route = AuthFlowRoute.Welcome
            },
            onSignIn = authViewModel::signInWithEmailOrUsername,
            onForgotPassword = authViewModel::resetPassword,
            passwordResetSent = authState.passwordResetSent,
            onTermsAgreed = authViewModel::savePreAuthTermsAgreement,
            modifier = modifier,
        )
    }
}
