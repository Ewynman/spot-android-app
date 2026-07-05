package com.spot.android.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.spot.android.core.design.component.PermissionType
import com.spot.android.data.permissions.PermissionState
import com.spot.android.data.permissions.PermissionsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Fused location updates for the map user-location marker.
 */
@Singleton
class AndroidMapLocationTracker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissionsRepository: PermissionsRepository,
) : MapLocationTracker {

    private val fusedClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    @SuppressLint("MissingPermission")
    override val locationUpdates: Flow<ViewerLocation> = callbackFlow {
        if (permissionsRepository.getState(PermissionType.LOCATION) != PermissionState.AUTHORIZED) {
            close()
            return@callbackFlow
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 5_000L)
            .setMinUpdateIntervalMillis(2_000L)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                trySend(ViewerLocation(location.latitude, location.longitude))
            }
        }

        fusedClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
        awaitClose { fusedClient.removeLocationUpdates(callback) }
    }

    override fun startTracking() {
        // Flow collection drives tracking lifecycle.
    }

    override fun stopTracking() {
        // Flow collection cancellation removes updates.
    }
}
