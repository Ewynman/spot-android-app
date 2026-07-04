package com.spot.android.data.location

/**
 * A place suggestion for the post flow location step.
 */
data class PlaceSuggestion(
    val name: String,
    val address: String?,
    val latitude: Double,
    val longitude: Double,
    val isCustomName: Boolean = false,
)

/**
 * Searches for places by query or returns the viewer's current location.
 */
interface PlaceSearchProvider {
    suspend fun search(query: String): Result<List<PlaceSuggestion>>
    suspend fun currentLocationPlace(): Result<PlaceSuggestion?>
}
