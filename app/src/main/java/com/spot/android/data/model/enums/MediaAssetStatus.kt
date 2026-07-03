package com.spot.android.data.model.enums

/**
 * Media asset status values from `media_assets.status`.
 * 
 * Reference: PRD/03-data-model.md
 */
enum class MediaAssetStatus(val value: String) {
    PENDING("pending"),
    APPROVED("approved"),
    REJECTED("rejected"),
    FAILED("failed"),
    DELETED("deleted"),
    LEGACY_UNMODERATED("legacy_unmoderated");
    
    companion object {
        fun fromValue(value: String?): MediaAssetStatus? {
            return entries.find { it.value == value }
        }
    }
}
