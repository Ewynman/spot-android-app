package com.spot.android.data.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.spot.android.core.design.component.PermissionType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Android implementation of [PermissionsRepository] using PackageManager checks.
 *
 * Reference: PRD/05-auth-onboarding.md
 */
@Singleton
class AndroidPermissionsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesStore: PermissionPreferencesStore,
) : PermissionsRepository {

    override suspend fun getState(type: PermissionType): PermissionState {
        if (!isRuntimePermissionRequired(type)) {
            return PermissionState.NOT_REQUIRED
        }

        val permissions = getAndroidPermissions(type)
        if (permissions.isEmpty()) {
            return PermissionState.NOT_REQUIRED
        }

        val allGranted = permissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
        if (allGranted) {
            return PermissionState.AUTHORIZED
        }

        val alreadyRequested = preferencesStore.hasRequested(type)
        return if (alreadyRequested) {
            PermissionState.DENIED
        } else {
            PermissionState.NOT_DETERMINED
        }
    }

    override suspend fun getAllStates(): Map<PermissionType, PermissionState> {
        return PermissionType.entries.associateWith { getState(it) }
    }

    override suspend fun shouldShowPrePrompt(type: PermissionType): Boolean {
        if (!isRuntimePermissionRequired(type)) {
            return !preferencesStore.hasRequested(type)
        }
        return getState(type) == PermissionState.NOT_DETERMINED
    }

    override suspend fun markRequested(type: PermissionType) {
        preferencesStore.markRequested(type)
    }

    override suspend fun hasAnyNeedsAttention(): Boolean {
        return PermissionType.entries.any { type ->
            val state = getState(type)
            state == PermissionState.DENIED || state == PermissionState.PERMANENTLY_DENIED
        }
    }

    override fun isRuntimePermissionRequired(type: PermissionType): Boolean {
        return when (type) {
            PermissionType.LOCATION -> true
            PermissionType.CAMERA -> true
            PermissionType.PHOTOS -> Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
            PermissionType.NOTIFICATIONS -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        }
    }

    override fun getAndroidPermissions(type: PermissionType): Array<String> {
        return when (type) {
            PermissionType.LOCATION -> arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
            PermissionType.CAMERA -> arrayOf(Manifest.permission.CAMERA)
            PermissionType.PHOTOS -> when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> emptyArray()
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                )
                else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            PermissionType.NOTIFICATIONS -> when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS)
                else -> emptyArray()
            }
        }
    }
}
