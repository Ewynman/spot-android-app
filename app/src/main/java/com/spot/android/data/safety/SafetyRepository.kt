package com.spot.android.data.safety

import com.spot.android.data.model.UserBrief
import com.spot.android.data.model.enums.ReportReason
import com.spot.android.data.model.enums.ReportTargetType

/**
 * Safety operations: content reports and user blocks.
 *
 * Reference: PRD/04-backend-api.md, PRD/13-moderation-safety.md
 */
interface SafetyRepository {

    suspend fun submitContentReport(
        targetType: ReportTargetType,
        targetId: String,
        reportedUserId: String,
        reason: ReportReason,
        details: String,
        blockRequested: Boolean,
    ): Result<String>

    suspend fun blockUser(
        blockedUserId: String,
        sourceTargetType: ReportTargetType? = null,
        sourceTargetId: String? = null,
        reason: String? = null,
    ): Result<String>

    suspend fun getBlockedUsers(): Result<List<UserBrief>>

    suspend fun unblockUser(userId: String): Result<Unit>
}
