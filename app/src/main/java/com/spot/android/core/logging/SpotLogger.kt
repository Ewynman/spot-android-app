package com.spot.android.core.logging

import com.spot.android.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Structured logger with per-area categories toggled via DataStore preferences.
 *
 * Debug logging is disabled in release builds regardless of stored preferences.
 * Never log PII, tokens, or raw image bytes.
 *
 * Reference: PRD/17-non-functional-testing.md, PRD/11-settings.md
 */
@Singleton
class SpotLogger private constructor(
    private val preferencesRepository: LogPreferencesRepository,
    private val logWriter: LogWriter,
    scope: CoroutineScope,
) {
    private val preferencesSnapshot = AtomicReference(LogPreferences.DEFAULT)
    private val _preferences = MutableStateFlow(LogPreferences.DEFAULT)
    val preferences: StateFlow<LogPreferences> = _preferences.asStateFlow()

    init {
        scope.launch {
            preferencesRepository.preferencesFlow.collect { preferences ->
                preferencesSnapshot.set(preferences)
                _preferences.value = preferences
            }
        }
    }

    @Inject
    constructor(
        preferencesRepository: LogPreferencesRepository,
        logWriter: LogWriter,
    ) : this(
        preferencesRepository = preferencesRepository,
        logWriter = logWriter,
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    )

    fun isEnabled(category: LogCategory): Boolean {
        return isLoggingEnabled(
            isDebugBuild = BuildConfig.DEBUG,
            preferences = preferencesSnapshot.get(),
            category = category,
        )
    }

    fun d(category: LogCategory, tag: String, message: String) {
        log(Log.DEBUG, category, tag, message)
    }

    fun i(category: LogCategory, tag: String, message: String) {
        log(Log.INFO, category, tag, message)
    }

    fun w(category: LogCategory, tag: String, message: String, throwable: Throwable? = null) {
        log(Log.WARN, category, tag, message, throwable)
    }

    fun e(category: LogCategory, tag: String, message: String, throwable: Throwable? = null) {
        log(Log.ERROR, category, tag, message, throwable)
    }

    private fun log(
        priority: Int,
        category: LogCategory,
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ) {
        if (!isEnabled(category)) return
        logWriter.write(
            priority = priority,
            tag = formatTag(category, tag),
            message = message,
            throwable = throwable,
        )
    }

    private fun formatTag(category: LogCategory, tag: String): String {
        return "Spot/${category.name}/$tag"
    }

    companion object {
        internal fun createForTest(
            preferencesRepository: LogPreferencesRepository,
            logWriter: LogWriter,
            scope: CoroutineScope,
        ): SpotLogger = SpotLogger(preferencesRepository, logWriter, scope)

        internal fun isLoggingEnabled(
            isDebugBuild: Boolean,
            preferences: LogPreferences,
            category: LogCategory,
        ): Boolean {
            if (!isDebugBuild) return false
            if (!preferences.debugLoggingEnabled) return false
            if (preferences.logAllDebugCategories) return true
            return preferences.isCategoryEnabled(category)
        }
    }
}

private object Log {
    const val DEBUG = android.util.Log.DEBUG
    const val INFO = android.util.Log.INFO
    const val WARN = android.util.Log.WARN
    const val ERROR = android.util.Log.ERROR
}
