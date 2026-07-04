package com.spot.android.core.logging

import androidx.datastore.preferences.core.booleanPreferencesKey

/**
 * DataStore keys for debug logging toggles.
 *
 * Mirrors the iOS debug log keys documented in PRD/11-settings.md.
 */
object LogPreferencesKeys {
    val DEBUG_LOGGING_ENABLED = booleanPreferencesKey("debugLoggingEnabled")
    val LOG_ALL_DEBUG_CATEGORIES = booleanPreferencesKey("logAllDebugCategories")
    val LOG_SPOT_CARD = booleanPreferencesKey("logSpotCard")
    val LOG_PRIVACY = booleanPreferencesKey("logPrivacy")
    val LOG_FEED_COMPONENT = booleanPreferencesKey("logFeedComponent")
    val LOG_POST_FLOW = booleanPreferencesKey("logPostFlow")
    val LOG_AUTH = booleanPreferencesKey("logAuth")
    val LOG_NETWORK_COMPONENT = booleanPreferencesKey("logNetworkComponent")
    val LOG_DEEP_LINK = booleanPreferencesKey("logDeepLink")
    val LOG_MAP = booleanPreferencesKey("logMap")
    val LOG_BILLING = booleanPreferencesKey("logBilling")
}
