package com.spot.android.data.settings

/**
 * Repository for settings-related operations: account management, password changes, deletion.
 *
 * Reference: PRD/11-settings.md, PRD/04-backend-api.md
 */
interface SettingsRepository {
    /**
     * Change the current user's password.
     * Requires current password for verification.
     */
    suspend fun changePassword(
        currentPassword: String,
        newPassword: String,
    ): Result<Unit>

    /**
     * Delete the current user's account after re-authentication.
     * Calls the `delete_my_account` RPC which purges all user data and storage.
     *
     * @param password Required for password-based accounts
     * @param useOAuthReauth If true, requires OAuth re-authentication for OAuth-only accounts
     */
    suspend fun deleteAccount(
        password: String? = null,
        useOAuthReauth: Boolean = false,
    ): Result<Unit>

    /**
     * Update the user's email address.
     * May trigger email verification.
     */
    suspend fun updateEmail(newEmail: String): Result<Unit>

    /**
     * Update profile fields (username, display name, profile photo).
     * Uses sync_current_user_v1 RPC for consistency.
     */
    suspend fun updateProfile(
        username: String? = null,
        displayName: String? = null,
        profileImageUrl: String? = null,
        profileImageAssetId: String? = null,
    ): Result<Unit>
}
