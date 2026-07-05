package com.spot.android.data.model

/**
 * Incoming follow request with requester profile enrichment.
 *
 * Reference: PRD/10-profile-social.md
 */
data class FollowRequest(
    val id: String,
    val requester: User,
    val createdAt: Long?,
)
