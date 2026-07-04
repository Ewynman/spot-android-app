package com.spot.android.data.feed

/**
 * Empty-state reasons from `get_home_feed_status_v1`.
 *
 * Reference: PRD/06-home-feed.md
 */
enum class HomeFeedEmptyReason {
    CAUGHT_UP,
    NO_ELIGIBLE_SPOTS,
    NO_SPOTS_GLOBAL,
    ;

    companion object {
        fun fromStatus(status: String?): HomeFeedEmptyReason? {
            return when (status) {
                "caught_up" -> CAUGHT_UP
                "no_eligible_spots" -> NO_ELIGIBLE_SPOTS
                "no_spots_global" -> NO_SPOTS_GLOBAL
                else -> null
            }
        }
    }
}
