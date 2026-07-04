package com.spot.android.data.billing

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.proOnboardingDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "pro_onboarding_preferences"
)

/**
 * Persistence for Pro post-purchase onboarding tour state.
 *
 * Tracks whether a user has seen the tour (keyed by user ID).
 *
 * Reference: PRD/12-pro-subscription.md
 */
@Singleton
class ProOnboardingPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.proOnboardingDataStore

    fun hasSeenOnboarding(userId: String): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[booleanPreferencesKey("has_seen_post_purchase_pro_onboarding_$userId")] ?: false
        }
    }

    suspend fun setOnboardingSeen(userId: String) {
        dataStore.edit { preferences ->
            preferences[booleanPreferencesKey("has_seen_post_purchase_pro_onboarding_$userId")] = true
        }
    }
}
