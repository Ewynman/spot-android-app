package com.spot.android.data.map

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.spot.android.core.util.Constants
import com.spot.android.data.model.Spot

/**
 * Pin with display position after overlap offset is applied.
 */
data class MapPin(
    val spot: Spot,
    val displayPosition: LatLng,
    val bucketIndex: Int = 0,
)

/**
 * Overlap handling and pin capping utilities.
 *
 * Reference: PRD/07-map.md
 */
object MapPinLayout {

    /**
     * Merge newly fetched spots with existing pins, trim to cap nearest-first.
     */
    fun mergeSpots(
        existing: Map<String, Spot>,
        incoming: List<Spot>,
        centerLat: Double,
        centerLng: Double,
        cap: Int,
    ): Map<String, Spot> {
        val merged = existing.toMutableMap()
        incoming.forEach { spot -> merged[spot.id] = spot }

        return merged.values
            .sortedBy { spot ->
                spot.distanceMeters ?: haversineMeters(
                    centerLat,
                    centerLng,
                    spot.latitude,
                    spot.longitude,
                )
            }
            .take(cap)
            .associateBy { it.id }
    }

    /**
     * Apply radial offsets to co-located pins within ~5m buckets.
     */
    fun layoutPins(spots: List<Spot>): List<MapPin> {
        val bucketSize = Constants.MapDesign.OVERLAP_BUCKET_SIZE
        val offsetMeters = Constants.MapDesign.OVERLAP_OFFSET_METERS

        val buckets = spots.groupBy { spot ->
            val latBucket = (spot.latitude / bucketSize).toLong()
            val lngBucket = (spot.longitude / bucketSize).toLong()
            latBucket to lngBucket
        }

        return buckets.values.flatMap { bucketSpots ->
            if (bucketSpots.size == 1) {
                val spot = bucketSpots.first()
                listOf(
                    MapPin(
                        spot = spot,
                        displayPosition = LatLng(spot.latitude, spot.longitude),
                    ),
                )
            } else {
                bucketSpots.mapIndexed { index, spot ->
                    val bearing = (360.0 / bucketSpots.size) * index
                    val base = LatLng(spot.latitude, spot.longitude)
                    val offset = SphericalUtil.computeOffset(base, offsetMeters, bearing)
                    MapPin(
                        spot = spot,
                        displayPosition = offset,
                        bucketIndex = index,
                    )
                }
            }
        }
    }

    /**
     * Cap visible pins when zoomed far out.
     */
    fun capForZoom(
        spots: List<Spot>,
        isFarZoom: Boolean,
    ): List<Spot> {
        if (!isFarZoom) return spots
        return spots.take(Constants.MapDesign.FAR_ZOOM_PIN_CAP)
    }

    private fun haversineMeters(
        lat1: Double,
        lng1: Double,
        lat2: Double,
        lng2: Double,
    ): Double {
        return SphericalUtil.computeDistanceBetween(LatLng(lat1, lng1), LatLng(lat2, lng2))
    }
}
