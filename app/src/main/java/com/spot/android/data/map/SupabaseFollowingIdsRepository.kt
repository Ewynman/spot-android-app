package com.spot.android.data.map

import com.spot.android.core.logging.LogCategory
import com.spot.android.core.logging.SpotLogger
import com.spot.android.core.supabase.SessionBridge
import com.spot.android.core.supabase.SupabaseClientProvider
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.Serializable

/**
 * Loads followed user ids from the `follows` table.
 */
@Singleton
class SupabaseFollowingIdsRepository @Inject constructor(
    private val supabaseProvider: SupabaseClientProvider,
    private val sessionBridge: SessionBridge,
    private val logger: SpotLogger,
) : FollowingIdsRepository {

    private val postgrest get() = supabaseProvider.client.postgrest

    override suspend fun getFollowedUserIds(): Result<Set<String>> {
        val userId = sessionBridge.currentUserId
            ?: return Result.success(emptySet())

        return try {
            val ids = postgrest.from("follows")
                .select(columns = Columns.list("followee_id")) {
                    filter {
                        eq("follower_id", userId)
                    }
                }
                .decodeList<FollowRowDto>()
                .map { it.followee_id }
                .toSet()
            Result.success(ids)
        } catch (e: Exception) {
            logger.w(LogCategory.Map, TAG, "Failed to load following ids", e)
            Result.failure(e)
        }
    }

    private companion object {
        const val TAG = "SupabaseFollowingIdsRepository"
    }
}

@Serializable
private data class FollowRowDto(
    val followee_id: String,
)
