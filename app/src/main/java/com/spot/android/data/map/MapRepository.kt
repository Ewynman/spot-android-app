package com.spot.android.data.map

import com.spot.android.data.dto.MapSpotRowDto

/**
 * Repository for map viewport RPCs.
 *
 * Reference: PRD/07-map.md, PRD/04-backend-api.md
 */
interface MapRepository {
    suspend fun getMapSpots(
        minLat: Double,
        minLng: Double,
        maxLat: Double,
        maxLng: Double,
        centerLat: Double,
        centerLng: Double,
        limit: Int = com.spot.android.core.util.Constants.MapDesign.VISIBLE_SPOTS_CAP,
    ): Result<List<MapSpotRowDto>>
}
