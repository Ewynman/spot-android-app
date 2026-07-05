package com.spot.android.data.search

import com.spot.android.data.model.User
import com.spot.android.data.model.VibeTag

class FakeSearchRepository : SearchRepository {
    var users: List<User> = emptyList()
    var locations: List<String> = emptyList()
    var vibes: List<VibeTag> = emptyList()
    var allVibeTags: List<VibeTag> = emptyList()
    var spotIdsByRequest: Map<SearchGridRequest, List<String>> = emptyMap()
    var shouldFail = false

    override suspend fun searchUsers(query: String): Result<List<User>> {
        if (shouldFail) return Result.failure(IllegalStateException("search failed"))
        return Result.success(
            users.filter { it.username.startsWith(query, ignoreCase = true) },
        )
    }

    override suspend fun searchLocations(query: String): Result<List<String>> {
        if (shouldFail) return Result.failure(IllegalStateException("search failed"))
        return Result.success(
            locations.filter { it.contains(query, ignoreCase = true) },
        )
    }

    override suspend fun searchVibes(query: String): Result<List<VibeTag>> {
        if (shouldFail) return Result.failure(IllegalStateException("search failed"))
        return Result.success(
            vibes.filter { it.name.contains(query, ignoreCase = true) },
        )
    }

    override suspend fun listAllVibeTags(): Result<List<VibeTag>> {
        if (shouldFail) return Result.failure(IllegalStateException("search failed"))
        return Result.success(allVibeTags)
    }

    override suspend fun fetchSpotIds(
        request: SearchGridRequest,
        offset: Int,
        limit: Int,
    ): Result<List<String>> {
        if (shouldFail) return Result.failure(IllegalStateException("search failed"))
        val ids = spotIdsByRequest[request].orEmpty()
        val slice = ids.drop(offset).take(limit)
        return Result.success(slice)
    }
}
