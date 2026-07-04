package com.spot.android.data.billing

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository interface for billing operations.
 *
 * Reference: PRD/12-pro-subscription.md
 */
interface BillingRepository {
    val state: StateFlow<BillingState>
    val productDetails: StateFlow<ProProductDetails?>
    val errorMessage: StateFlow<String?>

    /**
     * Connect to Play Billing and load product details.
     */
    suspend fun initialize()

    /**
     * Launch the purchase flow for Pro subscription.
     *
     * @param activity The activity context for launching the billing flow
     * @param userId The Supabase user UUID to bind the purchase to
     * @return BillingResult indicating success, error, or cancellation
     */
    suspend fun purchasePro(activity: Activity, userId: String): BillingResult

    /**
     * Restore existing purchases and sync entitlement.
     *
     * @param userId The current user's UUID for validation
     * @return BillingResult indicating success or error
     */
    suspend fun restorePurchases(userId: String): BillingResult

    /**
     * Query current entitlement from Play Billing.
     * Returns true if the user has an active Pro subscription.
     */
    suspend fun queryEntitlement(userId: String): Boolean

    /**
     * Disconnect from Play Billing.
     */
    fun disconnect()
}
