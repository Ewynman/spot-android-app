package com.spot.android.data.permissions

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.spot.android.core.design.component.PermissionType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.permissionPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "spot_permission_preferences",
)

/**
 * Persists whether each permission pre-prompt has already been shown.
 *
 * Reference: PRD/05-auth-onboarding.md
 */
@Singleton
class PermissionPreferencesStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore = context.permissionPreferencesDataStore

    val preferencesFlow: Flow<PermissionPreferences> = dataStore.data.map { prefs ->
        PermissionPreferences(
            locationPermissionRequested = prefs[PermissionPreferencesKeys.LOCATION_PERMISSION_REQUESTED] ?: false,
            notificationsRequested = prefs[PermissionPreferencesKeys.NOTIFICATIONS_REQUESTED] ?: false,
            photoPermissionRequested = prefs[PermissionPreferencesKeys.PHOTO_PERMISSION_REQUESTED] ?: false,
            cameraPermissionRequested = prefs[PermissionPreferencesKeys.CAMERA_PERMISSION_REQUESTED] ?: false,
            firstRun = prefs[PermissionPreferencesKeys.FIRST_RUN] ?: true,
            promptPermsOnNextLogin = prefs[PermissionPreferencesKeys.PROMPT_PERMS_ON_NEXT_LOGIN] ?: false,
        )
    }

    suspend fun hasRequested(type: PermissionType): Boolean {
        return preferencesFlow.map { prefs -> prefs.hasRequested(type) }.first()
    }

    suspend fun markRequested(type: PermissionType) {
        dataStore.edit { prefs ->
            prefs[type.requestedKey()] = true
        }
    }

    suspend fun setFirstRun(firstRun: Boolean) {
        dataStore.edit { prefs ->
            prefs[PermissionPreferencesKeys.FIRST_RUN] = firstRun
        }
    }

    suspend fun clearPromptPermsOnNextLogin() {
        dataStore.edit { prefs ->
            prefs[PermissionPreferencesKeys.PROMPT_PERMS_ON_NEXT_LOGIN] = false
        }
    }

    private fun PermissionType.requestedKey() = when (this) {
        PermissionType.LOCATION -> PermissionPreferencesKeys.LOCATION_PERMISSION_REQUESTED
        PermissionType.NOTIFICATIONS -> PermissionPreferencesKeys.NOTIFICATIONS_REQUESTED
        PermissionType.PHOTOS -> PermissionPreferencesKeys.PHOTO_PERMISSION_REQUESTED
        PermissionType.CAMERA -> PermissionPreferencesKeys.CAMERA_PERMISSION_REQUESTED
    }
}

data class PermissionPreferences(
    val locationPermissionRequested: Boolean = false,
    val notificationsRequested: Boolean = false,
    val photoPermissionRequested: Boolean = false,
    val cameraPermissionRequested: Boolean = false,
    val firstRun: Boolean = true,
    val promptPermsOnNextLogin: Boolean = false,
) {
    fun hasRequested(type: PermissionType): Boolean = when (type) {
        PermissionType.LOCATION -> locationPermissionRequested
        PermissionType.NOTIFICATIONS -> notificationsRequested
        PermissionType.PHOTOS -> photoPermissionRequested
        PermissionType.CAMERA -> cameraPermissionRequested
    }
}
