package com.spot.android.data.search

import com.spot.android.core.logging.LogCategory
import com.spot.android.core.logging.SpotLogger
import com.spot.android.core.supabase.SupabaseClientProvider
import com.spot.android.core.util.Constants
import com.spot.android.data.dto.SearchSpotIdRowDto
import com.spot.android.data.dto.UserBriefRowDto
import com.spot.android.data.dto.VibeRowDto
import com.spot.android.data.mapper.SpotMapper
import com.spot.android.data.mapper.UserMapper
import com.spot.android.data.model.User
import com.spot.android.data.model.VibeTag
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Supabase-backed search repository.
 *
 * Reference: PRD/09-search.md, PRD/04-backend-api.md
 */
@Singleton
class SupabaseSearchRepository @Inject constructor(
    private val supabaseProvider: SupabaseClientProvider,
    private val logger: SpotLogger,
) : SearchRepository {

    private val postgrest get() = supabaseProvider.client.postgrest

    override suspend fun searchUsers(query: String): Result<List<User>> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return Result.success(emptyList())

        return try {
            val rows = postgrest.from("users_public")
                .select {
                    filter {
                        ilike("username_lower", "${escapeIlike(trimmed.lowercase())}%")
                    }
                    order("username_lower", Order.ASCENDING)
                    limit(24)
                }
                .decodeList<UserBriefRowDto>()
            Result.success(rows.map { UserMapper.fromUserBriefRow(it) })
        } catch (e: Exception) {
            logger.e(LogCategory.Network, TAG, "User search failed", e)
            Result.failure(e)
        }
    }

    override suspend fun searchLocations(query: String): Result<List<String>> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return Result.success(emptyList())

        return try {
            val rows = postgrest.from("spots")
                .select(columns = io.github.jan.supabase.postgrest.query.Columns.list("location_name")) {
                    filter {
                        ilike("location_name", "%${escapeIlike(trimmed)}%")
                    }
                    order("location_name", Order.ASCENDING)
                    limit(100)
                }
                .decodeList<LocationNameRowDto>()

            val distinct = rows
                .mapNotNull { it.location_name?.trim()?.takeIf { name -> name.isNotEmpty() } }
                .distinct()
                .take(24)
            Result.success(distinct)
        } catch (e: Exception) {
            logger.e(LogCategory.Network, TAG, "Location search failed", e)
            Result.failure(e)
        }
    }

    override suspend fun searchVibes(query: String): Result<List<VibeTag>> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return listAllVibeTags()

        return try {
            val rows = postgrest.from("vibe_tags")
                .select {
                    filter {
                        ilike("name_lower", "%${escapeIlike(trimmed.lowercase())}%")
                    }
                    order("name_lower", Order.ASCENDING)
                    limit(24)
                }
                .decodeList<VibeRowDto>()
            Result.success(rows.map { SpotMapper.fromVibeRow(it) })
        } catch (e: Exception) {
            logger.e(LogCategory.Network, TAG, "Vibe search failed", e)
            Result.failure(e)
        }
    }

    override suspend fun listAllVibeTags(): Result<List<VibeTag>> {
        return try {
            val rows = postgrest.from("vibe_tags")
                .select {
                    order("name_lower", Order.ASCENDING)
                }
                .decodeList<VibeRowDto>()
            Result.success(rows.map { SpotMapper.fromVibeRow(it) })
        } catch (e: Exception) {
            logger.e(LogCategory.Network, TAG, "Failed to list vibe tags", e)
            Result.failure(e)
        }
    }

    override suspend fun fetchSpotIds(
        request: SearchGridRequest,
        offset: Int,
        limit: Int,
    ): Result<List<String>> {
        return try {
            when (request) {
                is SearchGridRequest.Vibe -> {
                    val rows = postgrest.rpc(
                        function = "list_spot_ids_for_vibe_search_v1",
                        parameters = ListSpotIdsForVibeSearchRpcParams(
                            p_vibe_tag_ids = request.vibeTagIds,
                            p_limit = limit,
                            p_offset = offset,
                        ),
                    ).decodeList<SearchSpotIdRowDto>()
                    Result.success(rows.map { it.spot_id })
                }

                is SearchGridRequest.Location -> {
                    if (request.vibeTagIds.isEmpty()) {
                        val pattern = "%${escapeIlike(request.locationPattern)}%"
                        val rows = postgrest.from("spots")
                            .select(columns = io.github.jan.supabase.postgrest.query.Columns.list("id")) {
                                filter {
                                    ilike("location_name", pattern)
                                }
                                order("created_at", Order.DESCENDING)
                                range(offset.toLong(), (offset + limit - 1).toLong())
                            }
                            .decodeList<SpotIdOnlyRowDto>()
                        Result.success(rows.map { it.id })
                    } else {
                        val rows = postgrest.rpc(
                            function = "list_spot_ids_for_location_and_vibe_search_v1",
                            parameters = ListSpotIdsForLocationAndVibeSearchRpcParams(
                                p_location_pattern = "%${escapeIlike(request.locationPattern)}%",
                                p_vibe_tag_ids = request.vibeTagIds,
                                p_limit = limit,
                                p_offset = offset,
                            ),
                        ).decodeList<SearchSpotIdRowDto>()
                        Result.success(rows.map { it.spot_id })
                    }
                }
            }
        } catch (e: Exception) {
            logger.e(LogCategory.Network, TAG, "Search grid id fetch failed", e)
            Result.failure(e)
        }
    }

    private fun escapeIlike(value: String): String {
        return value
            .replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_")
    }

    private companion object {
        const val TAG = "SupabaseSearchRepository"
    }
}

@kotlinx.serialization.Serializable
private data class SpotIdOnlyRowDto(
    val id: String,
)
