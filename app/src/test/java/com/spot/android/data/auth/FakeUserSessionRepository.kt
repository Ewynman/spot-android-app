package com.spot.android.data.auth

class FakeUserSessionRepository : UserSessionRepository {

    var snapshot: Result<UserSessionSnapshot> = Result.success(
        UserSessionSnapshot(
            username = "testuser",
            email = "test@example.com",
            profileImageURL = "https://example.com/avatar.jpg",
            isPrivate = false,
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

    override suspend fun updatePrivateAccount(isPrivate: Boolean): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun isPrivateAccount(): Boolean {
        return false
    }
}
