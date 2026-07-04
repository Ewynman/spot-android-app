package com.spot.android.feature.permissions

import com.spot.android.core.design.component.PermissionType

/**
 * Copy for permission pre-prompt screens.
 *
 * Reference: PRD/05-auth-onboarding.md
 */
object PermissionContent {
    fun title(type: PermissionType): String = when (type) {
        PermissionType.LOCATION -> "Enable Location"
        PermissionType.CAMERA -> "Camera Access"
        PermissionType.PHOTOS -> "Photo Access"
        PermissionType.NOTIFICATIONS -> "Enable Notifications"
    }

    fun message(type: PermissionType): String = when (type) {
        PermissionType.LOCATION ->
            "We need your location to show you spots nearby and help you discover new places."
        PermissionType.CAMERA ->
            "Take photos to share your favorite spots with the community."
        PermissionType.PHOTOS ->
            "Choose photos from your gallery to share your favorite spots."
        PermissionType.NOTIFICATIONS ->
            "Get notified when someone follows you or likes your spots."
    }
}
