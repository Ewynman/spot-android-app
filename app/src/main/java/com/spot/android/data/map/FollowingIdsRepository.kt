package com.spot.android.data.map

/**
 * Loads the set of user ids the current user follows (for map filter).
 */
interface FollowingIdsRepository {
    suspend fun getFollowedUserIds(): Result<Set<String>>
}
