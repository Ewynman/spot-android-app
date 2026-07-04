package com.spot.android.data.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult as AndroidBillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.spot.android.BuildConfig
import com.spot.android.core.logging.LogCategory
import com.spot.android.core.logging.SpotLogger
import com.spot.android.data.auth.UserSessionHolder
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * Play Billing implementation of BillingRepository.
 *
 * Handles Google Play Billing v6+ subscription flows, account binding via obfuscatedAccountId,
 * and entitlement syncing with the Supabase backend.
 *
 * Reference: PRD/12-pro-subscription.md
 */
@Singleton
class PlayBillingRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val billingService: BillingService,
    private val userSessionHolder: UserSessionHolder,
    private val logger: SpotLogger,
) : BillingRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _state = MutableStateFlow(BillingState.IDLE)
    override val state: StateFlow<BillingState> = _state.asStateFlow()

    private val _productDetails = MutableStateFlow<ProProductDetails?>(null)
    override val productDetails: StateFlow<ProProductDetails?> = _productDetails.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    override val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var billingClient: BillingClient? = null
    private var cachedProductDetails: ProductDetails? = null

    private val purchaseChannel = Channel<AndroidBillingResult>(Channel.CONFLATED)

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        scope.launch {
            logger.d(LogCategory.Billing, TAG, "onPurchasesUpdated: ${billingResult.responseCode}")
            purchaseChannel.send(billingResult)
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                handlePurchases(purchases)
            }
        }
    }

    override suspend fun initialize() = withContext(Dispatchers.Main) {
        if (billingClient != null) {
            logger.d(LogCategory.Billing, TAG, "BillingClient already initialized")
            return@withContext
        }

        _state.value = BillingState.LOADING_PRODUCTS
        _errorMessage.value = null

        try {
            val client = BillingClient.newBuilder(context)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build()

            billingClient = client

            val connected = suspendCancellableCoroutine { continuation ->
                client.startConnection(object : BillingClientStateListener {
                    override fun onBillingSetupFinished(billingResult: AndroidBillingResult) {
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            logger.i(LogCategory.Billing, TAG, "Billing client connected")
                            continuation.resume(true)
                        } else {
                            logger.w(LogCategory.Billing, TAG, "Billing setup failed: ${billingResult.debugMessage}")
                            continuation.resume(false)
                        }
                    }

                    override fun onBillingServiceDisconnected() {
                        logger.w(LogCategory.Billing, TAG, "Billing service disconnected")
                    }
                })
            }

            if (!connected) {
                _state.value = BillingState.ERROR
                _errorMessage.value = "Couldn't connect to Play Billing"
                return@withContext
            }

            loadProductDetails(client)
        } catch (e: Exception) {
            logger.e(LogCategory.Billing, TAG, "Initialize failed", e)
            _state.value = BillingState.ERROR
            _errorMessage.value = "Billing initialization failed"
        }
    }

    private suspend fun loadProductDetails(client: BillingClient) = withContext(Dispatchers.Main) {
        try {
            val productList = listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(BuildConfig.PRODUCT_ID_PRO_YEARLY)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )

            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build()

            val result = client.queryProductDetails(params)

            if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val details = result.productDetailsList?.firstOrNull()
                if (details != null) {
                    cachedProductDetails = details
                    _productDetails.value = mapProductDetails(details)
                    _state.value = BillingState.READY
                    logger.i(LogCategory.Billing, TAG, "Product details loaded: ${details.productId}")
                } else {
                    logger.w(LogCategory.Billing, TAG, "No product details found")
                    _state.value = BillingState.ERROR
                    _errorMessage.value = "Product not available"
                }
            } else {
                logger.w(LogCategory.Billing, TAG, "Query products failed: ${result.billingResult.debugMessage}")
                _state.value = BillingState.ERROR
                _errorMessage.value = "Couldn't load products"
            }
        } catch (e: Exception) {
            logger.e(LogCategory.Billing, TAG, "Load product details failed", e)
            _state.value = BillingState.ERROR
            _errorMessage.value = "Failed to load products"
        }
    }

    private fun mapProductDetails(details: ProductDetails): ProProductDetails {
        val offerDetails = details.subscriptionOfferDetails?.firstOrNull()
        val pricingPhase = offerDetails?.pricingPhases?.pricingPhaseList?.firstOrNull()

        return ProProductDetails(
            productId = details.productId,
            title = details.title,
            description = details.description,
            formattedPrice = pricingPhase?.formattedPrice ?: "",
            priceAmountMicros = pricingPhase?.priceAmountMicros ?: 0L,
            priceCurrencyCode = pricingPhase?.priceCurrencyCode ?: "USD",
        )
    }

    override suspend fun purchasePro(activity: Activity, userId: String): BillingResult = withContext(Dispatchers.Main) {
        val client = billingClient
        val product = cachedProductDetails

        if (client == null || product == null) {
            logger.w(LogCategory.Billing, TAG, "Purchase attempted but client or product not ready")
            return@withContext BillingResult.Error("Billing not ready")
        }

        _state.value = BillingState.PURCHASING
        _errorMessage.value = null

        try {
            val offerToken = product.subscriptionOfferDetails?.firstOrNull()?.offerToken
            if (offerToken == null) {
                logger.w(LogCategory.Billing, TAG, "No offer token found")
                _state.value = BillingState.READY
                return@withContext BillingResult.Error("Product not available")
            }

            val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(product)
                .setOfferToken(offerToken)
                .build()

            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productDetailsParams))
                .setObfuscatedAccountId(userId)
                .build()

            val launchResult = client.launchBillingFlow(activity, flowParams)

            if (launchResult.responseCode != BillingClient.BillingResponseCode.OK) {
                logger.w(LogCategory.Billing, TAG, "Launch billing flow failed: ${launchResult.debugMessage}")
                _state.value = BillingState.READY
                return@withContext BillingResult.Error(launchResult.debugMessage ?: "Purchase failed")
            }

            val billingResult = purchaseChannel.receive()

            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    _state.value = BillingState.READY
                    BillingResult.Success
                }
                BillingClient.BillingResponseCode.USER_CANCELED -> {
                    logger.d(LogCategory.Billing, TAG, "Purchase canceled by user")
                    _state.value = BillingState.READY
                    BillingResult.Canceled
                }
                else -> {
                    logger.w(LogCategory.Billing, TAG, "Purchase failed: ${billingResult.debugMessage}")
                    _state.value = BillingState.READY
                    BillingResult.Error(billingResult.debugMessage ?: "Purchase failed")
                }
            }
        } catch (e: Exception) {
            logger.e(LogCategory.Billing, TAG, "Purchase error", e)
            _state.value = BillingState.READY
            BillingResult.Error("Purchase error: ${e.message}")
        }
    }

    override suspend fun restorePurchases(userId: String): BillingResult = withContext(Dispatchers.IO) {
        val client = billingClient
        if (client == null) {
            logger.w(LogCategory.Billing, TAG, "Restore attempted but client not ready")
            return@withContext BillingResult.Error("Billing not ready")
        }

        _state.value = BillingState.RESTORING
        _errorMessage.value = null

        try {
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()

            val result = client.queryPurchasesAsync(params)

            if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                logger.d(LogCategory.Billing, TAG, "Restore found ${result.purchasesList.size} purchases")
                
                val hasPro = handlePurchases(result.purchasesList, userId)
                
                _state.value = BillingState.READY
                if (hasPro) {
                    BillingResult.Success
                } else {
                    BillingResult.Error("No active Pro subscription found")
                }
            } else {
                logger.w(LogCategory.Billing, TAG, "Restore failed: ${result.billingResult.debugMessage}")
                _state.value = BillingState.READY
                BillingResult.Error(result.billingResult.debugMessage ?: "Restore failed")
            }
        } catch (e: Exception) {
            logger.e(LogCategory.Billing, TAG, "Restore error", e)
            _state.value = BillingState.READY
            BillingResult.Error("Restore error: ${e.message}")
        }
    }

    override suspend fun queryEntitlement(userId: String): Boolean = withContext(Dispatchers.IO) {
        val client = billingClient ?: return@withContext false

        try {
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()

            val result = client.queryPurchasesAsync(params)

            if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                return@withContext hasValidProPurchase(result.purchasesList, userId)
            }
        } catch (e: Exception) {
            logger.e(LogCategory.Billing, TAG, "Query entitlement error", e)
        }

        return@withContext false
    }

    private suspend fun handlePurchases(purchases: List<Purchase>, currentUserId: String? = null): Boolean {
        var foundPro = false

        for (purchase in purchases) {
            if (purchase.products.contains(BuildConfig.PRODUCT_ID_PRO_YEARLY)) {
                val accountId = purchase.accountIdentifiers?.obfuscatedAccountId
                val userId = currentUserId ?: userSessionHolder.currentUserUsername.value

                if (accountId != null && userId != null && accountId != userId) {
                    logger.w(LogCategory.Billing, TAG, "Purchase account mismatch - not granting Pro")
                    continue
                }

                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    foundPro = true
                    
                    if (!purchase.isAcknowledged) {
                        acknowledgePurchase(purchase)
                    }

                    syncProEntitlement(purchase)
                }
            }
        }

        return foundPro
    }

    private fun hasValidProPurchase(purchases: List<Purchase>, userId: String): Boolean {
        return purchases.any { purchase ->
            purchase.products.contains(BuildConfig.PRODUCT_ID_PRO_YEARLY) &&
            purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
            (purchase.accountIdentifiers?.obfuscatedAccountId == userId || 
             purchase.accountIdentifiers?.obfuscatedAccountId == null)
        }
    }

    private suspend fun acknowledgePurchase(purchase: Purchase) {
        val client = billingClient ?: return

        try {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()

            val result = client.acknowledgePurchase(params)
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                logger.i(LogCategory.Billing, TAG, "Purchase acknowledged")
            } else {
                logger.w(LogCategory.Billing, TAG, "Acknowledge failed: ${result.debugMessage}")
            }
        } catch (e: Exception) {
            logger.e(LogCategory.Billing, TAG, "Acknowledge error", e)
        }
    }

    private suspend fun syncProEntitlement(purchase: Purchase) {
        try {
            val success = billingService.verifyAndSyncPurchase(purchase.purchaseToken)
            if (success) {
                logger.i(LogCategory.Billing, TAG, "Pro entitlement synced to backend")
            } else {
                logger.w(LogCategory.Billing, TAG, "Failed to sync Pro entitlement")
            }
        } catch (e: Exception) {
            logger.e(LogCategory.Billing, TAG, "Sync entitlement error", e)
        }
    }

    override fun disconnect() {
        billingClient?.endConnection()
        billingClient = null
        cachedProductDetails = null
        _state.value = BillingState.IDLE
        _productDetails.value = null
        logger.d(LogCategory.Billing, TAG, "Billing client disconnected")
    }

    private companion object {
        const val TAG = "PlayBillingRepository"
    }
}
