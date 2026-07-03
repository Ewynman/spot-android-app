package com.spot.android.data.model

/**
 * Domain model for a vibe tag.
 * 
 * Assembled from VibeRowDto.
 * 
 * Reference: PRD/03-data-model.md
 */
data class VibeTag(
    val id: String,
    val name: String,
    val nameLower: String
) {
    companion object {
        /**
         * Parse from ISO-8601 string if needed for createdAt timestamp.
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
