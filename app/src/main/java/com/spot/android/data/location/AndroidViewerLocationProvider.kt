package com.spot.android.data.location

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.spot.android.core.design.component.PermissionType
import com.spot.android.data.permissions.PermissionState
import com.spot.android.data.permissions.PermissionsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.tasks.await

/**
 * Reads last known location when location permission is authorized.
 */
@Singleton
class AndroidViewerLocationProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissionsRepository: PermissionsRepository,
) : ViewerLocationProvider {

    private val fusedClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    @SuppressLint("MissingPermission")
    override suspend fun getLocation(): ViewerLocation? {
        if (permissionsRepository.getState(PermissionType.LOCATION) != PermissionState.AUTHORIZED) {
            return null
        }

        return try {
            val location = fusedClient.lastLocation.await()
            location?.let {
                ViewerLocation(latitude = it.latitude, longitude = it.longitude)
            }
        } catch (_: Exception) {
            null
        }
    }
}
