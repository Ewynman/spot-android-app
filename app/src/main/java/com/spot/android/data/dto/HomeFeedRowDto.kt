package com.spot.android.data.dto

import kotlinx.serialization.Serializable

/**
 * DTO for `get_home_feed_v1` RPC response.
 * 
 * Field names match snake_case Postgres columns exactly.
 * This is NOT a global conversion; field names must match schema.
 * 
 * Reference: PRD/04-backend-api.md
 */
@Serializable
data class HomeFeedRowDto(
    val spot_id: String,
    val user_id: String,
    val vibe_tag_id: String?,
    val caption: String,
    val latitude: Double,
    val longitude: Double,
    val location_name: String?,
    val likes_count: Long,
    val saves_count: Long,
    val created_at: String, // ISO-8601 timestamptz
    val updated_at: String,
    val author_username: String,
    val author_profile_image_url: String?,
    val author_is_private: Boolean,
    val vibe_name: String?,
    val primary_storage_path: String?,
    val primary_public_url: String?,
    val source_bucket: String?,
    val rank_position: Int?,
    val ranking_score: Double?,
    val seen_before: Boolean?,
    val last_seen_at: String?,
    val media_display_aspect_ratio: Double?,
    val media_count: Int?
)
