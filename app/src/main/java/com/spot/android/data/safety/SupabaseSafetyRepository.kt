package com.spot.android.data.safety

import com.spot.android.core.logging.LogCategory
import com.spot.android.core.logging.SpotLogger
import com.spot.android.core.supabase.SupabaseClientProvider
import com.spot.android.data.dto.UserBriefRowDto
import com.spot.android.data.dto.UserBlockRowDto
import com.spot.android.data.mapper.UserMapper
import com.spot.android.data.model.User
import com.spot.android.data.model.enums.ReportReason
import com.spot.android.data.model.enums.ReportTargetType
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
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

    override suspend fun getBlockedUsers(): Result<List<User>> {
        return try {
            // Get blocked user IDs
            val blockRows = postgrest.from("user_blocks")
                .select(columns = Columns.list("blocked_user_id")) {
                    // RLS automatically filters to current user's blocks
                }
                .decodeList<UserBlockRowDto>()

            val blockedUserIds = blockRows.map { it.blocked_user_id }

            if (blockedUserIds.isEmpty()) {
                return Result.success(emptyList())
            }

            // Fetch user details from users_public
            val users = postgrest.from("users_public")
                .select {
                    filter {
                        isIn("user_id", blockedUserIds)
                    }
                }
                .decodeList<UserBriefRowDto>()
                .map { UserMapper.fromUserBriefRow(it) }

            logger.d(LogCategory.Privacy, TAG, "Loaded ${users.size} blocked users")
            Result.success(users)
        } catch (e: Exception) {
            logger.e(LogCategory.Privacy, TAG, "Failed to load blocked users", e)
            Result.failure(e)
        }
    }

    override suspend fun unblockUser(userId: String): Result<Unit> {
        return try {
            postgrest.from("user_blocks")
                .delete {
                    filter {
                        eq("blocked_user_id", userId)
                    }
                }
            logger.i(LogCategory.Privacy, TAG, "User unblocked: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.e(LogCategory.Privacy, TAG, "Failed to unblock user", e)
            Result.failure(e)
        }
    }

    private companion object {
        const val TAG = "SupabaseSafetyRepository"
    }
}
