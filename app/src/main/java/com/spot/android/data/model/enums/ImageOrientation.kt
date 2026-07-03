package com.spot.android.data.model.enums

/**
 * Image orientation values from `spot_images.orientation`.
 * 
 * Reference: PRD/03-data-model.md
 */
enum class ImageOrientation(val value: String) {
    LANDSCAPE("landscape"),
    SQUARE("square"),
    PORTRAIT("portrait");
    
    companion object {
        fun fromValue(value: String?): ImageOrientation? {
            return entries.find { it.value == value }
        }
    }
}
