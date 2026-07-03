package com.spot.android.data.dto

import kotlinx.serialization.Serializable

/**
 * DTO for `media_assets` table inserts.
 * 
 * Field names match snake_case Postgres columns exactly.
 * Used for uploading pending images before moderation.
 * 
 * Reference: PRD/03-data-model.md, PRD/04-backend-api.md
 */
@Serializable
data class MediaAssetDto(
    val id: String,
    val owner_id: String,
    val kind: String, // "spot_image" or "profile_image"
    val status: String = "pending",
    val pending_bucket: String = "pending_images",
    val pending_path: String, // "{userId}/{assetId}.jpg"
    val mime_type: String = "image/jpeg",
    val byte_size: Int?,
    val width: Int?,
    val height: Int?
)
