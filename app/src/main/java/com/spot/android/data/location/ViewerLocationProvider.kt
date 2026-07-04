package com.spot.android.data.location

/**
 * Provides the viewer's location for feed ranking when permission is granted.
 */
interface ViewerLocationProvider {
    suspend fun getLocation(): ViewerLocation?
}

data class ViewerLocation(
    val latitude: Double,
    val longitude: Double,
)
