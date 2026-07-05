package com.spot.android.data.profile

import com.spot.android.data.model.Spot
import com.spot.android.data.model.User

/**
 * Repository for profile reads and own-spot deletion.
 *
 * Reference: PRD/04-backend-api.md, PRD/10-profile-social.md
 */
interface ProfileRepository {
    suspend fun getOwnProfile(): Result<User>
    suspend fun getPublicProfile(userId: String): Result<User>
    suspend fun getUserSpotIds(
        userId: String,
        offset: Int,
        limit: Int,
    ): Result<List<String>>
    suspend fun getLikedSpotIds(
        userId: String,
        offset: Int,
        limit: Int,
    ): Result<List<String>>
    suspend fun getBookmarkedSpotIds(
        userId: String,
        offset: Int,
        limit: Int,
    ): Result<List<String>>
    suspend fun hydrateSpots(spotIds: List<String>): Result<List<Spot>>
    suspend fun deleteOwnSpot(spotId: String): Result<Unit>
}
