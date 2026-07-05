package com.spot.android.data.map

import com.spot.android.core.util.Constants
import com.spot.android.data.model.Spot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MapPinLayoutTest {

    @Test
    fun `mergeSpots keeps nearest spots up to cap`() {
        val existing = mapOf(
            "1" to sampleSpot("1", 40.0, -74.0, distance = 100.0),
            "2" to sampleSpot("2", 40.01, -74.01, distance = 200.0),
        )
        val incoming = listOf(
            sampleSpot("3", 40.02, -74.02, distance = 50.0),
            sampleSpot("4", 41.0, -75.0, distance = 10_000.0),
        )

        val merged = MapPinLayout.mergeSpots(
            existing = existing,
            incoming = incoming,
            centerLat = 40.0,
            centerLng = -74.0,
            cap = 3,
        )

        assertEquals(3, merged.size)
        assertTrue(merged.containsKey("3"))
    }

    @Test
    fun `layoutPins offsets co-located spots`() {
        val spots = listOf(
            sampleSpot("1", 40.0, -74.0),
            sampleSpot("2", 40.0, -74.0),
        )

        val pins = MapPinLayout.layoutPins(spots)
        assertEquals(2, pins.size)
        assertTrue(
            pins[0].displayPosition.latitude != pins[1].displayPosition.latitude ||
                pins[0].displayPosition.longitude != pins[1].displayPosition.longitude,
        )
    }

    @Test
    fun `capForZoom limits pins when far zoom`() {
        val spots = (1..100).map { index ->
            sampleSpot(index.toString(), 40.0 + index * 0.001, -74.0)
        }

        val capped = MapPinLayout.capForZoom(spots, isFarZoom = true)
        assertEquals(Constants.MapDesign.FAR_ZOOM_PIN_CAP, capped.size)
    }

    @Test
    fun `viewport isFarZoom when span exceeds city threshold`() {
        val viewport = MapViewportBounds.fromCenterZoom(40.0, -74.0, zoom = 8f)
        assertTrue(viewport.isFarZoom)
    }

    private fun sampleSpot(
        id: String,
        lat: Double,
        lng: Double,
        distance: Double? = null,
    ): Spot {
        return Spot(
            id = id,
            userId = "user-$id",
            username = "user",
            userProfileImageURL = null,
            caption = "caption",
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
            mediaCount = 1,
            vibeTag = "Chill Spot",
            authorIsPrivate = false,
            distanceMeters = distance,
        )
    }
}
