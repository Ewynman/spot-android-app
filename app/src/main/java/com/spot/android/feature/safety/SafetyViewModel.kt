package com.spot.android.feature.safety

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spot.android.core.logging.LogCategory
import com.spot.android.core.logging.SpotLogger
import com.spot.android.core.supabase.SessionBridge
import com.spot.android.data.auth.UserSessionHolder
import com.spot.android.data.content.LocalContentRemovalBus
import com.spot.android.data.model.Spot
import com.spot.android.data.model.enums.ReportReason
import com.spot.android.data.model.enums.ReportTargetType
import com.spot.android.data.safety.SafetyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Coordinates report sheets, block dialogs, and overflow menus.
 *
 * Reference: PRD/13-moderation-safety.md, PRD/10-profile-social.md
 */
@HiltViewModel
class SafetyViewModel @Inject constructor(
    private val safetyRepository: SafetyRepository,
    private val userSessionHolder: UserSessionHolder,
    private val localContentRemovalBus: LocalContentRemovalBus,
    private val sessionBridge: SessionBridge,
    private val logger: SpotLogger,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SafetyUiState())
    val uiState: StateFlow<SafetyUiState> = _uiState.asStateFlow()

    fun openSpotOverflowMenu(spot: Spot) {
        val currentUserId = sessionBridge.currentUserId
        val isOwner = currentUserId != null && currentUserId == spot.userId
        _uiState.update {
            it.copy(
                spotOverflowMenu = SpotOverflowMenuState(spot = spot, isOwner = isOwner),
                errorMessage = null,
            )
        }
    }

    fun dismissSpotOverflowMenu() {
        _uiState.update { it.copy(spotOverflowMenu = null) }
    }

    fun openProfileOverflowMenu(userId: String, username: String?) {
        _uiState.update {
            it.copy(
                profileOverflowMenu = ProfileOverflowMenuState(userId = userId, username = username),
                errorMessage = null,
            )
        }
    }

    fun dismissProfileOverflowMenu() {
        _uiState.update { it.copy(profileOverflowMenu = null) }
    }

    fun openReportSpot(spot: Spot) {
        dismissSpotOverflowMenu()
        openReportSheet(
            targetType = ReportTargetType.SPOT,
            targetId = spot.id,
            reportedUserId = spot.userId,
            reportedUsername = spot.username,
        )
    }

    fun openReportUser(userId: String, username: String?) {
        dismissProfileOverflowMenu()
        openReportSheet(
            targetType = ReportTargetType.PROFILE,
            targetId = userId,
            reportedUserId = userId,
            reportedUsername = username,
        )
    }

    fun openBlockUserFromSpot(spot: Spot) {
        dismissSpotOverflowMenu()
        openBlockDialog(
            blockedUserId = spot.userId,
            blockedUsername = spot.username,
            sourceTargetType = ReportTargetType.SPOT,
            sourceTargetId = spot.id,
        )
    }

    fun openBlockUserFromProfile(userId: String, username: String?) {
        dismissProfileOverflowMenu()
        openBlockDialog(
            blockedUserId = userId,
            blockedUsername = username,
            sourceTargetType = ReportTargetType.PROFILE,
            sourceTargetId = userId,
        )
    }

    fun dismissReportSheet() {
        _uiState.update { it.copy(reportSheet = null, errorMessage = null) }
    }

    fun dismissBlockDialog() {
        _uiState.update { it.copy(blockDialog = null, errorMessage = null) }
    }

    fun selectReportReason(reason: ReportReason) {
        _uiState.update { state ->
            state.copy(
                reportSheet = state.reportSheet?.copy(selectedReason = reason),
                errorMessage = null,
            )
        }
    }

    fun updateReportDetails(details: String) {
        _uiState.update { state ->
            state.copy(reportSheet = state.reportSheet?.copy(details = details))
        }
    }

    fun toggleBlockRequested() {
        _uiState.update { state ->
            state.copy(
                reportSheet = state.reportSheet?.copy(
                    blockRequested = !(state.reportSheet?.blockRequested ?: false),
                ),
            )
        }
    }

    fun submitReport() {
        val sheet = _uiState.value.reportSheet ?: return
        val reason = sheet.selectedReason ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

            val result = safetyRepository.submitContentReport(
                targetType = sheet.targetType,
                targetId = sheet.targetId,
                reportedUserId = sheet.reportedUserId,
                reason = reason,
                details = sheet.details,
                blockRequested = sheet.blockRequested,
            )

            result.fold(
                onSuccess = {
                    applyLocalRemovalAfterReport(sheet)
                    if (sheet.blockRequested) {
                        applyLocalBlock(sheet.reportedUserId)
                    }
                    logger.i(LogCategory.Privacy, TAG, "Report submitted successfully")
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            reportSheet = null,
                            successToast = "Report submitted. Thank you.",
                        )
                    }
                },
                onFailure = {
                    logger.e(LogCategory.Privacy, TAG, "Report submission failed", it)
                    _uiState.update { state ->
                        state.copy(
                            isSubmitting = false,
                            errorMessage = "Couldn't submit report. Please try again.",
                        )
                    }
                },
            )
        }
    }

    fun confirmBlock() {
        val dialog = _uiState.value.blockDialog ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

            val result = safetyRepository.blockUser(
                blockedUserId = dialog.blockedUserId,
                sourceTargetType = dialog.sourceTargetType,
                sourceTargetId = dialog.sourceTargetId,
            )

            result.fold(
                onSuccess = {
                    applyLocalBlock(dialog.blockedUserId)
                    logger.i(LogCategory.Privacy, TAG, "User blocked successfully")
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            blockDialog = null,
                            successToast = "User blocked.",
                        )
                    }
                },
                onFailure = {
                    logger.e(LogCategory.Privacy, TAG, "Block failed", it)
                    _uiState.update { state ->
                        state.copy(
                            isSubmitting = false,
                            errorMessage = "Couldn't block user. Please try again.",
                        )
                    }
                },
            )
        }
    }

    fun clearSuccessToast() {
        _uiState.update { it.copy(successToast = null) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun openReportSheet(
        targetType: ReportTargetType,
        targetId: String,
        reportedUserId: String,
        reportedUsername: String?,
    ) {
        _uiState.update {
            it.copy(
                reportSheet = ReportSheetState(
                    targetType = targetType,
                    targetId = targetId,
                    reportedUserId = reportedUserId,
                    reportedUsername = reportedUsername,
                ),
                errorMessage = null,
            )
        }
    }

    private fun openBlockDialog(
        blockedUserId: String,
        blockedUsername: String?,
        sourceTargetType: ReportTargetType?,
        sourceTargetId: String?,
    ) {
        _uiState.update {
            it.copy(
                blockDialog = BlockDialogState(
                    blockedUserId = blockedUserId,
                    blockedUsername = blockedUsername,
                    sourceTargetType = sourceTargetType,
                    sourceTargetId = sourceTargetId,
                ),
                errorMessage = null,
            )
        }
    }

    private fun applyLocalRemovalAfterReport(sheet: ReportSheetState) {
        when (sheet.targetType) {
            ReportTargetType.SPOT -> localContentRemovalBus.removeBySpotId(sheet.targetId)
            ReportTargetType.PROFILE -> localContentRemovalBus.removeByAuthor(sheet.reportedUserId)
            else -> localContentRemovalBus.removeBySpotId(sheet.targetId)
        }
    }

    private fun applyLocalBlock(userId: String) {
        userSessionHolder.addBlockedUser(userId)
        localContentRemovalBus.removeByAuthor(userId)
    }

    private companion object {
        const val TAG = "SafetyViewModel"
    }
}
