package com.spot.android.data.feed

/**
 * Repository for spot like and bookmark writes.
 *
 * Reference: PRD/04-backend-api.md, PRD/06-home-feed.md
 */
interface EngagementRepository {
    suspend fun likeSpot(spotId: String): Result<Unit>
    suspend fun unlikeSpot(spotId: String): Result<Unit>
    suspend fun bookmarkSpot(spotId: String): Result<Unit>
    suspend fun unbookmarkSpot(spotId: String): Result<Unit>
}
