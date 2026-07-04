package com.spot.android.data.feed

class FakeEngagementRepository : EngagementRepository {

    var likeResult: Result<Unit> = Result.success(Unit)
    var unlikeResult: Result<Unit> = Result.success(Unit)
    var bookmarkResult: Result<Unit> = Result.success(Unit)
    var unbookmarkResult: Result<Unit> = Result.success(Unit)

    var lastSpotId: String? = null
    var likeCallCount = 0
    var unlikeCallCount = 0
    var bookmarkCallCount = 0
    var unbookmarkCallCount = 0

    override suspend fun likeSpot(spotId: String): Result<Unit> {
        likeCallCount++
        lastSpotId = spotId
        return likeResult
    }

    override suspend fun unlikeSpot(spotId: String): Result<Unit> {
        unlikeCallCount++
        lastSpotId = spotId
        return unlikeResult
    }

    override suspend fun bookmarkSpot(spotId: String): Result<Unit> {
        bookmarkCallCount++
        lastSpotId = spotId
        return bookmarkResult
    }

    override suspend fun unbookmarkSpot(spotId: String): Result<Unit> {
        unbookmarkCallCount++
        lastSpotId = spotId
        return unbookmarkResult
    }
}
