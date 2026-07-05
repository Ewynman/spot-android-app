package com.spot.android.data.map

import com.spot.android.data.dto.MapSpotRowDto

class FakeMapRepository : MapRepository {
    var spots: List<MapSpotRowDto> = emptyList()
    var shouldFail = false
    var lastRequest: GetMapSpotsRpcParams? = null

    override suspend fun getMapSpots(
        minLat: Double,
        minLng: Double,
        maxLat: Double,
        maxLng: Double,
        centerLat: Double,
        centerLng: Double,
        limit: Int,
    ): Result<List<MapSpotRowDto>> {
        lastRequest = GetMapSpotsRpcParams(
            p_min_lat = minLat,
            p_min_lng = minLng,
            p_max_lat = maxLat,
            p_max_lng = maxLng,
            p_center_lat = centerLat,
            p_center_lng = centerLng,
            p_limit = limit,
        )
        return if (shouldFail) {
            Result.failure(IllegalStateException("Map fetch failed"))
        } else {
            Result.success(spots)
        }
    }
}
