package com.spot.android.core.logging

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LogPreferencesTest {

    @Test
    fun `default preferences keep all categories disabled`() {
        val preferences = LogPreferences.DEFAULT

        LogCategory.entries.forEach { category ->
            assertFalse(preferences.isCategoryEnabled(category))
        }
    }

    @Test
    fun `maps categories to preference flags`() {
        val preferences = LogPreferences(
            logFeedComponent = true,
            logAuth = true,
            logNetworkComponent = true,
            logPostFlow = true,
            logMap = true,
            logDeepLink = true,
            logPrivacy = true,
            logSpotCard = true,
        )

        LogCategory.entries.forEach { category ->
            assertTrue(preferences.isCategoryEnabled(category))
        }
    }
}
