package com.spot.android.feature.home

import com.spot.android.core.util.cityStateFromLocation
import com.spot.android.data.model.Spot
import com.spot.android.data.model.hasValidCoordinates
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for the pure model helpers backing the place-first Home Spot card
 * (task 13): coordinate validity gate that toggles the map-flip control and
 * the shared location-format helper that produces the place header string.
 */
class HomeSpotCardModelTest {

    @Test
    fun `hasValidCoordinates rejects 0,0 (unset iOS sentinel)`() {
        assertFalse(sampleSpot(lat = 0.0, lng = 0.0).hasValidCoordinates)
    }

    @Test
    fun `hasValidCoordinates rejects NaN`() {
        assertFalse(sampleSpot(lat = Double.NaN, lng = -122.4).hasValidCoordinates)
        assertFalse(sampleSpot(lat = 37.7, lng = Double.NaN).hasValidCoordinates)
    }

    @Test
    fun `hasValidCoordinates rejects out-of-range values`() {
        assertFalse(sampleSpot(lat = 91.0, lng = 0.0).hasValidCoordinates)
        assertFalse(sampleSpot(lat = -91.0, lng = 0.0).hasValidCoordinates)
        assertFalse(sampleSpot(lat = 0.0, lng = 181.0).hasValidCoordinates)
        assertFalse(sampleSpot(lat = 0.0, lng = -181.0).hasValidCoordinates)
    }

    @Test
    fun `hasValidCoordinates accepts real world coordinates`() {
        assertTrue(sampleSpot(lat = 37.7749, lng = -122.4194).hasValidCoordinates)
        assertTrue(sampleSpot(lat = -33.8688, lng = 151.2093).hasValidCoordinates)
    }

    @Test
    fun `place header uses cityStateFromLocation stripping the country suffix`() {
        val expected = cityStateFromLocation("Fisherman's Wharf, San Francisco, CA, United States")
        assertEquals("San Francisco, CA", expected)
    }

    @Test
    fun `place header returns the raw when only a single segment is provided`() {
        assertEquals("Tokyo", cityStateFromLocation("Tokyo"))
    }

    private fun sampleSpot(lat: Double, lng: Double): Spot = Spot(
        id = "id",
        userId = "user",
        username = "user",
        userProfileImageURL = null,
        caption = "",
        latitude = lat,
        longitude = lng,
        locationName = null,
        likes = 0,
        saves = 0,
        createdAt = 0L,
        updatedAt = null,
        imageURL = null,
        thumbnailURL = null,
        mediaDisplayAspectRatio = 1.0,
        mediaCount = 0,
        vibeTag = null,
        authorIsPrivate = false,
    )
}
