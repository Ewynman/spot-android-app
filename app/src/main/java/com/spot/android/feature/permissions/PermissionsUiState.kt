package com.spot.android.feature.permissions

import com.spot.android.core.design.component.PermissionType
import com.spot.android.data.permissions.PermissionState

/**
 * UI state for the permissions framework.
 */
data class PermissionsUiState(
    val permissionStates: Map<PermissionType, PermissionState> = emptyMap(),
    val activePrePrompt: PermissionType? = null,
    val pendingLaunchPermissions: Array<String>? = null,
    val hasAnyNeedsAttention: Boolean = false,
)

/**
 * One-shot effect emitted when a permission request flow completes.
 */
data class PermissionRequestResult(
    val type: PermissionType,
    val state: PermissionState,
)
