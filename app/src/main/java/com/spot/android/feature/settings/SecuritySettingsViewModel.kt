package com.spot.android.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spot.android.core.logging.LogCategory
import com.spot.android.core.logging.SpotLogger
import com.spot.android.data.auth.UserSessionRepository
import com.spot.android.data.model.UserBrief
import com.spot.android.data.safety.SafetyRepository
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
 * ViewModel for Security Settings screen.
 *
 * Reference: PRD/11-settings.md
 */
@HiltViewModel
class SecuritySettingsViewModel @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val safetyRepository: SafetyRepository,
    private val logger: SpotLogger,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SecuritySettingsUiState())
    val uiState: StateFlow<SecuritySettingsUiState> = _uiState.asStateFlow()

    private val _effects = Channel<SecuritySettingsEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onFirstAppear() {
        loadSecuritySettings()
        loadBlockedUsers()
    }

    fun onPrivateAccountToggled(isPrivate: Boolean) {
        _uiState.update { it.copy(isPrivateAccount = isPrivate, isUpdatingPrivacy = true) }
        viewModelScope.launch {
            userSessionRepository.updatePrivateAccount(isPrivate).fold(
                onSuccess = {
                    _uiState.update { it.copy(isUpdatingPrivacy = false) }
                    _effects.send(SecuritySettingsEffect.ShowSuccess("Privacy setting updated"))
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isPrivateAccount = !isPrivate, // Revert on failure
                            isUpdatingPrivacy = false,
                        )
                    }
                    _effects.send(SecuritySettingsEffect.ShowError("Failed to update privacy: ${error.message}"))
                },
            )
        }
    }

    fun onUnblockUser(userId: String) {
        viewModelScope.launch {
            safetyRepository.unblockUser(userId).fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            blockedUsers = state.blockedUsers.filter { it.id != userId },
                        )
                    }
                    _effects.send(SecuritySettingsEffect.ShowSuccess("User unblocked"))
                },
                onFailure = { error ->
                    _effects.send(SecuritySettingsEffect.ShowError("Failed to unblock user: ${error.message}"))
                },
            )
        }
    }

    private fun loadSecuritySettings() {
        viewModelScope.launch {
            // Load privacy setting from session
            val isPrivate = userSessionRepository.isPrivateAccount() ?: false
            _uiState.update { 
                it.copy(
                    isPrivateAccount = isPrivate,
                    isLoading = false,
                )
            }
        }
    }

    private fun loadBlockedUsers() {
        _uiState.update { it.copy(isLoadingBlockedUsers = true) }
        viewModelScope.launch {
            safetyRepository.getBlockedUsers().fold(
                onSuccess = { users ->
                    _uiState.update {
                        it.copy(
                            blockedUsers = users,
                            isLoadingBlockedUsers = false,
                        )
                    }
                },
                onFailure = { error ->
                    logger.e(LogCategory.Network, "Failed to load blocked users", error)
                    _uiState.update { it.copy(isLoadingBlockedUsers = false) }
                },
            )
        }
    }
}

data class SecuritySettingsUiState(
    val isLoading: Boolean = true,
    val isPrivateAccount: Boolean = false,
    val isUpdatingPrivacy: Boolean = false,
    val blockedUsers: List<User> = emptyList(),
    val isLoadingBlockedUsers: Boolean = true,
)

sealed interface SecuritySettingsEffect {
    data class ShowError(val message: String) : SecuritySettingsEffect
    data class ShowSuccess(val message: String) : SecuritySettingsEffect
}
