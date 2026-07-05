package com.spot.android.data.profile

import com.spot.android.core.logging.LogCategory
import com.spot.android.core.logging.SpotLogger
import com.spot.android.core.supabase.SessionBridge
import com.spot.android.core.supabase.SupabaseClientProvider
import com.spot.android.data.dto.UserBriefRowDto
import com.spot.android.data.dto.UserRowDto
import com.spot.android.data.mapper.UserMapper
import com.spot.android.data.model.Spot
import com.spot.android.data.model.User
import com.spot.android.data.search.SearchSpotHydrator
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.Serializable

/**
 * Supabase-backed profile repository.
 */
@Singleton
class SupabaseProfileRepository @Inject constructor(
    private val supabaseProvider: SupabaseClientProvider,
    private val sessionBridge: SessionBridge,
    private val searchSpotHydrator: SearchSpotHydrator,
    private val logger: SpotLogger,
) : ProfileRepository {

    private val postgrest get() = supabaseProvider.client.postgrest

    override suspend fun getOwnProfile(): Result<User> {
        val userId = sessionBridge.currentUserId
            ?: return Result.failure(IllegalStateException("Not authenticated"))

        return try {
            val row = postgrest.from("users")
                .select {
                    filter { eq("id", userId) }
                }
                .decodeSingle<UserRowDto>()
            Result.success(UserMapper.fromUserRow(row))
        } catch (e: Exception) {
            logger.e(LogCategory.Network, TAG, "Failed to load own profile", e)
            Result.failure(e)
        }
    }

    override suspend fun getPublicProfile(userId: String): Result<User> {
        return try {
            val row = postgrest.from("users_public")
                .select {
                    filter { eq("id", userId) }
                }
                .decodeSingle<UserBriefRowDto>()
            val isCurrentUser = sessionBridge.currentUserId == userId
            Result.success(UserMapper.fromUserBriefRow(row, isCurrentUser = isCurrentUser))
        } catch (e: Exception) {
            logger.e(LogCategory.Network, TAG, "Failed to load public profile", e)
            Result.failure(e)
        }
    }

    override suspend fun getUserSpotIds(
        userId: String,
        offset: Int,
        limit: Int,
    ): Result<List<String>> {
        return try {
            val rows = postgrest.from("spots")
                .select(columns = Columns.list("id")) {
                    filter { eq("user_id", userId) }
                    order(column = "created_at", order = Order.DESCENDING)
                    range(offset.toLong(), (offset + limit - 1).toLong())
                }
                .decodeList<SpotIdRowDto>()
            Result.success(rows.map { it.id })
        } catch (e: Exception) {
            logger.e(LogCategory.Network, TAG, "Failed to load user spot ids", e)
            Result.failure(e)
        }
    }

    override suspend fun getLikedSpotIds(
        userId: String,
        offset: Int,
        limit: Int,
    ): Result<List<String>> {
        return loadEngagementSpotIds(
            table = "spot_likes",
            userId = userId,
            offset = offset,
            limit = limit,
        )
    }

    override suspend fun getBookmarkedSpotIds(
        userId: String,
        offset: Int,
        limit: Int,
    ): Result<List<String>> {
        return loadEngagementSpotIds(
            table = "spot_bookmarks",
            userId = userId,
            offset = offset,
            limit = limit,
        )
    }

    override suspend fun hydrateSpots(spotIds: List<String>): Result<List<Spot>> {
        return try {
            Result.success(searchSpotHydrator.hydrateByIds(spotIds))
        } catch (e: Exception) {
            logger.e(LogCategory.Network, TAG, "Failed to hydrate profile spots", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteOwnSpot(spotId: String): Result<Unit> {
        val userId = sessionBridge.currentUserId
            ?: return Result.failure(IllegalStateException("Not authenticated"))

        return try {
            postgrest.from("spots").delete {
                filter {
                    eq("id", spotId)
                    eq("user_id", userId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            logger.e(LogCategory.Network, TAG, "Failed to delete spot", e)
            Result.failure(e)
        }
    }

    private suspend fun loadEngagementSpotIds(
        table: String,
        userId: String,
        offset: Int,
        limit: Int,
    ): Result<List<String>> {
        return try {
            val spotIds = postgrest.from(table)
                .select(columns = Columns.list("spot_id")) {
                    filter { eq("user_id", userId) }
                    order(column = "created_at", order = Order.DESCENDING)
                    range(offset.toLong(), (offset + limit - 1).toLong())
                }
                .decodeList<EngagementSpotIdRowDto>()
                .map { it.spot_id }
            Result.success(spotIds)
        } catch (e: Exception) {
            logger.e(LogCategory.Network, TAG, "Failed to load engagement spot ids from $table", e)
            Result.failure(e)
        }
    }

    private companion object {
        const val TAG = "SupabaseProfileRepository"
    }
}

@Serializable
private data class SpotIdRowDto(
    val id: String,
)

@Serializable
private data class EngagementSpotIdRowDto(
    val spot_id: String,
)
