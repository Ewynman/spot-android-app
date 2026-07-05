package com.spot.android.data.map

import com.spot.android.core.util.Constants
import kotlin.math.cos
import kotlin.math.pow

/**
 * Visible map viewport used for `get_map_spots_v1`.
 */
data class MapViewportBounds(
    val minLat: Double,
    val minLng: Double,
    val maxLat: Double,
    val maxLng: Double,
    val centerLat: Double,
    val centerLng: Double,
    val zoom: Float,
) {
    val latSpan: Double get() = (maxLat - minLat).coerceAtLeast(0.0)
    val lngSpan: Double get() = (maxLng - minLng).coerceAtLeast(0.0)
    val maxSpan: Double get() = maxOf(latSpan, lngSpan)

    val isFarZoom: Boolean
        get() = maxSpan >= Constants.MapDesign.CITY_SPAN

    companion object {
        /**
         * Approximate visible bounds from center + zoom level.
         */
        fun fromCenterZoom(
            centerLat: Double,
            centerLng: Double,
            zoom: Float,
        ): MapViewportBounds {
            val latSpan = 360.0 / 2.0.pow(zoom.toDouble())
            val lngSpan = latSpan / cos(Math.toRadians(centerLat)).coerceAtLeast(0.1)
            return MapViewportBounds(
                minLat = centerLat - latSpan / 2,
                maxLat = centerLat + latSpan / 2,
                minLng = centerLng - lngSpan / 2,
                maxLng = centerLng + lngSpan / 2,
                centerLat = centerLat,
                centerLng = centerLng,
                zoom = zoom,
            )
        }
    }
}
