package com.spot.android.data.permissions

import androidx.datastore.preferences.core.booleanPreferencesKey

/**
 * DataStore keys for permission prompt state.
 *
 * Mirrors iOS keys documented in PRD/05-auth-onboarding.md.
 */
object PermissionPreferencesKeys {
    val LOCATION_PERMISSION_REQUESTED = booleanPreferencesKey("locationPermissionRequested")
    val NOTIFICATIONS_REQUESTED = booleanPreferencesKey("notificationsRequested")
    val PHOTO_PERMISSION_REQUESTED = booleanPreferencesKey("photoPermissionRequested")
    val CAMERA_PERMISSION_REQUESTED = booleanPreferencesKey("cameraPermissionRequested")
    val FIRST_RUN = booleanPreferencesKey("firstRun")
    val PROMPT_PERMS_ON_NEXT_LOGIN = booleanPreferencesKey("promptPermsOnNextLogin")
}
