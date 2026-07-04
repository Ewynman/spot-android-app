package com.spot.android.feature.billing

import com.spot.android.data.billing.BillingState
import com.spot.android.data.billing.ProProductDetails

/**
 * UI state for billing/paywall screens.
 */
data class BillingUiState(
    val billingState: BillingState = BillingState.IDLE,
    val productDetails: ProProductDetails? = null,
    val billingErrorMessage: String? = null,
    val userErrorMessage: String? = null,
    val isPro: Boolean = false,
)

/**
 * One-shot effects for billing flows.
 */
sealed interface BillingEffect {
    data object ShowProSuccess : BillingEffect
    data object ShowProOnboarding : BillingEffect
    data object ShowRestoreSuccess : BillingEffect
}
