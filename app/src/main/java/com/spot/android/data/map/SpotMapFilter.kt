package com.spot.android.data.map

import com.spot.android.data.model.Spot

/**
 * Pro map filter dimensions.
 *
 * Reference: PRD/07-map.md
 */
enum class SpotMapFilter {
    VIBE,
    SAVED,
    LIKED,
    FOLLOWING,
}

/**
 * Client-side map filter logic. Filters combine as AND across dimensions,
 * OR within selected vibes.
 */
object MapSpotFilterEngine {

    fun applyFilters(
        spots: Collection<Spot>,
        activeFilters: Set<SpotMapFilter>,
        selectedVibeNames: Set<String>,
        likedSpotIds: Set<String>,
        bookmarkedSpotIds: Set<String>,
        followedUserIds: Set<String>,
    ): List<Spot> {
        if (activeFilters.isEmpty()) return spots.toList()

        return spots.filter { spot ->
            activeFilters.all { filter ->
                when (filter) {
                    SpotMapFilter.VIBE -> {
                        selectedVibeNames.isEmpty() ||
                            spot.vibeTag?.let { it in selectedVibeNames } == true
                    }
                    SpotMapFilter.SAVED -> {
                        spot.isSaved || bookmarkedSpotIds.contains(spot.id)
                    }
                    SpotMapFilter.LIKED -> {
                        spot.isLiked || likedSpotIds.contains(spot.id)
                    }
                    SpotMapFilter.FOLLOWING -> {
                        followedUserIds.contains(spot.userId)
                    }
                }
            }
        }
    }

    fun isSpotVisibleAfterFilter(
        spot: Spot,
        activeFilters: Set<SpotMapFilter>,
        selectedVibeNames: Set<String>,
        likedSpotIds: Set<String>,
        bookmarkedSpotIds: Set<String>,
        followedUserIds: Set<String>,
    ): Boolean {
        return applyFilters(
            spots = listOf(spot),
            activeFilters = activeFilters,
            selectedVibeNames = selectedVibeNames,
            likedSpotIds = likedSpotIds,
            bookmarkedSpotIds = bookmarkedSpotIds,
            followedUserIds = followedUserIds,
        ).isNotEmpty()
    }
}
