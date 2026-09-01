package com.spot.android.data.model

/**
 * Whether the Spot has usable map coordinates.
 *
 * We treat `(0.0, 0.0)` and NaN values as "unset" (matches iOS
 * `Spot.hasValidCoordinates`).
 */
val Spot.hasValidCoordinates: Boolean
    get() {
        val lat = latitude
        val lng = longitude
        if (lat.isNaN() || lng.isNaN()) return false
        if (lat == 0.0 && lng == 0.0) return false
        if (lat !in -90.0..90.0) return false
        if (lng !in -180.0..180.0) return false
        return true
    }
