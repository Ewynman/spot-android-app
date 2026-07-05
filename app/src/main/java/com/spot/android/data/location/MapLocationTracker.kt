package com.spot.android.data.location

import kotlinx.coroutines.flow.Flow

/**
 * Streams user location updates for the map tab.
 */
interface MapLocationTracker {
    val locationUpdates: Flow<ViewerLocation>
    fun startTracking()
    fun stopTracking()
}
