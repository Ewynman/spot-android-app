package com.spot.android.data.model.enums

/**
 * Media asset kind values from `media_assets.kind`.
 * 
 * Reference: PRD/03-data-model.md
 */
enum class MediaAssetKind(val value: String) {
    SPOT_IMAGE("spot_image"),
    PROFILE_IMAGE("profile_image");
    
    companion object {
        fun fromValue(value: String?): MediaAssetKind? {
            return entries.find { it.value == value }
        }
    }
}
