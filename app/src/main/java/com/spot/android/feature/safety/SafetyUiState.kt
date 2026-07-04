package com.spot.android.feature.safety

import com.spot.android.data.model.Spot
import com.spot.android.data.model.enums.ReportReason
import com.spot.android.data.model.enums.ReportTargetType

data class SafetyUiState(
    val reportSheet: ReportSheetState? = null,
    val blockDialog: BlockDialogState? = null,
    val spotOverflowMenu: SpotOverflowMenuState? = null,
    val profileOverflowMenu: ProfileOverflowMenuState? = null,
    val isSubmitting: Boolean = false,
    val successToast: String? = null,
    val errorMessage: String? = null,
)

data class ReportSheetState(
    val targetType: ReportTargetType,
    val targetId: String,
    val reportedUserId: String,
    val reportedUsername: String?,
    val selectedReason: ReportReason? = null,
    val details: String = "",
    val blockRequested: Boolean = false,
)

data class BlockDialogState(
    val blockedUserId: String,
    val blockedUsername: String?,
    val sourceTargetType: ReportTargetType? = null,
    val sourceTargetId: String? = null,
)

data class SpotOverflowMenuState(
    val spot: Spot,
    val isOwner: Boolean,
)

data class ProfileOverflowMenuState(
    val userId: String,
    val username: String?,
)
