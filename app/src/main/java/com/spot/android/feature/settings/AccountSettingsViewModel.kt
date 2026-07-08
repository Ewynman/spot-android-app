package com.spot.android.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spot.android.core.logging.LogCategory
import com.spot.android.core.logging.SpotLogger
import com.spot.android.data.auth.AuthRepository
import com.spot.android.data.auth.UserSessionHolder
import com.spot.android.data.settings.SettingsRepository
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
 * ViewModel for Account Settings screen.
 *
 * Reference: PRD/11-settings.md
 */
@HiltViewModel
class AccountSettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
    private val userSessionHolder: UserSessionHolder,
    private val logger: SpotLogger,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountSettingsUiState())
    val uiState: StateFlow<AccountSettingsUiState> = _uiState.asStateFlow()

    private val _effects = Channel<AccountSettingsEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onFirstAppear() {
        loadCurrentUser()
    }

    fun onChangePasswordRequested() {
        _uiState.update { it.copy(showChangePasswordDialog = true) }
    }

    fun onChangePasswordCancelled() {
        _uiState.update { 
            it.copy(
                showChangePasswordDialog = false,
                currentPassword = "",
                newPassword = "",
                confirmPassword = "",
            )
        }
    }

    fun onCurrentPasswordChanged(password: String) {
        _uiState.update { it.copy(currentPassword = password) }
    }

    fun onNewPasswordChanged(password: String) {
        _uiState.update { it.copy(newPassword = password) }
    }

    fun onConfirmPasswordChanged(password: String) {
        _uiState.update { it.copy(confirmPassword = password) }
    }

    fun onChangePasswordConfirmed() {
        val state = _uiState.value
        if (state.newPassword != state.confirmPassword) {
            viewModelScope.launch {
                _effects.send(AccountSettingsEffect.ShowError("Passwords do not match"))
            }
            return
        }
        if (state.newPassword.length < 6) {
            viewModelScope.launch {
                _effects.send(AccountSettingsEffect.ShowError("Password must be at least 6 characters"))
            }
            return
        }

        _uiState.update { it.copy(isChangingPassword = true) }
        viewModelScope.launch {
            settingsRepository.changePassword(
                currentPassword = state.currentPassword,
                newPassword = state.newPassword,
            ).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isChangingPassword = false,
                            showChangePasswordDialog = false,
                            currentPassword = "",
                            newPassword = "",
                            confirmPassword = "",
                        )
                    }
                    _effects.send(AccountSettingsEffect.ShowSuccess("Password changed successfully"))
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isChangingPassword = false) }
                    _effects.send(AccountSettingsEffect.ShowError("Failed to change password: ${error.message}"))
                },
            )
        }
    }

    fun onLogoutRequested() {
        _uiState.update { it.copy(showLogoutDialog = true) }
    }

    fun onLogoutCancelled() {
        _uiState.update { it.copy(showLogoutDialog = false) }
    }

    fun onLogoutConfirmed() {
        _uiState.update { it.copy(isLoggingOut = true) }
        viewModelScope.launch {
            authRepository.signOut()
            _effects.send(AccountSettingsEffect.NavigateToWelcome)
        }
    }

    fun onDeleteAccountRequested() {
        _uiState.update { it.copy(showDeleteAccountDialog = true) }
    }

    fun onDeleteAccountCancelled() {
        _uiState.update { 
            it.copy(
                showDeleteAccountDialog = false,
                deleteAccountPassword = "",
                deleteAccountConfirmed = false,
            )
        }
    }

    fun onDeleteAccountPasswordChanged(password: String) {
        _uiState.update { it.copy(deleteAccountPassword = password) }
    }

    fun onDeleteAccountConfirmationChanged(confirmed: Boolean) {
        _uiState.update { it.copy(deleteAccountConfirmed = confirmed) }
    }

    fun onDeleteAccountConfirmed() {
        val state = _uiState.value
        if (!state.deleteAccountConfirmed) {
            viewModelScope.launch {
                _effects.send(AccountSettingsEffect.ShowError("Please confirm account deletion"))
            }
            return
        }

        _uiState.update { it.copy(isDeletingAccount = true) }
        viewModelScope.launch {
            settingsRepository.deleteAccount(
                password = state.deleteAccountPassword.takeIf { it.isNotBlank() },
                useOAuthReauth = state.deleteAccountPassword.isBlank(),
            ).fold(
                onSuccess = {
                    _effects.send(AccountSettingsEffect.NavigateToWelcome)
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isDeletingAccount = false) }
                    _effects.send(AccountSettingsEffect.ShowError("Failed to delete account: ${error.message}"))
                },
            )
        }
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val session = userSessionHolder.sessionSnapshot.value
            _uiState.update {
                it.copy(
                    username = session?.username ?: "",
                    email = session?.email ?: "",
                    isLoading = false,
                )
            }
        }
    }
}

data class AccountSettingsUiState(
    val isLoading: Boolean = true,
    val username: String = "",
    val email: String = "",
    val showChangePasswordDialog: Boolean = false,
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isChangingPassword: Boolean = false,
    val showLogoutDialog: Boolean = false,
    val isLoggingOut: Boolean = false,
    val showDeleteAccountDialog: Boolean = false,
    val deleteAccountPassword: String = "",
    val deleteAccountConfirmed: Boolean = false,
    val isDeletingAccount: Boolean = false,
)

sealed interface AccountSettingsEffect {
    data class ShowError(val message: String) : AccountSettingsEffect
    data class ShowSuccess(val message: String) : AccountSettingsEffect
    data object NavigateToWelcome : AccountSettingsEffect
}
