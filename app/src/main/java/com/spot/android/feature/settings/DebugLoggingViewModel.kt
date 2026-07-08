package com.spot.android.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spot.android.core.logging.LogCategory
import com.spot.android.core.logging.LogPreferences
import com.spot.android.core.logging.LogPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for Debug Logging settings screen.
 *
 * Reference: PRD/11-settings.md
 */
@HiltViewModel
class DebugLoggingViewModel @Inject constructor(
    private val logPreferencesRepository: LogPreferencesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DebugLoggingUiState())
    val uiState: StateFlow<DebugLoggingUiState> = _uiState.asStateFlow()

    init {
        loadPreferences()
    }

    fun onDebugLoggingToggled(enabled: Boolean) {
        viewModelScope.launch {
            logPreferencesRepository.setDebugLoggingEnabled(enabled)
            _uiState.update { it.copy(debugLoggingEnabled = enabled) }
        }
    }

    fun onLogAllCategoriesToggled(enabled: Boolean) {
        viewModelScope.launch {
            logPreferencesRepository.setLogAllDebugCategories(enabled)
            _uiState.update { it.copy(logAllCategories = enabled) }
        }
    }

    fun onCategoryToggled(category: LogCategory, enabled: Boolean) {
        viewModelScope.launch {
            when (category) {
                LogCategory.SPOT_CARD -> logPreferencesRepository.setLogSpotCard(enabled)
                LogCategory.PRIVACY -> logPreferencesRepository.setLogPrivacy(enabled)
                LogCategory.FEED_COMPONENT -> logPreferencesRepository.setLogFeedComponent(enabled)
                LogCategory.POST_FLOW -> logPreferencesRepository.setLogPostFlow(enabled)
                LogCategory.AUTH -> logPreferencesRepository.setLogAuth(enabled)
                LogCategory.NETWORK_COMPONENT -> logPreferencesRepository.setLogNetworkComponent(enabled)
                LogCategory.DEEP_LINK -> logPreferencesRepository.setLogDeepLink(enabled)
            }
            loadPreferences()
        }
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            logPreferencesRepository.getPreferences().collect { prefs ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        debugLoggingEnabled = prefs.debugLoggingEnabled,
                        logAllCategories = prefs.logAllDebugCategories,
                        logSpotCard = prefs.logSpotCard,
                        logPrivacy = prefs.logPrivacy,
                        logFeedComponent = prefs.logFeedComponent,
                        logPostFlow = prefs.logPostFlow,
                        logAuth = prefs.logAuth,
                        logNetworkComponent = prefs.logNetworkComponent,
                        logDeepLink = prefs.logDeepLink,
                    )
                }
            }
        }
    }
}

data class DebugLoggingUiState(
    val isLoading: Boolean = true,
    val debugLoggingEnabled: Boolean = false,
    val logAllCategories: Boolean = false,
    val logSpotCard: Boolean = false,
    val logPrivacy: Boolean = false,
    val logFeedComponent: Boolean = false,
    val logPostFlow: Boolean = false,
    val logAuth: Boolean = false,
    val logNetworkComponent: Boolean = false,
    val logDeepLink: Boolean = false,
)
