package com.spot.android.data.billing

/**
 * Purchase flow state for the billing system.
 *
 * Reference: PRD/12-pro-subscription.md
 */
enum class BillingState {
    IDLE,
    LOADING_PRODUCTS,
    READY,
    PURCHASING,
    RESTORING,
    ERROR,
}

/**
 * Result of a purchase or restore operation.
 */
sealed class BillingResult {
    data object Success : BillingResult()
    data class Error(val message: String) : BillingResult()
    data object Canceled : BillingResult()
}

/**
 * Product details for Pro subscription.
 */
data class ProProductDetails(
    val productId: String,
    val title: String,
    val description: String,
    val formattedPrice: String,
    val priceAmountMicros: Long,
    val priceCurrencyCode: String,
)
