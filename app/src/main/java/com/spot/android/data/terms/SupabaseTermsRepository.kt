package com.spot.android.data.terms

import com.spot.android.core.logging.LogCategory
import com.spot.android.core.logging.SpotLogger
import com.spot.android.core.supabase.SupabaseClientProvider
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Supabase-backed terms acceptance repository.
 */
@Singleton
class SupabaseTermsRepository @Inject constructor(
    private val supabaseProvider: SupabaseClientProvider,
    private val logger: SpotLogger,
) : TermsRepository {

    private val postgrest get() = supabaseProvider.client.postgrest

    override suspend fun hasAcceptedActiveTerms(): Result<Boolean> {
        return try {
            val accepted = postgrest.rpc(function = "has_accepted_active_terms")
                .decodeAs<Boolean>()
            Result.success(accepted)
        } catch (e: Exception) {
            logger.e(LogCategory.Auth, TAG, "Failed to check terms acceptance", e)
            Result.failure(e)
        }
    }

    override suspend fun recordTermsAcceptance(
        appVersion: String?,
        buildNumber: String?,
        deviceInfo: String?,
    ): Result<Unit> {
        return try {
            postgrest.rpc(
                function = "record_terms_acceptance_v1",
                parameters = RecordTermsAcceptanceRpcParams(
                    p_app_version = appVersion,
                    p_build_number = buildNumber,
                    p_device_info = deviceInfo,
                ),
            )
            Result.success(Unit)
        } catch (e: Exception) {
            logger.e(LogCategory.Auth, TAG, "Failed to record terms acceptance", e)
            Result.failure(e)
        }
    }

    private companion object {
        const val TAG = "SupabaseTermsRepository"
    }
}
