package com.spot.android.data.dto

import kotlinx.serialization.Serializable

/**
 * DTO for `users_public` view reads.
 * 
 * Field names match snake_case Postgres columns exactly.
 * Safe public projection without email.
 * 
 * Reference: PRD/03-data-model.md, PRD/04-backend-api.md
 */
@Serializable
data class UserBriefRowDto(
    val id: String,
    val username: String,
    val profile_image_url: String?,
    val is_pro: Boolean,
    val pro_until: String?, // ISO-8601 timestamptz
    val is_private: Boolean,
    val spots_count: Long,
    val created_at: String
)
