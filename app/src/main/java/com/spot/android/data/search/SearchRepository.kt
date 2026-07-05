package com.spot.android.data.search

import com.spot.android.data.model.Spot
import com.spot.android.data.model.User
import com.spot.android.data.model.VibeTag

/**
 * Search RPC and query contract.
 *
 * Reference: PRD/09-search.md, PRD/04-backend-api.md
 */
interface SearchRepository {
    suspend fun searchUsers(query: String): Result<List<User>>
    suspend fun searchLocations(query: String): Result<List<String>>
    suspend fun searchVibes(query: String): Result<List<VibeTag>>
    suspend fun listAllVibeTags(): Result<List<VibeTag>>
    suspend fun fetchSpotIds(
        request: SearchGridRequest,
        offset: Int,
        limit: Int,
    ): Result<List<String>>
}

sealed interface SearchGridRequest {
    val locationPattern: String?

    data class Location(
        override val locationPattern: String,
        val vibeTagIds: List<String> = emptyList(),
    ) : SearchGridRequest

    data class Vibe(
        val vibeTagIds: List<String>,
    ) : SearchGridRequest {
        override val locationPattern: String? = null
    }
}

data class SearchGridPageResult(
    val spots: List<Spot>,
    val nextOffset: Int,
    val hasMore: Boolean,
)
