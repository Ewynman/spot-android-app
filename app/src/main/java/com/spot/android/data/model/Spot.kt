package com.spot.android.data.model

/**
 * Domain model for a Spot.
 * 
 * Assembled from HomeFeedRowDto, MapSpotRowDto, or SpotRowDto + joined data.
 * 
 * This is the UI-facing model used by feed, map, search, and profile.
 * 
 * Reference: PRD/06-home-feed.md, PRD/04-backend-api.md
 */
data class Spot(
    val id: String,
    val userId: String,
    val username: String,
    val userProfileImageURL: String?,
    val caption: String,
    val latitude: Double,
    val longitude: Double,
    val locationName: String?,
    val likes: Long,
    val saves: Long,
    val createdAt: Long, // Epoch millis
    val updatedAt: Long?, // Epoch millis
    
    // Images
    val imageURL: String?, // Cover image (signed URL or public URL)
    val thumbnailURL: String?, // Same as imageURL for now
    val imageURLs: List<String>? = null, // Multi-image expanded (signed URLs)
    val images: List<SpotImage>? = null, // Full image data when available
    val mediaDisplayAspectRatio: Double,
    val mediaCount: Int,
    
    // Vibes
    val vibeTag: String?, // Primary vibe name
    val vibeTags: List<VibeTag>? = null, // Full multi-vibe set when available
    
    // Author info
    val authorIsPrivate: Boolean,
    val authorIsPro: Boolean = false,
    
    // Client state
    var isLiked: Boolean = false,
    var isSaved: Boolean = false,
    
    // Feed ranking metadata
    val rankPosition: Int? = null,
    val rankingScore: Double? = null,
    val seenBefore: Boolean? = null,
    val lastSeenAt: Long? = null,
    val sourceBucket: String? = null,
    
    // Map metadata
    val distanceMeters: Double? = null
) {
    companion object {
        /**
         * Parse ISO-8601 timestamp to epoch millis.
         */
        fun parseTimestamp(isoString: String?): Long? {
            if (isoString == null) return null
            return try {
                java.time.Instant.parse(isoString).toEpochMilli()
            } catch (e: Exception) {
                null
            }
        }
    }
}
