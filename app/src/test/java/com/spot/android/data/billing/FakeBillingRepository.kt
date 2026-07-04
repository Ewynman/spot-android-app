package com.spot.android.data.billing

import android.app.Activity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Fake implementation of BillingRepository for testing.
 */
class FakeBillingRepository : BillingRepository {
    
    private val _state = MutableStateFlow(BillingState.IDLE)
    override val state: StateFlow<BillingState> = _state.asStateFlow()

    private val _productDetails = MutableStateFlow<ProProductDetails?>(null)
    override val productDetails: StateFlow<ProProductDetails?> = _productDetails.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    override val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    var initializeResult: Result<Unit> = Result.success(Unit)
    var purchaseResult: BillingResult = BillingResult.Success
    var restoreResult: BillingResult = BillingResult.Success
    var entitlementResult: Boolean = false

    override suspend fun initialize() {
        initializeResult.fold(
            onSuccess = {
                _state.value = BillingState.READY
                _productDetails.value = ProProductDetails(
                    productId = "spot_pro_yearly",
                    title = "Spot Pro",
                    description = "Annual subscription",
                    formattedPrice = "$49.99",
                    priceAmountMicros = 49990000L,
                    priceCurrencyCode = "USD",
                )
            },
            onFailure = {
                _state.value = BillingState.ERROR
                _errorMessage.value = it.message
            }
        )
    }

    override suspend fun purchasePro(activity: Activity, userId: String): BillingResult {
        _state.value = BillingState.PURCHASING
        val result = purchaseResult
        _state.value = BillingState.READY
        return result
    }

    override suspend fun restorePurchases(userId: String): BillingResult {
        _state.value = BillingState.RESTORING
        val result = restoreResult
        _state.value = BillingState.READY
        return result
    }

    override suspend fun queryEntitlement(userId: String): Boolean {
        return entitlementResult
    }

    override fun disconnect() {
        _state.value = BillingState.IDLE
        _productDetails.value = null
        _errorMessage.value = null
    }
}
