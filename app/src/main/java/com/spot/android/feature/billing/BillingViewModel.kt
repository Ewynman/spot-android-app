package com.spot.android.feature.billing

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spot.android.core.logging.LogCategory
import com.spot.android.core.logging.SpotLogger
import com.spot.android.core.supabase.SessionBridge
import com.spot.android.data.auth.UserSessionHolder
import com.spot.android.data.billing.BillingRepository
import com.spot.android.data.billing.BillingResult
import com.spot.android.data.billing.BillingState
import com.spot.android.data.billing.ProOnboardingPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for Pro subscription purchase, restore, and onboarding flows.
 *
 * Reference: PRD/12-pro-subscription.md
 */
@HiltViewModel
class BillingViewModel @Inject constructor(
    private val billingRepository: BillingRepository,
    private val sessionBridge: SessionBridge,
    private val userSessionHolder: UserSessionHolder,
    private val proOnboardingPreferences: ProOnboardingPreferences,
    private val logger: SpotLogger,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BillingUiState())
    val uiState: StateFlow<BillingUiState> = combine(
        _uiState,
        billingRepository.state,
        billingRepository.productDetails,
        billingRepository.errorMessage,
        userSessionHolder.isPro,
    ) { state, billingState, product, errorMsg, isPro ->
        state.copy(
            billingState = billingState,
            productDetails = product,
            billingErrorMessage = errorMsg,
            isPro = isPro,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = _uiState.value
    )

    private val _effects = Channel<BillingEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            billingRepository.initialize()
        }
    }

    fun purchasePro(activity: Activity, entryPoint: String?) {
        viewModelScope.launch {
            val userId = sessionBridge.currentUserId
            if (userId == null) {
                logger.w(LogCategory.Billing, TAG, "Purchase attempted without user ID")
                _uiState.update { it.copy(userErrorMessage = "Not authenticated") }
                return@launch
            }

            _uiState.update { it.copy(userErrorMessage = null) }

            when (val result = billingRepository.purchasePro(activity, userId)) {
                is BillingResult.Success -> {
                    logger.i(LogCategory.Billing, TAG, "Purchase successful from $entryPoint")
                    
                    val shouldShowOnboarding = !proOnboardingPreferences.hasSeenOnboarding(userId).first()
                    
                    if (shouldShowOnboarding) {
                        _effects.send(BillingEffect.ShowProOnboarding)
                        proOnboardingPreferences.setOnboardingSeen(userId)
                    } else {
                        _effects.send(BillingEffect.ShowProSuccess)
                    }
                }
                is BillingResult.Error -> {
                    logger.w(LogCategory.Billing, TAG, "Purchase error: ${result.message}")
                    _uiState.update { it.copy(userErrorMessage = result.message) }
                }
                is BillingResult.Canceled -> {
                    logger.d(LogCategory.Billing, TAG, "Purchase canceled")
                }
            }
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            val userId = sessionBridge.currentUserId
            if (userId == null) {
                logger.w(LogCategory.Billing, TAG, "Restore attempted without user ID")
                _uiState.update { it.copy(userErrorMessage = "Not authenticated") }
                return@launch
            }

            _uiState.update { it.copy(userErrorMessage = null) }

            when (val result = billingRepository.restorePurchases(userId)) {
                is BillingResult.Success -> {
                    logger.i(LogCategory.Billing, TAG, "Restore successful")
                    _effects.send(BillingEffect.ShowRestoreSuccess)
                }
                is BillingResult.Error -> {
                    logger.w(LogCategory.Billing, TAG, "Restore error: ${result.message}")
                    _uiState.update { it.copy(userErrorMessage = result.message) }
                }
                is BillingResult.Canceled -> {
                    // Not applicable for restore
                }
            }
        }
    }

    fun retryLoadProducts() {
        viewModelScope.launch {
            billingRepository.initialize()
        }
    }

    fun clearUserError() {
        _uiState.update { it.copy(userErrorMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        billingRepository.disconnect()
    }

    private companion object {
        const val TAG = "BillingViewModel"
    }
}
