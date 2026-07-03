package com.spot.android.data.dto

import kotlinx.serialization.Serializable

/**
 * DTO for `spot_vibe_tags` junction table reads.
 * 
 * Field names match snake_case Postgres columns exactly.
 * 
 * Reference: PRD/03-data-model.md, PRD/04-backend-api.md
 */
@Serializable
data class SpotVibeJunctionRowDto(
    val spot_id: String,
    val vibe_tag_id: String,
    val sort_order: Int
)
