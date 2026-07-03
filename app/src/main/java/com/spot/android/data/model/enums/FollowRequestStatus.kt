package com.spot.android.data.model.enums

/**
 * Follow request status values from `follow_requests.status`.
 * 
 * Reference: PRD/03-data-model.md
 */
enum class FollowRequestStatus(val value: String) {
    PENDING("pending"),
    ACCEPTED("accepted"),
    REJECTED("rejected"),
    CANCELLED("cancelled");
    
    companion object {
        fun fromValue(value: String?): FollowRequestStatus? {
            return entries.find { it.value == value }
        }
    }
}
