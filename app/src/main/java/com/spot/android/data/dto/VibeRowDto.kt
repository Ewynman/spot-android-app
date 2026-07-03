package com.spot.android.data.dto

import kotlinx.serialization.Serializable

/**
 * DTO for `vibe_tags` table reads.
 * 
 * Field names match snake_case Postgres columns exactly.
 * 
 * Reference: PRD/03-data-model.md, PRD/04-backend-api.md
 */
@Serializable
data class VibeRowDto(
    val id: String,
    val name: String,
    val name_lower: String,
    val created_at: String
)
