package com.spot.android.data.dto

import kotlinx.serialization.Serializable

/**
 * DTO for `follow_requests` table reads.
 *
 * Reference: PRD/03-data-model.md
 */
@Serializable
data class FollowRequestRowDto(
    val id: String,
    val requester_id: String,
    val target_user_id: String,
    val status: String,
    val created_at: String,
)
