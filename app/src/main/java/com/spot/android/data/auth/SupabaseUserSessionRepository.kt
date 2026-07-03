package com.spot.android.data.auth

import com.spot.android.core.logging.LogCategory
import com.spot.android.core.logging.SpotLogger
import com.spot.android.core.supabase.SessionBridge
import com.spot.android.core.supabase.SupabaseClientProvider
import com.spot.android.data.dto.SpotBookmarkRowDto
import com.spot.android.data.dto.SpotLikeRowDto
import com.spot.android.data.dto.UserBlockRowDto
import com.spot.android.data.dto.UserRowDto
import com.spot.android.data.mapper.UserMapper
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Loads session snapshot data from Supabase tables.
 */
@Singleton
class SupabaseUserSessionRepository @Inject constructor(
    private val supabaseProvider: SupabaseClientProvider,
    private val logger: SpotLogger,
) : UserSessionRepository {

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
                    profileImageURL = user.profileImageURL,
                    isPro = user.isPro,
                    proUntil = user.proUntil,
                    emailVerified = user.emailVerified,
                    likedSpots = likedSpots,
                    bookmarkedSpots = bookmarkedSpots,
                    blockedUsers = blockedUsers,
                    customVibeTags = customVibeTags,
                    needsUsernameSetup = needsUsernameSetup,
                ),
            )
        } catch (e: Exception) {
            logger.e(LogCategory.Auth, TAG, "Failed to load session snapshot", e)
            Result.failure(e)
        }
    }

    private companion object {
        const val TAG = "SupabaseUserSessionRepository"
    }
}
