package com.spot.android.data.dto

import kotlinx.serialization.Serializable

/**
 * Row returned by vibe/location search grid RPCs.
 *
 * Reference: PRD/04-backend-api.md
 */
@Serializable
data class SearchSpotIdRowDto(
    val spot_id: String,
    val created_at: String,
)
