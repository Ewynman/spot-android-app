package com.spot.android.data.map

import kotlinx.serialization.Serializable

/**
 * Parameters for `get_map_spots_v1`.
 *
 * Reference: PRD/04-backend-api.md
 */
@Serializable
data class GetMapSpotsRpcParams(
    val p_min_lat: Double,
    val p_min_lng: Double,
    val p_max_lat: Double,
    val p_max_lng: Double,
    val p_center_lat: Double,
    val p_center_lng: Double,
    val p_limit: Int,
)
