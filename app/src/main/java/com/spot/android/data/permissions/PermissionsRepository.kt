package com.spot.android.data.permissions

import com.spot.android.core.design.component.PermissionType

/**
 * Repository for checking and tracking runtime permission state.
 *
 * Reference: PRD/05-auth-onboarding.md, PRD/11-settings.md
 */
interface PermissionsRepository {
    suspend fun getState(type: PermissionType): PermissionState

    suspend fun getAllStates(): Map<PermissionType, PermissionState>

    suspend fun shouldShowPrePrompt(type: PermissionType): Boolean

    suspend fun markRequested(type: PermissionType)

    suspend fun hasAnyNeedsAttention(): Boolean

    fun isRuntimePermissionRequired(type: PermissionType): Boolean

    fun getAndroidPermissions(type: PermissionType): Array<String>
}
