package com.spot.android.data.feed

import com.spot.android.data.dto.HomeFeedRowDto

/**
 * Repository for home feed RPCs.
 *
 * Reference: PRD/06-home-feed.md, PRD/04-backend-api.md
 */
interface FeedRepository {
    suspend fun getHomeFeed(
        limit: Int,
        batchId: String,
        viewerLat: Double? = null,
        viewerLng: Double? = null,
        forceSeenFallback: Boolean = false,
    ): Result<List<HomeFeedRowDto>>

    suspend fun getHomeFeedStatus(): Result<HomeFeedStatusDto>

    suspend fun getTopVibeNamesForDiversity(): Result<List<String>>
}
