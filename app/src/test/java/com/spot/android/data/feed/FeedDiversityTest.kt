package com.spot.android.data.feed

import com.spot.android.data.model.Spot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class FeedDiversityTest {

    @Test
    fun `apply interleaves when same vibe repeats too many times`() {
        val spots = listOf(
            spot("1", "Chill Spot"),
            spot("2", "Chill Spot"),
            spot("3", "Chill Spot"),
            spot("4", "Chill Spot"),
            spot("5", "Foodie Heaven"),
        )

        val result = FeedDiversity.apply(spots, topVibeNames = listOf("Chill Spot"))

        assertEquals(5, result.size)
        assertNotEquals(
            listOf("Chill Spot", "Chill Spot", "Chill Spot", "Chill Spot"),
            result.take(4).map { it.vibeTag },
        )
    }

    @Test
    fun `apply leaves short lists unchanged`() {
        val spots = listOf(spot("1", "Chill Spot"))
        val result = FeedDiversity.apply(spots, topVibeNames = emptyList())
        assertEquals(spots, result)
    }

    private fun spot(id: String, vibe: String): Spot {
        return Spot(
            id = id,
            userId = "user-$id",
            username = "user",
            userProfileImageURL = null,
            caption = "caption",
            latitude = 0.0,
            longitude = 0.0,
            locationName = null,
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
