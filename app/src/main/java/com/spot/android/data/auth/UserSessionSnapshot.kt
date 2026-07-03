package com.spot.android.data.auth

/**
 * Loaded session data for the authenticated user.
 *
 * Reference: PRD/05-auth-onboarding.md, PRD/10-profile-social.md
 */
data class UserSessionSnapshot(
    val username: String,
    val profileImageURL: String?,
    val isPro: Boolean,
    val proUntil: Long?,
    val emailVerified: Boolean,
    val likedSpots: Set<String>,
    val bookmarkedSpots: Set<String>,
    val blockedUsers: Set<String>,
    val customVibeTags: List<String>,
    val needsUsernameSetup: Boolean,
)
