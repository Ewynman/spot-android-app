package com.spot.android.data.permissions

import com.spot.android.core.design.component.PermissionType

/**
 * In-memory fake for unit testing permission flows.
 */
class FakePermissionsRepository : PermissionsRepository {
    private val states = mutableMapOf<PermissionType, PermissionState>()
    private val requested = mutableSetOf<PermissionType>()
    private val runtimeRequired = mutableMapOf<PermissionType, Boolean>()

    init {
        PermissionType.entries.forEach { type ->
            states[type] = PermissionState.NOT_DETERMINED
            runtimeRequired[type] = true
        }
    }

    fun setState(type: PermissionType, state: PermissionState) {
        states[type] = state
    }

    fun setRuntimeRequired(type: PermissionType, required: Boolean) {
        runtimeRequired[type] = required
    }

    override suspend fun getState(type: PermissionType): PermissionState {
        if (runtimeRequired[type] == false) {
            return PermissionState.NOT_REQUIRED
        }
        return states[type] ?: PermissionState.NOT_DETERMINED
    }

    override suspend fun getAllStates(): Map<PermissionType, PermissionState> {
        return PermissionType.entries.associateWith { getState(it) }
    }

    override suspend fun shouldShowPrePrompt(type: PermissionType): Boolean {
        if (runtimeRequired[type] == false) {
            return !requested.contains(type)
        }
        return getState(type) == PermissionState.NOT_DETERMINED
    }

    override suspend fun markRequested(type: PermissionType) {
        requested.add(type)
        if (states[type] == PermissionState.NOT_DETERMINED) {
            states[type] = PermissionState.DENIED
        }
    }

    override suspend fun hasAnyNeedsAttention(): Boolean {
        return states.values.any {
            it == PermissionState.DENIED || it == PermissionState.PERMANENTLY_DENIED
        }
    }

    override fun isRuntimePermissionRequired(type: PermissionType): Boolean {
        return runtimeRequired[type] ?: true
    }

    override fun getAndroidPermissions(type: PermissionType): Array<String> {
        return when (type) {
            PermissionType.LOCATION -> arrayOf("android.permission.ACCESS_FINE_LOCATION")
            PermissionType.CAMERA -> arrayOf("android.permission.CAMERA")
            PermissionType.PHOTOS -> arrayOf("android.permission.READ_MEDIA_IMAGES")
            PermissionType.NOTIFICATIONS -> arrayOf("android.permission.POST_NOTIFICATIONS")
        }
    }
}
