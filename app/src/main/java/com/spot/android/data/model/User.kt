package com.spot.android.data.model

/**
 * Domain model for a user profile.
 * 
 * Assembled from UserRowDto or UserBriefRowDto.
 * 
 * Reference: PRD/10-profile-social.md, PRD/04-backend-api.md
 */
data class User(
    val id: String,
    val username: String,
    val profileImageURL: String?,
    val isPrivate: Boolean,
    val isPro: Boolean,
    val proUntil: Long?, // Epoch millis
    val spotsCount: Long,
    val isCurrentUser: Boolean = false,
    val createdAt: Long?, // Epoch millis
    
    // Optional fields for own profile
    val email: String? = null,
    val emailVerified: Boolean = false,
    val blockedUsers: List<String>? = null,
    val customVibeTags: List<String>? = null,
    val vibeStats: Map<String, Int>? = null
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
