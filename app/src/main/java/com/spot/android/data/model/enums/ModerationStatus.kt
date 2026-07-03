package com.spot.android.data.model.enums

/**
 * Moderation status values for users, spots, and media assets.
 * 
 * Reference: PRD/03-data-model.md
 */
enum class ModerationStatus(val value: String) {
    APPROVED("approved"),
    FLAGGED("flagged"),
    REJECTED("rejected"),
    PENDING_REVIEW("pending_review");
    
    companion object {
        fun fromValue(value: String?): ModerationStatus? {
            return entries.find { it.value == value }
        }
    }
}
