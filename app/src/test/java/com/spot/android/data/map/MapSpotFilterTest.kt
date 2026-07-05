package com.spot.android.data.map

import com.spot.android.data.model.Spot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MapSpotFilterTest {

    private val spotA = sampleSpot(id = "a", vibe = "Chill Spot", userId = "user-1")
    private val spotB = sampleSpot(id = "b", vibe = "Hidden Gem", userId = "user-2")
    private val spotC = sampleSpot(id = "c", vibe = "Scenic View", userId = "user-3")

    @Test
    fun `no active filters returns all spots`() {
        val result = MapSpotFilterEngine.applyFilters(
            spots = listOf(spotA, spotB),
            activeFilters = emptySet(),
            selectedVibeNames = emptySet(),
            likedSpotIds = emptySet(),
            bookmarkedSpotIds = emptySet(),
            followedUserIds = emptySet(),
        )
        assertEquals(2, result.size)
    }

    @Test
    fun `vibe filter matches selected vibes with OR semantics`() {
        val result = MapSpotFilterEngine.applyFilters(
            spots = listOf(spotA, spotB, spotC),
            activeFilters = setOf(SpotMapFilter.VIBE),
            selectedVibeNames = setOf("Chill Spot", "Hidden Gem"),
            likedSpotIds = emptySet(),
            bookmarkedSpotIds = emptySet(),
            followedUserIds = emptySet(),
        )
        assertEquals(2, result.size)
        assertTrue(result.any { it.id == "a" })
        assertTrue(result.any { it.id == "b" })
    }

    @Test
    fun `saved and liked filters combine with AND`() {
        val liked = spotA.copy(isLiked = true)
        val saved = spotB.copy(isSaved = true)
        val result = MapSpotFilterEngine.applyFilters(
            spots = listOf(liked, saved, spotC),
            activeFilters = setOf(SpotMapFilter.SAVED, SpotMapFilter.LIKED),
            selectedVibeNames = emptySet(),
            likedSpotIds = setOf("a"),
            bookmarkedSpotIds = setOf("b"),
            followedUserIds = emptySet(),
        )
        assertTrue(result.isEmpty())

        val both = spotA.copy(isLiked = true, isSaved = true)
        val bothResult = MapSpotFilterEngine.applyFilters(
            spots = listOf(both),
            activeFilters = setOf(SpotMapFilter.SAVED, SpotMapFilter.LIKED),
            selectedVibeNames = emptySet(),
            likedSpotIds = setOf("a"),
            bookmarkedSpotIds = setOf("a"),
            followedUserIds = emptySet(),
        )
        assertEquals(1, bothResult.size)
    }

    @Test
    fun `following filter uses followed user ids`() {
        val result = MapSpotFilterEngine.applyFilters(
            spots = listOf(spotA, spotB),
            activeFilters = setOf(SpotMapFilter.FOLLOWING),
            selectedVibeNames = emptySet(),
            likedSpotIds = emptySet(),
            bookmarkedSpotIds = emptySet(),
            followedUserIds = setOf("user-2"),
        )
        assertEquals(1, result.size)
        assertEquals("b", result.first().id)
    }

    @Test
    fun `isSpotVisibleAfterFilter returns false when filtered out`() {
        val visible = MapSpotFilterEngine.isSpotVisibleAfterFilter(
            spot = spotC,
            activeFilters = setOf(SpotMapFilter.VIBE),
            selectedVibeNames = setOf("Chill Spot"),
            likedSpotIds = emptySet(),
            bookmarkedSpotIds = emptySet(),
            followedUserIds = emptySet(),
        )
        assertFalse(visible)
    }

    private fun sampleSpot(
        id: String,
        vibe: String,
        userId: String,
    ): Spot {
        return Spot(
            id = id,
            userId = userId,
            username = "user",
            userProfileImageURL = null,
            caption = "caption",
            latitude = 40.0,
            longitude = -74.0,
            locationName = "NYC",
            likes = 0,
            saves = 0,
            createdAt = 0L,
            updatedAt = null,
            imageURL = null,
            thumbnailURL = null,
            mediaDisplayAspectRatio = 1.0,
            mediaCount = 1,
            vibeTag = vibe,
            authorIsPrivate = false,
        )
    }
}
