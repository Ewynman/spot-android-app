package com.spot.android.data.search

import com.spot.android.core.util.Constants
import com.spot.android.data.model.Spot
import com.spot.android.data.model.User
import com.spot.android.data.model.VibeTag
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Loads search result grids with the 24-target / 5-attempt fill semantics.
 *
 * Reference: PRD/09-search.md
 */
@Singleton
class SearchGridPageLoader @Inject constructor(
    private val searchRepository: SearchRepository,
    private val searchSpotHydrator: SearchSpotHydrator,
) {
    suspend fun loadInitialPage(request: SearchGridRequest): Result<SearchGridPageResult> {
        return loadPage(request = request, currentOffset = 0)
    }

    suspend fun loadNextPage(
        request: SearchGridRequest,
        currentOffset: Int,
    ): Result<SearchGridPageResult> {
        return loadPage(request = request, currentOffset = currentOffset)
    }

    internal suspend fun loadPage(
        request: SearchGridRequest,
        currentOffset: Int,
    ): Result<SearchGridPageResult> {
        val pageTarget = Constants.Search.GRID_PAGE_TARGET
        val hydrated = mutableListOf<Spot>()
        var offset = currentOffset
        var attempts = 0
        var lastBatchSize = 0
        var lastRequestedLimit = pageTarget

        while (hydrated.size < pageTarget && attempts < Constants.Search.GRID_MAX_FETCH_ATTEMPTS) {
            attempts++
            lastRequestedLimit = pageTarget - hydrated.size
            val idsResult = searchRepository.fetchSpotIds(
                request = request,
                offset = offset,
                limit = lastRequestedLimit,
            )
            val ids = idsResult.getOrElse { return Result.failure(it) }
            lastBatchSize = ids.size
            if (ids.isEmpty()) {
                return Result.success(
                    SearchGridPageResult(
                        spots = hydrated,
                        nextOffset = offset,
                        hasMore = false,
                    ),
                )
            }

            hydrated += searchSpotHydrator.hydrateByIds(ids)
            offset += ids.size
        }

        return Result.success(
            SearchGridPageResult(
                spots = hydrated,
                nextOffset = offset,
                hasMore = lastBatchSize >= lastRequestedLimit,
            ),
        )
    }
}
