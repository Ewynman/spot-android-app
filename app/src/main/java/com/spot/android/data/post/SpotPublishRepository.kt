package com.spot.android.data.post

/**
 * Publish pipeline: media asset insert, upload, moderation, and spot publish RPC.
 *
 * Reference: PRD/08-post-flow.md, PRD/04-backend-api.md
 */
interface SpotPublishRepository {
    suspend fun publishSpot(request: PublishSpotRequest): Result<String>
}

data class PublishSpotRequest(
    val images: List<ProcessedImage>,
    val vibeTagIds: List<String>,
    val latitude: Double,
    val longitude: Double,
    val locationName: String,
)

sealed class PublishException(message: String) : Exception(message) {
    class ImageRejected(message: String = "This image doesn't meet our community guidelines.") :
        PublishException(message)

    class ModerationUnavailable(message: String = "Moderation is temporarily unavailable. Please try again later.") :
        PublishException(message)

    class NotAuthenticated(message: String = "Not authenticated") : PublishException(message)
}
