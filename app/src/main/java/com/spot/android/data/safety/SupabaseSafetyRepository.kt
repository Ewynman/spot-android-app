package com.spot.android.data.safety

import com.spot.android.core.logging.LogCategory
import com.spot.android.core.logging.SpotLogger
import com.spot.android.core.supabase.SupabaseClientProvider
import com.spot.android.data.model.enums.ReportReason
import com.spot.android.data.model.enums.ReportTargetType
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Supabase-backed safety repository.
 */
@Singleton
class SupabaseSafetyRepository @Inject constructor(
    private val supabaseProvider: SupabaseClientProvider,
    private val logger: SpotLogger,
) : SafetyRepository {

    private val postgrest get() = supabaseProvider.client.postgrest

    override suspend fun submitContentReport(
        targetType: ReportTargetType,
        targetId: String,
        reportedUserId: String,
        reason: ReportReason,
        details: String,
        blockRequested: Boolean,
    ): Result<String> {
        return try {
            val reportId = postgrest.rpc(
                function = "submit_content_report",
                parameters = SubmitContentReportRpcParams(
                    p_target_type = targetType.value,
                    p_target_id = targetId,
                    p_reported_user_id = reportedUserId,
                    p_reason = reason.value,
                    p_details = details.trim(),
                    p_block_requested = blockRequested,
                ),
            ).decodeAs<String>()
            logger.i(LogCategory.Privacy, TAG, "Content report submitted")
            Result.success(reportId)
        } catch (e: Exception) {
            logger.e(LogCategory.Privacy, TAG, "Failed to submit content report", e)
            Result.failure(e)
        }
    }

    override suspend fun blockUser(
        blockedUserId: String,
        sourceTargetType: ReportTargetType?,
        sourceTargetId: String?,
        reason: String?,
    ): Result<String> {
        return try {
            val blockId = postgrest.rpc(
                function = "block_user_v1",
                parameters = BlockUserRpcParams(
                    p_blocked_user_id = blockedUserId,
                    p_source_target_type = sourceTargetType?.value,
                    p_source_target_id = sourceTargetId,
                    p_reason = reason,
                ),
            ).decodeAs<String>()
            logger.i(LogCategory.Privacy, TAG, "User blocked")
            Result.success(blockId)
        } catch (e: Exception) {
            logger.e(LogCategory.Privacy, TAG, "Failed to block user", e)
            Result.failure(e)
        }
    }

    private companion object {
        const val TAG = "SupabaseSafetyRepository"
    }
}
