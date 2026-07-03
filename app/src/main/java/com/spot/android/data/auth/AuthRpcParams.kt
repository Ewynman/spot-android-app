package com.spot.android.data.auth

import kotlinx.serialization.Serializable

@Serializable
internal data class SyncCurrentUserRpcParams(
    val p_username: String,
    val p_username_lower: String,
    val p_email: String? = null,
    val p_email_verified: Boolean = false,
    val p_is_private: Boolean = false,
    val p_locale: String? = null,
    val p_last_active_at: String? = null,
)

@Serializable
internal data class ResolveLoginEmailRpcParams(
    val p_username: String,
)

@Serializable
internal data class IsUsernameAvailableRpcParams(
    val p_username: String,
)
