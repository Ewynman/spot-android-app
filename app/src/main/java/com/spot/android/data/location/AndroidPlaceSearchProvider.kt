package com.spot.android.data.location

import android.content.Context
import android.location.Geocoder
import android.os.Build
import com.spot.android.core.design.component.PermissionType
import com.spot.android.data.permissions.PermissionState
import com.spot.android.data.permissions.PermissionsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Android Geocoder-backed place search with current-location fallback.
 *
 * Reference: PRD/08-post-flow.md
 */
@Singleton
class AndroidPlaceSearchProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val viewerLocationProvider: ViewerLocationProvider,
    private val permissionsRepository: PermissionsRepository,
) : PlaceSearchProvider {

    override suspend fun search(query: String): Result<List<PlaceSuggestion>> =
        withContext(Dispatchers.IO) {
            runCatching {
                if (!Geocoder.isPresent()) return@runCatching emptyList()
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCoroutine { continuation ->
                        geocoder.getFromLocationName(query, 8) { results ->
                            continuation.resume(results)
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocationName(query, 8).orEmpty()
                }

                addresses.mapNotNull { address ->
                    val lat = address.latitude
                    val lng = address.longitude
                    if (lat == 0.0 && lng == 0.0) return@mapNotNull null
                    PlaceSuggestion(
                        name = address.featureName ?: address.locality ?: query,
                        address = address.getAddressLine(0),
                        latitude = lat,
                        longitude = lng,
                    )
                }
            }
        }

    override suspend fun currentLocationPlace(): Result<PlaceSuggestion?> {
        if (permissionsRepository.getState(PermissionType.LOCATION) != PermissionState.AUTHORIZED) {
            return Result.success(null)
        }
        val location = viewerLocationProvider.getLocation() ?: return Result.success(null)
        return Result.success(
            PlaceSuggestion(
                name = "Current Location",
                address = null,
                latitude = location.latitude,
                longitude = location.longitude,
            ),
        )
    }
}
