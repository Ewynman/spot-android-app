package com.spot.android.data.auth

class FakeUserSessionRepository : UserSessionRepository {

    var snapshot: Result<UserSessionSnapshot> = Result.success(
        UserSessionSnapshot(
            username = "testuser",
            profileImageURL = "https://example.com/avatar.jpg",
            isPro = false,
            proUntil = null,
            emailVerified = true,
            likedSpots = setOf("spot-1"),
            bookmarkedSpots = setOf("spot-2"),
            blockedUsers = setOf("user-blocked"),
            customVibeTags = emptyList(),
            needsUsernameSetup = false,
        ),
    )

    var loadCalls = 0

    override suspend fun loadSessionSnapshot(userId: String): Result<UserSessionSnapshot> {
        loadCalls++
        return snapshot
    }
}
