package com.spot.android.data.feed

import com.spot.android.core.logging.LogCategory
import com.spot.android.core.logging.SpotLogger
import com.spot.android.core.supabase.SessionBridge
import com.spot.android.core.supabase.SupabaseClientProvider
import com.spot.android.data.dto.HomeFeedRowDto
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Supabase-backed home feed repository.
 */
@Singleton
class SupabaseFeedRepository @Inject constructor(
    private val supabaseProvider: SupabaseClientProvider,
    private val sessionBridge: SessionBridge,
    private val logger: SpotLogger,
) : FeedRepository {

    private val postgrest get() = supabaseProvider.client.postgrest

    override suspend fun getHomeFeed(
        limit: Int,
        batchId: String,
        viewerLat: Double?,
        viewerLng: Double?,
        forceSeenFallback: Boolean,
    ): Result<List<HomeFeedRowDto>> {
        return try {
            val rows = postgrest.rpc(
                function = "get_home_feed_v1",
                parameters = GetHomeFeedRpcParams(
                    p_limit = limit,
                    p_viewer_lat = viewerLat,
                    p_viewer_lng = viewerLng,
                    p_batch_id = batchId,
                    p_force_seen_fallback = forceSeenFallback,
                ),
            ).decodeList<HomeFeedRowDto>()
            Result.success(rows)
        } catch (e: Exception) {
            logger.e(LogCategory.Feed, TAG, "Failed to load home feed", e)
            Result.failure(e)
        }
    }

    override suspend fun getHomeFeedStatus(): Result<HomeFeedStatusDto> {
        return try {
            val status = postgrest.rpc(
                function = "get_home_feed_status_v1",
            ).decodeList<HomeFeedStatusDto>().firstOrNull()
                ?: return Result.failure(IllegalStateException("Empty feed status response"))
            Result.success(status)
        } catch (e: Exception) {
            logger.e(LogCategory.Feed, TAG, "Failed to load home feed status", e)
            Result.failure(e)
        }
    }

    override suspend fun getTopVibeNamesForDiversity(): Result<List<String>> {
        val userId = sessionBridge.currentUserId
            ?: return Result.success(emptyList())

        return try {
            val profileRow = postgrest.from("user_feed_profiles")
                .select(columns = Columns.list("profile")) {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<UserFeedProfileRowDto>()
                .firstOrNull()

            val topVibes = profileRow?.profile
                ?.jsonObject
                ?.get("top_vibes")
                ?.jsonArray
                ?.mapNotNull { element ->
                    element.jsonObject["name"]?.jsonPrimitive?.contentOrNull
                }
                .orEmpty()

            Result.success(topVibes)
        } catch (e: Exception) {
            logger.w(LogCategory.Feed, TAG, "Failed to load feed profile for diversity", e)
            Result.success(emptyList())
        }
    }

    private companion object {
        const val TAG = "SupabaseFeedRepository"
    }
}

@kotlinx.serialization.Serializable
private data class UserFeedProfileRowDto(
    val profile: JsonObject,
)
