package com.spot.android.data.model

/**
 * Domain model for a bookmark collection (Pro feature).
 * 
 * Pro users can organize their saved spots into named collections.
 * 
 * Reference: PRD/10-profile-social.md, PRD/12-pro-subscription.md
 */
data class BookmarkCollection(
    val id: String,
    val userId: String,
    val name: String,
    val sortIndex: Int,
    val createdAt: Long, // Epoch millis
    val updatedAt: Long, // Epoch millis
    val spotCount: Int = 0, // Client-computed or fetched separately
) {
    companion object {
        /**
         * Parse ISO-8601 timestamp to epoch millis.
         */
        fun parseTimestamp(isoString: String): Long {
            return try {
                java.time.Instant.parse(isoString).toEpochMilli()
            } catch (e: Exception) {
                0L
            }
        }
    }
}
