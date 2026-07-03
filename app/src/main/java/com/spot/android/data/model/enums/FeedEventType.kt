package com.spot.android.data.model.enums

/**
 * Feed event types from `user_feed_events.event_type`.
 * 
 * Used with `record_feed_event_v1` RPC to log behavioral signals
 * that drive feed ranking and personalization.
 * 
 * Reference: PRD/03-data-model.md, PRD/04-backend-api.md
 */
enum class FeedEventType(val value: String) {
    IMPRESSION("impression"),
    VISIBLE_2S("visible_2s"),
    LONG_DWELL("long_dwell"),
    QUICK_SKIP("quick_skip"),
    DETAIL_OPEN("detail_open"),
    LIKE("like"),
    UNLIKE("unlike"),
    SAVE("save"),
    UNSAVE("unsave"),
    SHARE("share"),
    VIBE_TAP("vibe_tap"),
    PROFILE_TAP("profile_tap"),
    MAP_PIN_TAP("map_pin_tap"),
    HIDE("hide"),
    REPORT("report"),
    FOLLOW_AUTHOR("follow_author"),
    UNFOLLOW_AUTHOR("unfollow_author"),
    BLOCK_AUTHOR("block_author");
    
    companion object {
        fun fromValue(value: String?): FeedEventType? {
            return entries.find { it.value == value }
        }
    }
}
