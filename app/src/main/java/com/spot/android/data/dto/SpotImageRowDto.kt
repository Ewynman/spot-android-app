package com.spot.android.data.dto

import kotlinx.serialization.Serializable

/**
 * DTO for `spot_images` table reads.
 * 
 * Field names match snake_case Postgres columns exactly.
 * 
 * Reference: PRD/03-data-model.md, PRD/04-backend-api.md
 */
@Serializable
data class SpotImageRowDto(
    val id: String,
    val spot_id: String,
    val storage_path: String,
    val public_url: String,
    val sort_index: Int,
    val created_at: String,
    val media_asset_id: String?,
    val storage_bucket: String,
    val width: Int?,
    val height: Int?,
    val aspect_ratio: Double?,
    val display_aspect_ratio: Double,
    val orientation: String
)
