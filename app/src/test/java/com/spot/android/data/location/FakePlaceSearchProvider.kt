package com.spot.android.data.location

class FakePlaceSearchProvider : PlaceSearchProvider {
    var suggestions: List<PlaceSuggestion> = emptyList()
    var currentPlace: PlaceSuggestion? = null

    override suspend fun search(query: String): Result<List<PlaceSuggestion>> =
        Result.success(suggestions)

    override suspend fun currentLocationPlace(): Result<PlaceSuggestion?> =
        Result.success(currentPlace)
}
