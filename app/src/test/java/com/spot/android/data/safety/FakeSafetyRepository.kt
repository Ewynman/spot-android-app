package com.spot.android.data.safety

import com.spot.android.data.model.User
import com.spot.android.data.model.enums.ReportReason
import com.spot.android.data.model.enums.ReportTargetType

class FakeSafetyRepository : SafetyRepository {

    var submitReportResult: Result<String> = Result.success("report-id")
    var blockUserResult: Result<String> = Result.success("block-id")

    var lastReportTargetType: ReportTargetType? = null
    var lastReportTargetId: String? = null
    var lastReportedUserId: String? = null
    var lastReportReason: ReportReason? = null
    var lastReportDetails: String? = null
    var lastBlockRequested: Boolean? = null

    var lastBlockedUserId: String? = null
    var lastBlockSourceTargetType: ReportTargetType? = null
    var lastBlockSourceTargetId: String? = null
    var lastBlockReason: String? = null

    var submitReportCallCount = 0
    var blockUserCallCount = 0

    override suspend fun submitContentReport(
        targetType: ReportTargetType,
        targetId: String,
        reportedUserId: String,
        reason: ReportReason,
        details: String,
        blockRequested: Boolean,
    ): Result<String> {
        submitReportCallCount++
        lastReportTargetType = targetType
        lastReportTargetId = targetId
        lastReportedUserId = reportedUserId
        lastReportReason = reason
        lastReportDetails = details
        lastBlockRequested = blockRequested
        return submitReportResult
    }

    override suspend fun blockUser(
        blockedUserId: String,
        sourceTargetType: ReportTargetType?,
        sourceTargetId: String?,
        reason: String?,
    ): Result<String> {
        blockUserCallCount++
        lastBlockedUserId = blockedUserId
        lastBlockSourceTargetType = sourceTargetType
        lastBlockSourceTargetId = sourceTargetId
        lastBlockReason = reason
        return blockUserResult
    }

    override suspend fun getBlockedUsers(): Result<List<User>> {
        return Result.success(emptyList())
    }

    override suspend fun unblockUser(userId: String): Result<Unit> {
        return Result.success(Unit)
    }
}
