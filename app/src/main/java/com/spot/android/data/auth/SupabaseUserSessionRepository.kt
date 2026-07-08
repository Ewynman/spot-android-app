package com.spot.android.data.auth

import com.spot.android.core.logging.LogCategory
import com.spot.android.core.logging.SpotLogger
import com.spot.android.core.supabase.SupabaseClientProvider
import com.spot.android.data.dto.SpotBookmarkRowDto
import com.spot.android.data.dto.SpotLikeRowDto
import com.spot.android.data.dto.UserBlockRowDto
import com.spot.android.data.dto.UserRowDto
import com.spot.android.data.mapper.UserMapper
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Loads session snapshot data from Supabase tables.
 */
@Singleton
class SupabaseUserSessionRepository @Inject constructor(
    private val supabaseProvider: SupabaseClientProvider,
    private val userSessionHolder: UserSessionHolder,
    private val logger: SpotLogger,
) : UserSessionRepository {

    private val client get() = supabaseProvider.client
    private val postgrest get() = supabaseProvider.client.postgrest

    override suspend fun loadSessionSnapshot(userId: String): Result<UserSessionSnapshot> {
        return try {
            val userRow = postgrest.from("users")
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingle<UserRowDto>()

            val user = UserMapper.fromUserRow(userRow)

            val likedSpots = postgrest.from("spot_likes")
                .select(columns = Columns.list("spot_id")) {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<SpotLikeRowDto>()
                .map { it.spot_id }
                .toSet()

            val bookmarkedSpots = postgrest.from("spot_bookmarks")
                .select(columns = Columns.list("spot_id")) {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<SpotBookmarkRowDto>()
                .map { it.spot_id }
                .toSet()

            val blockedUsers = postgrest.from("user_blocks")
                .select(columns = Columns.list("blocked_user_id")) {
                    filter {
                        eq("blocker_id", userId)
                    }
                }
                .decodeList<UserBlockRowDto>()
                .map { it.blocked_user_id }
                .toSet()

            // Custom vibe tag ownership is resolved during post flow (PRD/08); no owner column on vibe_tags.
            val customVibeTags = emptyList<String>()

            val needsUsernameSetup = user.username.isBlank()

            Result.success(
                UserSessionSnapshot(
                    username = user.username,
                    email = user.email,
                    profileImageURL = user.profileImageURL,
                    isPro = user.isPro,
                    proUntil = user.proUntil,
                    emailVerified = user.emailVerified,
                    isPrivate = user.isPrivate,
                    likedSpots = likedSpots,
                    bookmarkedSpots = bookmarkedSpots,
                    blockedUsers = blockedUsers,
                    customVibeTags = customVibeTags,
                    needsUsernameSetup = needsUsernameSetup,
                ),
            )
        } catch (e: Exception) {
            logger.e(LogCategory.Auth, "Failed to load session snapshot", e)
            Result.failure(e)
        }
    }

    override suspend fun updatePrivateAccount(isPrivate: Boolean): Result<Unit> = runCatching {
        logger.d(LogCategory.Auth, "Updating private account setting: $isPrivate")
        
        val currentUser = client.auth.currentUserOrNull()
            ?: throw IllegalStateException("No authenticated user")

        val username = userSessionHolder.currentUserUsername.value
        val params = buildJsonObject {
            put("p_username", username ?: "")
            put("p_username_lower", username?.lowercase() ?: "")
            put("p_is_private", isPrivate)
            currentUser.email?.let {
                put("p_email", it)
                put("p_email_verified", currentUser.emailConfirmedAt != null)
            }
        }

        postgrest.rpc("sync_current_user_v1", params)
        
        // Reload session to get updated state
        currentUser.id.let { userId ->
            loadSessionSnapshot(userId)
        }

        logger.d(LogCategory.Auth, "Private account setting updated successfully")
    }

    override suspend fun isPrivateAccount(): Boolean? {
        // Query from the database since UserSessionHolder doesn't track isPrivate
        val currentUser = client.auth.currentUserOrNull() ?: return null
        return try {
            val userRow = postgrest.from("users")
                .select {
                    filter {
                        eq("id", currentUser.id)
                    }
                }
                .decodeSingle<UserRowDto>()
            userRow.is_private
        } catch (e: Exception) {
            logger.e(LogCategory.Auth, "Failed to check private status", e)
            null
        }
    }

    private companion object {
        const val TAG = "SupabaseUserSessionRepository"
    }
}
