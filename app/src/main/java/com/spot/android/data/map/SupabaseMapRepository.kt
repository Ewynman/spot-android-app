package com.spot.android.data.map

import com.spot.android.core.logging.LogCategory
import com.spot.android.core.logging.SpotLogger
import com.spot.android.core.supabase.SupabaseClientProvider
import com.spot.android.core.util.Constants
import com.spot.android.data.dto.MapSpotRowDto
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Supabase-backed map repository.
 */
@Singleton
class SupabaseMapRepository @Inject constructor(
    private val supabaseProvider: SupabaseClientProvider,
    private val logger: SpotLogger,
) : MapRepository {

    private val postgrest get() = supabaseProvider.client.postgrest

    override suspend fun getMapSpots(
        minLat: Double,
        minLng: Double,
        maxLat: Double,
        maxLng: Double,
        centerLat: Double,
        centerLng: Double,
        limit: Int,
    ): Result<List<MapSpotRowDto>> {
        return try {
            val rows = postgrest.rpc(
                function = "get_map_spots_v1",
                parameters = GetMapSpotsRpcParams(
                    p_min_lat = minLat,
                    p_min_lng = minLng,
                    p_max_lat = maxLat,
                    p_max_lng = maxLng,
                    p_center_lat = centerLat,
                    p_center_lng = centerLng,
                    p_limit = limit.coerceAtMost(Constants.MapDesign.VISIBLE_SPOTS_CAP),
                ),
            ).decodeList<MapSpotRowDto>()
            Result.success(rows)
        } catch (e: Exception) {
            logger.e(LogCategory.Map, TAG, "Failed to load map spots", e)
            Result.failure(e)
        }
    }

    private companion object {
        const val TAG = "SupabaseMapRepository"
    }
}
