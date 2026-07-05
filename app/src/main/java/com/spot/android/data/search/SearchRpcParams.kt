package com.spot.android.data.search

import kotlinx.serialization.Serializable

@Serializable
internal data class ListSpotIdsForVibeSearchRpcParams(
    val p_vibe_tag_ids: List<String>,
    val p_limit: Int,
    val p_offset: Int,
)

@Serializable
internal data class ListSpotIdsForLocationAndVibeSearchRpcParams(
    val p_location_pattern: String,
    val p_vibe_tag_ids: List<String>,
    val p_limit: Int,
    val p_offset: Int,
)

@Serializable
internal data class LocationNameRowDto(
    val location_name: String?,
)
