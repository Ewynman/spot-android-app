package com.spot.android.data.feed

import com.spot.android.data.dto.HomeFeedRowDto

class FakeFeedRepository : FeedRepository {

    var homeFeedResult: Result<List<HomeFeedRowDto>> = Result.success(emptyList())
    var statusResult: Result<HomeFeedStatusDto> = Result.success(
        HomeFeedStatusDto(
            total_spots = 0,
            eligible_spots = 0,
            unseen_eligible_spots = 0,
            seen_eligible_spots = 0,
            status = "no_eligible_spots",
        ),
    )
    var topVibesResult: Result<List<String>> = Result.success(emptyList())

    var getHomeFeedCallCount = 0
    var lastForceSeenFallback: Boolean? = null
    var lastBatchId: String? = null

    override suspend fun getHomeFeed(
        limit: Int,
        batchId: String,
        viewerLat: Double?,
        viewerLng: Double?,
        forceSeenFallback: Boolean,
    ): Result<List<HomeFeedRowDto>> {
        getHomeFeedCallCount++
        lastForceSeenFallback = forceSeenFallback
        lastBatchId = batchId
        return homeFeedResult
    }

    override suspend fun getHomeFeedStatus(): Result<HomeFeedStatusDto> = statusResult

    override suspend fun getTopVibeNamesForDiversity(): Result<List<String>> = topVibesResult
}
