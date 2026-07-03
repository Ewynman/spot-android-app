package com.spot.android.data.dto

import kotlinx.serialization.Serializable

/**
 * DTO for direct `spots` table reads.
 * 
 * Field names match snake_case Postgres columns exactly.
 * 
 * Reference: PRD/03-data-model.md, PRD/04-backend-api.md
 */
@Serializable
data class SpotRowDto(
    val id: String,
    val user_id: String,
    val vibe_tag_id: String?,
    val caption: String,
    val latitude: Double,
    val longitude: Double,
    val geohash: String?,
    val location_name: String?,
    val likes_count: Long,
    val saves_count: Long,
    val author_is_private_snapshot: Boolean,
    val created_at: String, // ISO-8601 timestamptz
    val updated_at: String,
    val media_display_aspect_ratio: Double,
    val media_count: Int,
    val media_layout_version: Int,
    val moderation_status: String,
    val hidden_at: String?,
    val hidden_reason: String?
)
