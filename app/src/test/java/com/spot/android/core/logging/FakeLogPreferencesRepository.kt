package com.spot.android.core.logging

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeLogPreferencesRepository(
    initial: LogPreferences = LogPreferences.DEFAULT,
) : LogPreferencesRepository {
    private val mutablePreferences = MutableStateFlow(initial)
    override val preferencesFlow: Flow<LogPreferences> = mutablePreferences.asStateFlow()

    fun setPreferences(preferences: LogPreferences) {
        mutablePreferences.value = preferences
    }

    override suspend fun setDebugLoggingEnabled(enabled: Boolean) {
        mutablePreferences.value = mutablePreferences.value.copy(debugLoggingEnabled = enabled)
    }

    override suspend fun setLogAllDebugCategories(enabled: Boolean) {
        mutablePreferences.value = mutablePreferences.value.copy(logAllDebugCategories = enabled)
    }

    override suspend fun setCategoryEnabled(category: LogCategory, enabled: Boolean) {
        mutablePreferences.value = when (category) {
            LogCategory.Feed -> mutablePreferences.value.copy(logFeedComponent = enabled)
            LogCategory.Auth -> mutablePreferences.value.copy(logAuth = enabled)
            LogCategory.Network -> mutablePreferences.value.copy(logNetworkComponent = enabled)
            LogCategory.Post -> mutablePreferences.value.copy(logPostFlow = enabled)
            LogCategory.Map -> mutablePreferences.value.copy(logMap = enabled)
            LogCategory.DeepLink -> mutablePreferences.value.copy(logDeepLink = enabled)
            LogCategory.Privacy -> mutablePreferences.value.copy(logPrivacy = enabled)
            LogCategory.SpotCard -> mutablePreferences.value.copy(logSpotCard = enabled)
            LogCategory.Billing -> mutablePreferences.value.copy(logBilling = enabled)
        }
    }
}
