package com.spot.android.feature.auth

import com.spot.android.data.auth.AuthError

/**
 * Session state exposed to UI, mirroring iOS `AuthViewModel`.
 *
 * Reference: PRD/05-auth-onboarding.md
 */
data class AuthUiState(
    val isLoading: Boolean = true,
    val isAuthenticated: Boolean = false,
    val userId: String? = null,
    val isEmailVerified: Boolean = false,
    val awaitingEmailVerification: Boolean = false,
    val pendingVerificationEmail: String? = null,
    val likedSpots: Set<String> = emptySet(),
    val bookmarkedSpots: Set<String> = emptySet(),
    val blockedUsers: Set<String> = emptySet(),
    val isPro: Boolean = false,
    val proUntil: Long? = null,
    val customVibeTags: List<String> = emptyList(),
    val currentUserProfileImageURL: String? = null,
    val currentUserUsername: String? = null,
    val needsUsernameSetup: Boolean = false,
    val needsTermsAcceptance: Boolean = false,
    val isResolvingLaunchGates: Boolean = true,
    val authError: AuthError? = null,
)
