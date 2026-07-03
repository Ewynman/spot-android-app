package com.spot.android.data.model.enums

/**
 * Reason values for hidden spots from `user_hidden_spots.reason`.
 * 
 * Reference: PRD/03-data-model.md
 */
enum class HiddenSpotReason(val value: String) {
    HIDE("hide"),
    REPORT("report"),
    NOT_INTERESTED("not_interested");
    
    companion object {
        fun fromValue(value: String?): HiddenSpotReason? {
            return entries.find { it.value == value }
        }
    }
}
