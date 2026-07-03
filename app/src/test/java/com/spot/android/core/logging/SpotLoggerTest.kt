package com.spot.android.core.logging

import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SpotLoggerTest {

    private lateinit var preferencesRepository: FakeLogPreferencesRepository
    private lateinit var logWriter: FakeLogWriter

    @Before
    fun setup() {
        preferencesRepository = FakeLogPreferencesRepository()
        logWriter = FakeLogWriter()
    }

    private fun TestScope.createLogger(): SpotLogger {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        return SpotLogger.createForTest(
            preferencesRepository = preferencesRepository,
            logWriter = logWriter,
            scope = scope,
        )
    }

    @Test
    fun `isLoggingEnabled returns false when debug logging disabled`() {
        val prefs = LogPreferences(debugLoggingEnabled = false, logAuth = true)

        assertFalse(
            SpotLogger.isLoggingEnabled(
                isDebugBuild = true,
                preferences = prefs,
                category = LogCategory.Auth,
            ),
        )
    }

    @Test
    fun `isLoggingEnabled returns false in release builds`() {
        val prefs = LogPreferences(
            debugLoggingEnabled = true,
            logAllDebugCategories = true,
        )

        assertFalse(
            SpotLogger.isLoggingEnabled(
                isDebugBuild = false,
                preferences = prefs,
                category = LogCategory.Auth,
            ),
        )
    }

    @Test
    fun `isLoggingEnabled returns true when log all is enabled`() {
        val prefs = LogPreferences(
            debugLoggingEnabled = true,
            logAllDebugCategories = true,
        )

        assertTrue(
            SpotLogger.isLoggingEnabled(
                isDebugBuild = true,
                preferences = prefs,
                category = LogCategory.Map,
            ),
        )
    }

    @Test
    fun `isLoggingEnabled respects per-category toggles`() {
        val prefs = LogPreferences(
            debugLoggingEnabled = true,
            logFeedComponent = true,
            logAuth = false,
        )

        assertTrue(
            SpotLogger.isLoggingEnabled(
                isDebugBuild = true,
                preferences = prefs,
                category = LogCategory.Feed,
            ),
        )
        assertFalse(
            SpotLogger.isLoggingEnabled(
                isDebugBuild = true,
                preferences = prefs,
                category = LogCategory.Auth,
            ),
        )
    }

    @Test
    fun `does not write when category disabled`() = runTest {
        val spotLogger = createLogger()
        preferencesRepository.setPreferences(
            LogPreferences(
                debugLoggingEnabled = true,
                logAuth = false,
            ),
        )

        spotLogger.d(LogCategory.Auth, "AuthRepo", "should not log")

        assertTrue(logWriter.entries.isEmpty())
    }

    @Test
    fun `writes when category enabled at runtime`() = runTest {
        val spotLogger = createLogger()
        preferencesRepository.setPreferences(
            LogPreferences(
                debugLoggingEnabled = true,
                logAuth = true,
            ),
        )

        spotLogger.d(LogCategory.Auth, "AuthRepo", "auth event")

        assertEquals(1, logWriter.entries.size)
        assertEquals("Spot/Auth/AuthRepo", logWriter.entries.first().tag)
        assertEquals("auth event", logWriter.entries.first().message)
    }

    @Test
    fun `log all enables every category at runtime`() = runTest {
        val spotLogger = createLogger()
        preferencesRepository.setPreferences(
            LogPreferences(
                debugLoggingEnabled = true,
                logAllDebugCategories = true,
            ),
        )

        spotLogger.d(LogCategory.Map, "MapRepo", "map event")
        spotLogger.d(LogCategory.SpotCard, "SpotCard", "card event")

        assertEquals(2, logWriter.entries.size)
    }

    @Test
    fun `category toggle updates at runtime`() = runTest {
        val spotLogger = createLogger()
        preferencesRepository.setPreferences(
            LogPreferences(
                debugLoggingEnabled = true,
                logFeedComponent = false,
            ),
        )

        spotLogger.d(LogCategory.Feed, "FeedRepo", "first")
        assertTrue(logWriter.entries.isEmpty())

        preferencesRepository.setCategoryEnabled(LogCategory.Feed, true)

        spotLogger.d(LogCategory.Feed, "FeedRepo", "second")
        assertEquals(1, logWriter.entries.size)
        assertEquals("second", logWriter.entries.first().message)
    }
}
