package com.spot.android.data.feed

import com.spot.android.core.logging.LogCategory
import com.spot.android.core.logging.SpotLogger
import com.spot.android.core.supabase.SessionBridge
import com.spot.android.core.supabase.SupabaseClientProvider
import io.github.jan.supabase.postgrest.postgrest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Supabase-backed engagement repository for likes and bookmarks.
 */
@Singleton
class SupabaseEngagementRepository @Inject constructor(
    private val supabaseProvider: SupabaseClientProvider,
    private val sessionBridge: SessionBridge,
    private val logger: SpotLogger,
) : EngagementRepository {

    private val postgrest get() = supabaseProvider.client.postgrest

    override suspend fun likeSpot(spotId: String): Result<Unit> {
        val userId = sessionBridge.currentUserId
            ?: return Result.failure(IllegalStateException("Not authenticated"))

        return try {
            postgrest.from("spot_likes").insert(
                SpotLikeInsertDto(user_id = userId, spot_id = spotId),
            )
            Result.success(Unit)
        } catch (e: Exception) {
            logger.e(LogCategory.Feed, TAG, "Failed to like spot", e)
            Result.failure(e)
        }
    }

    override suspend fun unlikeSpot(spotId: String): Result<Unit> {
        return deleteEngagement(table = "spot_likes", spotId = spotId, action = "unlike")
    }

    override suspend fun bookmarkSpot(spotId: String): Result<Unit> {
        val userId = sessionBridge.currentUserId
            ?: return Result.failure(IllegalStateException("Not authenticated"))

        return try {
            postgrest.from("spot_bookmarks").insert(
                SpotBookmarkInsertDto(user_id = userId, spot_id = spotId),
            )
            Result.success(Unit)
        } catch (e: Exception) {
            logger.e(LogCategory.Feed, TAG, "Failed to bookmark spot", e)
            Result.failure(e)
        }
    }

    override suspend fun unbookmarkSpot(spotId: String): Result<Unit> {
        return deleteEngagement(table = "spot_bookmarks", spotId = spotId, action = "unbookmark")
    }

    private suspend fun deleteEngagement(
        table: String,
        spotId: String,
        action: String,
    ): Result<Unit> {
        val userId = sessionBridge.currentUserId
            ?: return Result.failure(IllegalStateException("Not authenticated"))

        return try {
            postgrest.from(table).delete {
                filter {
                    eq("user_id", userId)
                    eq("spot_id", spotId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            logger.e(LogCategory.Feed, TAG, "Failed to $action spot", e)
            Result.failure(e)
        }
    }

    private companion object {
        const val TAG = "SupabaseEngagementRepository"
    }
}
