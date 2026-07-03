package com.spot.android.core.logging

/**
 * Snapshot of debug logging preferences.
 *
 * All categories default to disabled so release and fresh debug installs stay quiet.
 */
data class LogPreferences(
    val debugLoggingEnabled: Boolean = false,
    val logAllDebugCategories: Boolean = false,
    val logSpotCard: Boolean = false,
    val logPrivacy: Boolean = false,
    val logFeedComponent: Boolean = false,
    val logPostFlow: Boolean = false,
    val logAuth: Boolean = false,
    val logNetworkComponent: Boolean = false,
    val logDeepLink: Boolean = false,
    val logMap: Boolean = false,
) {
    fun isCategoryEnabled(category: LogCategory): Boolean {
        return when (category) {
            LogCategory.Feed -> logFeedComponent
            LogCategory.Auth -> logAuth
            LogCategory.Network -> logNetworkComponent
            LogCategory.Post -> logPostFlow
            LogCategory.Map -> logMap
            LogCategory.DeepLink -> logDeepLink
            LogCategory.Privacy -> logPrivacy
            LogCategory.SpotCard -> logSpotCard
        }
    }

    companion object {
        val DEFAULT = LogPreferences()
    }
}
