package com.spot.android.data.settings

import com.spot.android.core.logging.LogCategory
import com.spot.android.core.logging.SpotLogger
import com.spot.android.core.supabase.SupabaseClientProvider
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.user.UserInfo
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Supabase implementation of SettingsRepository.
 *
 * Reference: PRD/11-settings.md, PRD/04-backend-api.md
 */
@Singleton
class SupabaseSettingsRepository @Inject constructor(
    private val supabaseClientProvider: SupabaseClientProvider,
    private val logger: SpotLogger,
) : SettingsRepository {

    private val client get() = supabaseClientProvider.client

    override suspend fun changePassword(
        currentPassword: String,
        newPassword: String,
    ): Result<Unit> = runCatching {
        logger.d(LogCategory.Auth, "Changing user password")
        
        // Supabase requires re-authentication before password change
        val currentUser = client.auth.currentUserOrNull()
            ?: throw IllegalStateException("No authenticated user")
        
        val email = currentUser.email
            ?: throw IllegalStateException("User has no email")

        // Re-authenticate with current password
        client.auth.signInWith(io.github.jan.supabase.gotrue.providers.builtin.Email) {
            this.email = email
            password = currentPassword
        }

        // Update password
        client.auth.updateUser {
            password = newPassword
        }

        logger.d(LogCategory.Auth, "Password changed successfully")
    }

    override suspend fun deleteAccount(
        password: String?,
        useOAuthReauth: Boolean,
    ): Result<Unit> = runCatching {
        logger.d(LogCategory.Auth, "Deleting user account")

        val currentUser = client.auth.currentUserOrNull()
            ?: throw IllegalStateException("No authenticated user")

        // Re-authenticate before deletion
        if (useOAuthReauth) {
            // OAuth re-authentication
            // Note: This requires platform-specific OAuth flow
            // For now, we'll just verify the user is authenticated via OAuth
            val identities = currentUser.identities
            if (identities.isEmpty()) {
                throw IllegalStateException("OAuth re-authentication required")
            }
            logger.d(LogCategory.Auth, "OAuth user verified for deletion")
        } else {
            // Password re-authentication
            if (password == null) {
                throw IllegalArgumentException("Password required for re-authentication")
            }
            val email = currentUser.email
                ?: throw IllegalStateException("User has no email")
            
            client.auth.signInWith(io.github.jan.supabase.gotrue.providers.builtin.Email) {
                this.email = email
                this.password = password
            }
        }

        // Call the delete_my_account RPC
        postgrest.rpc("delete_my_account")

        // Sign out after deletion
        client.auth.signOut()

        logger.d(LogCategory.Auth, "Account deleted successfully")
    }

    override suspend fun updateEmail(newEmail: String): Result<Unit> = runCatching {
        logger.d(LogCategory.Auth, "Updating user email")
        
        client.auth.updateUser {
            email = newEmail
        }

        logger.d(LogCategory.Auth, "Email updated successfully")
    }

    override suspend fun updateProfile(
        username: String?,
        displayName: String?,
        profileImageUrl: String?,
        profileImageAssetId: String?,
    ): Result<Unit> = runCatching {
        logger.d(LogCategory.Auth, "Updating user profile")

        val currentUser = client.auth.currentUserOrNull()
            ?: throw IllegalStateException("No authenticated user")

        // Build the sync request
        val params = buildJsonObject {
            username?.let { 
                put("p_username", it)
                put("p_username_lower", it.lowercase())
            }
            displayName?.let { put("p_display_name", it) }
            profileImageUrl?.let { put("p_profile_image_url", it) }
            profileImageAssetId?.let { put("p_profile_image_asset_id", it) }
            // Always include email for sync
            currentUser.email?.let {
                put("p_email", it)
                put("p_email_verified", currentUser.emailConfirmedAt != null)
            }
        }

        postgrest.rpc("sync_current_user_v1", params)

        logger.d(LogCategory.Auth, "Profile updated successfully")
    }
}
