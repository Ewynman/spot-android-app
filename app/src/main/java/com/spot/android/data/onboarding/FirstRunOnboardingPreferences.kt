package com.spot.android.data.onboarding

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.firstRunOnboardingDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "spot_first_run_onboarding",
)

/**
 * Persistence for the first-run coach tour.
 * Keys mirror iOS SpotFirstRunOnboardingManager.
 */
@Singleton
class FirstRunOnboardingPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore = context.firstRunOnboardingDataStore

    private val completedKey = booleanPreferencesKey("spotFirstRunOnboarding.completed.v1")
    private val completedAtKey = longPreferencesKey("spotFirstRunOnboarding.completedAt.v1")
    private val skippedKey = booleanPreferencesKey("spotFirstRunOnboarding.skipped.v1")
    private val lastStepKey = intPreferencesKey("spotFirstRunOnboarding.lastStep.v1")
    private val homeTourAcceptedKey = booleanPreferencesKey("homeTourAccepted")

    val snapshot: Flow<FirstRunOnboardingSnapshot> = dataStore.data.map { prefs ->
        FirstRunOnboardingSnapshot(
            completed = prefs[completedKey] ?: false,
            completedAt = prefs[completedAtKey],
            skipped = prefs[skippedKey] ?: false,
            lastStep = prefs[lastStepKey] ?: 0,
            homeTourAccepted = prefs[homeTourAcceptedKey] ?: false,
        )
    }

    suspend fun getSnapshot(): FirstRunOnboardingSnapshot = snapshot.first()

    suspend fun setLastStep(step: Int) {
        dataStore.edit { it[lastStepKey] = step }
    }

    suspend fun markCompleted(skipped: Boolean) {
        dataStore.edit { prefs ->
            prefs[completedKey] = true
            prefs[completedAtKey] = System.currentTimeMillis()
            if (skipped) prefs[skippedKey] = true
            prefs[homeTourAcceptedKey] = true
        }
    }

    suspend fun markHomeTourAccepted() {
        dataStore.edit { it[homeTourAcceptedKey] = true }
    }
}

data class FirstRunOnboardingSnapshot(
    val completed: Boolean = false,
    val completedAt: Long? = null,
    val skipped: Boolean = false,
    val lastStep: Int = 0,
    val homeTourAccepted: Boolean = false,
) {
    val hasCompletedOrSkipped: Boolean
        get() = completed || skipped || homeTourAccepted
}
