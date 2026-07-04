package com.spot.android.core.logging

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.logPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "spot_log_preferences",
)

interface LogPreferencesRepository {
    val preferencesFlow: Flow<LogPreferences>

    suspend fun setDebugLoggingEnabled(enabled: Boolean)
    suspend fun setLogAllDebugCategories(enabled: Boolean)
    suspend fun setCategoryEnabled(category: LogCategory, enabled: Boolean)
}

@Singleton
class DataStoreLogPreferencesRepository @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext context: Context,
) : LogPreferencesRepository {

    private val dataStore = context.logPreferencesDataStore

    override val preferencesFlow: Flow<LogPreferences> = dataStore.data.map { preferences ->
        LogPreferences(
            debugLoggingEnabled = preferences[LogPreferencesKeys.DEBUG_LOGGING_ENABLED] ?: false,
            logAllDebugCategories = preferences[LogPreferencesKeys.LOG_ALL_DEBUG_CATEGORIES] ?: false,
            logSpotCard = preferences[LogPreferencesKeys.LOG_SPOT_CARD] ?: false,
            logPrivacy = preferences[LogPreferencesKeys.LOG_PRIVACY] ?: false,
            logFeedComponent = preferences[LogPreferencesKeys.LOG_FEED_COMPONENT] ?: false,
            logPostFlow = preferences[LogPreferencesKeys.LOG_POST_FLOW] ?: false,
            logAuth = preferences[LogPreferencesKeys.LOG_AUTH] ?: false,
            logNetworkComponent = preferences[LogPreferencesKeys.LOG_NETWORK_COMPONENT] ?: false,
            logDeepLink = preferences[LogPreferencesKeys.LOG_DEEP_LINK] ?: false,
            logMap = preferences[LogPreferencesKeys.LOG_MAP] ?: false,
            logBilling = preferences[LogPreferencesKeys.LOG_BILLING] ?: false,
        )
    }

    override suspend fun setDebugLoggingEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[LogPreferencesKeys.DEBUG_LOGGING_ENABLED] = enabled
        }
    }

    override suspend fun setLogAllDebugCategories(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[LogPreferencesKeys.LOG_ALL_DEBUG_CATEGORIES] = enabled
        }
    }

    override suspend fun setCategoryEnabled(category: LogCategory, enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[category.preferenceKey()] = enabled
        }
    }

    private fun LogCategory.preferenceKey() = when (this) {
        LogCategory.Feed -> LogPreferencesKeys.LOG_FEED_COMPONENT
        LogCategory.Auth -> LogPreferencesKeys.LOG_AUTH
        LogCategory.Network -> LogPreferencesKeys.LOG_NETWORK_COMPONENT
        LogCategory.Post -> LogPreferencesKeys.LOG_POST_FLOW
        LogCategory.Map -> LogPreferencesKeys.LOG_MAP
        LogCategory.DeepLink -> LogPreferencesKeys.LOG_DEEP_LINK
        LogCategory.Privacy -> LogPreferencesKeys.LOG_PRIVACY
        LogCategory.SpotCard -> LogPreferencesKeys.LOG_SPOT_CARD
        LogCategory.Billing -> LogPreferencesKeys.LOG_BILLING
    }
}
