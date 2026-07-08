package com.spot.android.feature.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spot.android.BuildConfig
import com.spot.android.core.logging.LogCategory
import com.spot.android.core.logging.SpotLogger
import com.spot.android.data.auth.UserSessionHolder
import com.spot.android.data.billing.BillingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for Subscription Settings screen.
 *
 * Reference: PRD/11-settings.md, PRD/12-pro-subscription.md
 */
@HiltViewModel
class SubscriptionSettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userSessionHolder: UserSessionHolder,
    private val billingRepository: BillingRepository,
    private val logger: SpotLogger,
) : ViewModel() {

    private val _localState = MutableStateFlow(SubscriptionSettingsLocalState())
    val uiState: StateFlow<SubscriptionSettingsUiState> = combine(
        _localState,
        userSessionHolder.isPro,
        userSessionHolder.proUntil,
    ) { localState, isPro, proUntil ->
        SubscriptionSettingsUiState(
            isLoading = localState.isLoading,
            isPro = isPro,
            proUntil = proUntil,
            isRestoringPurchases = localState.isRestoringPurchases,
        )
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = SubscriptionSettingsUiState(),
    )

    private val _effects = Channel<SubscriptionSettingsEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onFirstAppear() {
        _localState.update { it.copy(isLoading = false) }
    }

    fun onGoProClicked() {
        viewModelScope.launch {
            logger.d(LogCategory.Billing, TAG, "Go Pro clicked")
            _effects.send(SubscriptionSettingsEffect.ShowPaywall)
        }
    }

    fun onManageSubscriptionClicked() {
        viewModelScope.launch {
            logger.d(LogCategory.Billing, TAG, "Manage subscription clicked")
            openGooglePlaySubscriptions()
        }
    }

    fun onRestorePurchasesClicked() {
        _localState.update { it.copy(isRestoringPurchases = true) }
        viewModelScope.launch {
            logger.d(LogCategory.Billing, TAG, "Restore purchases clicked")
            
            val userId = userSessionHolder.currentUserId.value
            if (userId == null) {
                _localState.update { it.copy(isRestoringPurchases = false) }
                _effects.send(SubscriptionSettingsEffect.ShowError("Not authenticated"))
                return@launch
            }

            val result = billingRepository.restorePurchases(userId)
            _localState.update { it.copy(isRestoringPurchases = false) }

            when {
                result is com.spot.android.data.billing.BillingResult.Success -> {
                    _effects.send(SubscriptionSettingsEffect.ShowSuccess("Purchases restored"))
                }
                result is com.spot.android.data.billing.BillingResult.Error -> {
                    _effects.send(SubscriptionSettingsEffect.ShowError(result.message))
                }
            }
        }
    }

    private fun openGooglePlaySubscriptions() {
        try {
            // Open Google Play subscriptions deep link
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/account/subscriptions?package=${context.packageName}&sku=${BuildConfig.PLAY_PRO_PRODUCT_ID}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            logger.e(LogCategory.Billing, TAG, "Failed to open subscriptions", e)
            viewModelScope.launch {
                _effects.send(SubscriptionSettingsEffect.ShowError("Failed to open subscription management"))
            }
        }
    }

    private companion object {
        const val TAG = "SubscriptionSettingsViewModel"
    }
}

private data class SubscriptionSettingsLocalState(
    val isLoading: Boolean = true,
    val isRestoringPurchases: Boolean = false,
)

data class SubscriptionSettingsUiState(
    val isLoading: Boolean = true,
    val isPro: Boolean = false,
    val proUntil: Long? = null,
    val isRestoringPurchases: Boolean = false,
)

sealed interface SubscriptionSettingsEffect {
    data class ShowError(val message: String) : SubscriptionSettingsEffect
    data class ShowSuccess(val message: String) : SubscriptionSettingsEffect
    data object ShowPaywall : SubscriptionSettingsEffect
}
