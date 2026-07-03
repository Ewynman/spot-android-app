package com.spot.android.data.dto

import kotlinx.serialization.Serializable

/**
 * DTO for `get_map_spots_v1` RPC response.
 * 
 * Field names match snake_case Postgres columns exactly.
 * 
 * Reference: PRD/04-backend-api.md
 */
@Serializable
data class MapSpotRowDto(
    val spot_id: String,
    val user_id: String,
    val vibe_tag_id: String?,
    val caption: String,
    val latitude: Double,
    val longitude: Double,
    val location_name: String?,
    val created_at: String, // ISO-8601 timestamptz
    val author_username: String,
    val author_profile_image_url: String?,
    val vibe_name: String?,
    val primary_storage_path: String?,
    val primary_public_url: String?,
    val distance_meters: Double?
)
