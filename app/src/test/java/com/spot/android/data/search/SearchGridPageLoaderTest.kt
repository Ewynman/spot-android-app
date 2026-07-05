package com.spot.android.data.search

import com.spot.android.data.model.Spot
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchGridPageLoaderTest {

    private val repository = FakeSearchRepository()
    private val hydrator = mockk<SearchSpotHydrator>()
    private val loader = SearchGridPageLoader(repository, hydrator)

    @Test
    fun `loadPage fills page across multiple attempts when hydration drops rows`() = runTest {
        val request = SearchGridRequest.Vibe(vibeTagIds = listOf("vibe-1"))
        repository.spotIdsByRequest = mapOf(
            request to List(100) { index -> "spot-$index" },
        )

        coEvery { hydrator.hydrateByIds(any()) } answers {
            val ids = firstArg<List<String>>()
            ids.take(5).map { sampleSpot(it) }
        }

        val result = loader.loadPage(request = request, currentOffset = 0).getOrThrow()

        assertEquals(24, result.spots.size)
        assertTrue(result.hasMore)
        assertEquals(70, result.nextOffset)
    }

    @Test
    fun `loadPage stops when repository returns empty ids`() = runTest {
        val request = SearchGridRequest.Location(locationPattern = "Park")
        repository.spotIdsByRequest = emptyMap()

        val result = loader.loadPage(request = request, currentOffset = 0).getOrThrow()

        assertTrue(result.spots.isEmpty())
        assertFalse(result.hasMore)
        assertEquals(0, result.nextOffset)
    }

    private fun sampleSpot(id: String): Spot {
        return Spot(
            id = id,
            userId = "user-1",
            username = "tester",
            userProfileImageURL = null,
            caption = "caption",
            latitude = 0.0,
            longitude = 0.0,
            locationName = "Park",
            likes = 0,
            saves = 0,
            createdAt = 0L,
            updatedAt = null,
            imageURL = null,
            thumbnailURL = null,
            mediaDisplayAspectRatio = 1.0,
            mediaCount = 1,
            vibeTag = "Chill Spot",
            authorIsPrivate = false,
        )
    }
}
