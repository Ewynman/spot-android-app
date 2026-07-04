package com.spot.android.data.post

class FakeSpotPublishRepository : SpotPublishRepository {
    var spotId: String = "spot-new"
    var shouldFail: Boolean = false
    var failure: Throwable = IllegalStateException("publish failed")

    override suspend fun publishSpot(request: PublishSpotRequest): Result<String> {
        if (shouldFail) return Result.failure(failure)
        return Result.success(spotId)
    }
}
