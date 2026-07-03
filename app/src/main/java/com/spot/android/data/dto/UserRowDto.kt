package com.spot.android.data.dto

import kotlinx.serialization.Serializable

/**
 * DTO for `users` table reads (own profile only).
 * 
 * Field names match snake_case Postgres columns exactly.
 * Includes sensitive fields like email.
 * 
 * Reference: PRD/03-data-model.md
 */
@Serializable
data class UserRowDto(
    val id: String,
    val email: String?,
    val email_verified: Boolean,
    val username: String,
    val username_lower: String,
    val profile_image_url: String?,
    val is_private: Boolean,
    val is_pro: Boolean,
    val pro_until: String?, // ISO-8601 timestamptz
    val last_active_at: String?,
    val locale: String?,
    val spots_count: Long,
    val reported_count: Long,
    val created_at: String,
    val updated_at: String,
    val profile_image_asset_id: String?,
    val suspended_for_reports_at: String?,
    val account_status: String,
    val moderation_status: String
)
