package com.spot.android.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spot.android.core.design.component.PermissionType
import com.spot.android.core.logging.LogCategory
import com.spot.android.core.logging.SpotLogger
import com.spot.android.core.supabase.SessionBridge
import com.spot.android.data.auth.UserSessionHolder
import com.spot.android.data.onboarding.FirstRunOnboardingPreferences
import com.spot.android.data.permissions.PermissionState
import com.spot.android.data.permissions.PermissionsRepository
import com.spot.android.navigation.ShellNavigationBus
import com.spot.android.navigation.SpotTab
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * First-run coach tour manager. Mirrors iOS SpotFirstRunOnboardingManager.
 *
 * Reference: PRD/05-auth-onboarding.md
 */
@HiltViewModel
class FirstRunOnboardingViewModel @Inject constructor(
    private val preferences: FirstRunOnboardingPreferences,
    private val userSessionHolder: UserSessionHolder,
    private val sessionBridge: SessionBridge,
    private val shellNavigationBus: ShellNavigationBus,
    private val permissionsRepository: PermissionsRepository,
    private val logger: SpotLogger,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FirstRunUiState())
    val uiState: StateFlow<FirstRunUiState> = _uiState.asStateFlow()

    private val _effects = Channel<FirstRunEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var startJob: Job? = null
    private var uiTestMode: Boolean = false

    fun setUiTestMode(enabled: Boolean) {
        uiTestMode = enabled
    }

    fun startIfNeeded() {
        if (uiTestMode) return
        startJob?.cancel()
        startJob = viewModelScope.launch {
            val snapshot = preferences.getSnapshot()
            if (snapshot.hasCompletedOrSkipped) {
                _uiState.update {
                    it.copy(
                        hasCompletedOrSkipped = true,
                        currentStep = FirstRunStep.FINALE,
                        isPresented = false,
                    )
                }
                return@launch
            }

            // Wait for engagement sets to settle after auth (~500ms per PRD).
            delay(500)
            val isAuthenticated = sessionBridge.currentUserId != null
            val likesEmpty = userSessionHolder.likedSpots.value.isEmpty()
            val bookmarksEmpty = userSessionHolder.bookmarkedSpots.value.isEmpty()
            val isFirstSessionCandidate = likesEmpty && bookmarksEmpty

            if (!isAuthenticated || !isFirstSessionCandidate) return@launch
            if (_uiState.value.isPresented || _uiState.value.hasCompletedOrSkipped) return@launch

            logger.i(LogCategory.Auth, "FirstRun", "Starting first-run coach tour")
            _uiState.update {
                it.copy(
                    isPresented = true,
                    currentStep = FirstRunStep.WELCOME,
                    hasCompletedOrSkipped = false,
                )
            }
            preferences.setLastStep(FirstRunStep.WELCOME.ordinal)
        }
    }

    fun onPrimary() {
        val state = _uiState.value
        when {
            state.currentStep == FirstRunStep.WELCOME -> startTour()
            state.isFinale -> finish()
            else -> next()
        }
    }

    fun startTour() {
        viewModelScope.launch {
            _uiState.update { it.copy(currentStep = FirstRunStep.SPOT_CARD) }
            preferences.setLastStep(FirstRunStep.SPOT_CARD.ordinal)
        }
    }

    fun next() {
        viewModelScope.launch {
            val current = _uiState.value.currentStep
            val next = current.nextOrNull()
            if (next == null) {
                complete(skipped = false)
                return@launch
            }
            applyStep(next)
        }
    }

    fun back() {
        viewModelScope.launch {
            val current = _uiState.value.currentStep
            if (!current.canGoBack) return@launch
            val previous = current.previousOrNull() ?: return@launch
            applyStep(previous)
        }
    }

    fun skip() {
        viewModelScope.launch { complete(skipped = true) }
    }

    fun finish() {
        viewModelScope.launch { complete(skipped = false) }
    }

    fun onMapTabSelected() {
        if (_uiState.value.isPresented && _uiState.value.currentStep == FirstRunStep.MAP_TAB) {
            next()
        }
    }

    private suspend fun applyStep(step: FirstRunStep) {
        when (step) {
            FirstRunStep.MAP_TAB -> shellNavigationBus.navigateToTab(SpotTab.Map)
            FirstRunStep.USER_LOCATION -> {
                shellNavigationBus.navigateToTab(SpotTab.Map)
                val locationState = permissionsRepository.getState(PermissionType.LOCATION)
                if (locationState == PermissionState.NOT_DETERMINED) {
                    _effects.send(FirstRunEffect.RequestLocationPermission)
                }
            }
            FirstRunStep.MAP_MARKERS,
            FirstRunStep.MARKER_PREVIEW,
            -> shellNavigationBus.navigateToTab(SpotTab.Map)
            else -> Unit
        }
        _uiState.update { it.copy(currentStep = step) }
        preferences.setLastStep(step.ordinal)
    }

    private suspend fun complete(skipped: Boolean) {
        preferences.markCompleted(skipped = skipped)
        _uiState.update {
            it.copy(
                isPresented = false,
                hasCompletedOrSkipped = true,
                currentStep = FirstRunStep.FINALE,
            )
        }
        logger.i(
            LogCategory.Auth,
            "FirstRun",
            if (skipped) "First-run tour skipped" else "First-run tour completed",
        )
        delay(600)
        val notifState = permissionsRepository.getState(PermissionType.NOTIFICATIONS)
        if (notifState == PermissionState.NOT_DETERMINED) {
            _effects.send(FirstRunEffect.RequestNotificationPermission)
        }
    }
}

sealed interface FirstRunEffect {
    data object RequestLocationPermission : FirstRunEffect
    data object RequestNotificationPermission : FirstRunEffect
}
