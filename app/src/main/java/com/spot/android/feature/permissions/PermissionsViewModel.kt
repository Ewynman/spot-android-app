package com.spot.android.feature.permissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spot.android.core.analytics.AnalyticsTracker
import com.spot.android.core.design.component.PermissionType
import com.spot.android.core.logging.LogCategory
import com.spot.android.core.logging.SpotLogger
import com.spot.android.data.permissions.PermissionState
import com.spot.android.data.permissions.PermissionsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Coordinates contextual permission requests with pre-prompt → OS dialog flow.
 *
 * Denial never blocks app access. Features call [requestPermission] at the moment of need.
 *
 * Reference: PRD/05-auth-onboarding.md
 */
@HiltViewModel
class PermissionsViewModel @Inject constructor(
    private val permissionsRepository: PermissionsRepository,
    private val analyticsTracker: AnalyticsTracker,
    private val logger: SpotLogger,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PermissionsUiState())
    val uiState: StateFlow<PermissionsUiState> = _uiState.asStateFlow()

    private val _requestResults = Channel<PermissionRequestResult>(Channel.BUFFERED)
    val requestResults = _requestResults.receiveAsFlow()

    private var pendingCallback: ((PermissionState) -> Unit)? = null
    private var pendingType: PermissionType? = null

    init {
        refreshStates()
    }

    fun refreshStates() {
        viewModelScope.launch {
            val states = permissionsRepository.getAllStates()
            val needsAttention = permissionsRepository.hasAnyNeedsAttention()
            _uiState.update {
                it.copy(
                    permissionStates = states,
                    hasAnyNeedsAttention = needsAttention,
                )
            }
        }
    }

    /**
     * Request a permission contextually. Shows pre-prompt if needed; otherwise completes immediately.
     */
    fun requestPermission(
        type: PermissionType,
        onComplete: (PermissionState) -> Unit = {},
    ) {
        viewModelScope.launch {
            val currentState = permissionsRepository.getState(type)
            when {
                currentState == PermissionState.AUTHORIZED -> {
                    onComplete(currentState)
                    _requestResults.send(PermissionRequestResult(type, currentState))
                }
                currentState == PermissionState.PERMANENTLY_DENIED -> {
                    onComplete(currentState)
                    _requestResults.send(PermissionRequestResult(type, currentState))
                }
                permissionsRepository.shouldShowPrePrompt(type) -> {
                    pendingCallback = onComplete
                    pendingType = type
                    _uiState.update { it.copy(activePrePrompt = type) }
                }
                else -> launchSystemPermission(type, onComplete)
            }
        }
    }

    fun onPrePromptContinue() {
        val type = _uiState.value.activePrePrompt ?: return
        viewModelScope.launch {
            permissionsRepository.markRequested(type)
            trackPermissionRequested(type)
            _uiState.update { it.copy(activePrePrompt = null) }

            if (!permissionsRepository.isRuntimePermissionRequired(type)) {
                completeRequest(type, PermissionState.NOT_REQUIRED)
                refreshStates()
                return@launch
            }

            val permissions = permissionsRepository.getAndroidPermissions(type)
            if (permissions.isEmpty()) {
                completeRequest(type, PermissionState.NOT_REQUIRED)
                refreshStates()
                return@launch
            }

            _uiState.update { it.copy(pendingLaunchPermissions = permissions) }
        }
    }

    fun onPrePromptSkip() {
        val type = _uiState.value.activePrePrompt ?: return
        viewModelScope.launch {
            permissionsRepository.markRequested(type)
            logger.d(LogCategory.Privacy, "Permissions", "Permission pre-prompt skipped: ${type.name}")
            _uiState.update { it.copy(activePrePrompt = null) }
            val state = if (permissionsRepository.isRuntimePermissionRequired(type)) {
                PermissionState.DENIED
            } else {
                PermissionState.NOT_REQUIRED
            }
            completeRequest(type, state)
            refreshStates()
        }
    }

    fun onSystemPermissionResult(granted: Boolean) {
        val type = pendingType ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(pendingLaunchPermissions = null) }
            val state = if (granted) {
                PermissionState.AUTHORIZED
            } else {
                PermissionState.DENIED
            }
            completeRequest(type, state)
            refreshStates()
        }
    }

    fun clearPendingLaunch() {
        _uiState.update { it.copy(pendingLaunchPermissions = null) }
    }

    private suspend fun launchSystemPermission(
        type: PermissionType,
        onComplete: (PermissionState) -> Unit,
    ) {
        pendingCallback = onComplete
        pendingType = type

        if (!permissionsRepository.isRuntimePermissionRequired(type)) {
            completeRequest(type, PermissionState.NOT_REQUIRED)
            refreshStates()
            return
        }

        val permissions = permissionsRepository.getAndroidPermissions(type)
        if (permissions.isEmpty()) {
            completeRequest(type, PermissionState.NOT_REQUIRED)
            refreshStates()
            return
        }

        permissionsRepository.markRequested(type)
        trackPermissionRequested(type)
        _uiState.update { it.copy(pendingLaunchPermissions = permissions) }
    }

    private suspend fun completeRequest(type: PermissionType, state: PermissionState) {
        pendingCallback?.invoke(state)
        pendingCallback = null
        pendingType = null
        _requestResults.send(PermissionRequestResult(type, state))
    }

    private fun trackPermissionRequested(type: PermissionType) {
        analyticsTracker.trackPermissionsRequested(type.analyticsValue())
        logger.d(LogCategory.Privacy, "Permissions", "Permission requested: ${type.name}")
    }

    private fun PermissionType.analyticsValue(): String = when (this) {
        PermissionType.LOCATION -> "location"
        PermissionType.CAMERA -> "camera"
        PermissionType.PHOTOS -> "photos"
        PermissionType.NOTIFICATIONS -> "notifications"
    }
}
