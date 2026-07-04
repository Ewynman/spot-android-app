package com.spot.android.data.billing

import com.spot.android.core.logging.LogCategory
import com.spot.android.core.logging.SpotLogger
import com.spot.android.core.supabase.SupabaseClientProvider
import com.spot.android.data.auth.UserSessionHolder
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Service for server-side purchase verification and Pro entitlement syncing.
 *
 * Reference: PRD/12-pro-subscription.md
 */
@Singleton
class BillingService @Inject constructor(
    private val supabaseProvider: SupabaseClientProvider,
    private val userSessionHolder: UserSessionHolder,
    private val logger: SpotLogger,
) {
    private val postgrest get() = supabaseProvider.client.postgrest

    /**
     * Verify purchase token with Play Developer API (server-side) and sync Pro entitlement.
     *
     * In production, this should call a Supabase edge function that verifies the purchase
     * with Google Play Developer API and updates users.is_pro/pro_until.
     *
     * For now, this is a stub that would trigger the server verification.
     */
    suspend fun verifyAndSyncPurchase(purchaseToken: String): Boolean {
        return try {
            val result = postgrest.rpc(
                function = "verify_play_purchase_v1",
                parameters = VerifyPurchaseParams(
                    p_purchase_token = purchaseToken,
                    p_platform = "android"
                )
            ).decodeAs<VerifyPurchaseResult>()

            if (result.success) {
                userSessionHolder.updateProStatus(
                    isPro = result.is_pro,
                    proUntil = result.pro_until_ms
                )
                logger.i(LogCategory.Billing, TAG, "Purchase verified and synced: isPro=${result.is_pro}")
            }

            result.success
        } catch (e: Exception) {
            logger.e(LogCategory.Billing, TAG, "Verify purchase failed", e)
            false
        }
    }

    private companion object {
        const val TAG = "BillingService"
    }
}

@Serializable
private data class VerifyPurchaseParams(
    @SerialName("p_purchase_token") val p_purchase_token: String,
    @SerialName("p_platform") val p_platform: String,
)

@Serializable
private data class VerifyPurchaseResult(
    @SerialName("success") val success: Boolean,
    @SerialName("is_pro") val is_pro: Boolean = false,
    @SerialName("pro_until_ms") val pro_until_ms: Long? = null,
)
