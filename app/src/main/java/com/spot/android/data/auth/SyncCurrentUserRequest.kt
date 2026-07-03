package com.spot.android.data.auth

/**
 * Parameters for the `sync_current_user_v1` RPC.
 *
 * Reference: PRD/04-backend-api.md
 */
data class SyncCurrentUserRequest(
    val username: String,
    val usernameLower: String,
    val email: String? = null,
    val emailVerified: Boolean = false,
    val isPrivate: Boolean = false,
    val locale: String? = null,
)
