package com.spot.android.data.feed

import kotlinx.serialization.Serializable

/**
 * DTO for `get_home_feed_status_v1` RPC response.
 *
 * Reference: PRD/04-backend-api.md
 */
@Serializable
data class HomeFeedStatusDto(
    val total_spots: Int,
    val eligible_spots: Int,
    val unseen_eligible_spots: Int,
    val seen_eligible_spots: Int,
    val status: String,
)
