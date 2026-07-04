package com.spot.android.feature.safety

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spot.android.core.design.component.Toast
import com.spot.android.core.design.component.ToastType
import com.spot.android.data.model.Spot

/**
 * Actions exposed to feature screens for safety flows.
 */
interface SafetyActions {
    fun openSpotOverflowMenu(spot: Spot)
    fun openProfileOverflowMenu(userId: String, username: String?)
}

val LocalSafetyActions = staticCompositionLocalOf<SafetyActions?> { null }

/**
 * Host composable for report sheets, block dialogs, and overflow menus.
 *
 * Wrap app content with this to enable safety flows from any screen.
 *
 * Reference: PRD/13-moderation-safety.md
 */
@Composable
fun SafetyFlowHost(
    modifier: Modifier = Modifier,
    viewModel: SafetyViewModel = hiltViewModel(),
    content: @Composable () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val actions = object : SafetyActions {
        override fun openSpotOverflowMenu(spot: Spot) = viewModel.openSpotOverflowMenu(spot)
        override fun openProfileOverflowMenu(userId: String, username: String?) =
            viewModel.openProfileOverflowMenu(userId, username)
    }

    CompositionLocalProvider(LocalSafetyActions provides actions) {
        content()
    }

    uiState.reportSheet?.let { sheet ->
        ReportSheet(
            state = sheet,
            isSubmitting = uiState.isSubmitting,
            errorMessage = uiState.errorMessage,
            onReasonSelected = viewModel::selectReportReason,
            onDetailsChanged = viewModel::updateReportDetails,
            onBlockRequestedChanged = viewModel::toggleBlockRequested,
            onSubmit = viewModel::submitReport,
            onDismiss = viewModel::dismissReportSheet,
        )
    }

    uiState.blockDialog?.let { dialog ->
        BlockUserDialog(
            state = dialog,
            isSubmitting = uiState.isSubmitting,
            errorMessage = uiState.errorMessage,
            onConfirm = viewModel::confirmBlock,
            onDismiss = viewModel::dismissBlockDialog,
        )
    }

    uiState.spotOverflowMenu?.let { menu ->
        if (!menu.isOwner) {
            SafetyOverflowDialog(
                title = "Spot options",
                modifier = Modifier.testTag("safety.spotOverflowMenu"),
                actions = listOf(
                    OverflowAction(
                        label = "Report",
                        testTag = "safety.spotOverflowMenu.report",
                        onClick = { viewModel.openReportSpot(menu.spot) },
                    ),
                    OverflowAction(
                        label = "Block User",
                        testTag = "safety.spotOverflowMenu.block",
                        onClick = { viewModel.openBlockUserFromSpot(menu.spot) },
                    ),
                ),
                onDismiss = viewModel::dismissSpotOverflowMenu,
            )
        }
    }

    uiState.profileOverflowMenu?.let { menu ->
        SafetyOverflowDialog(
            title = "Profile options",
            modifier = Modifier.testTag("safety.profileOverflowMenu"),
            actions = listOf(
                OverflowAction(
                    label = "Report User",
                    testTag = "safety.profileOverflowMenu.report",
                    onClick = { viewModel.openReportUser(menu.userId, menu.username) },
                ),
                OverflowAction(
                    label = "Block User",
                    testTag = "safety.profileOverflowMenu.block",
                    onClick = { viewModel.openBlockUserFromProfile(menu.userId, menu.username) },
                ),
            ),
            onDismiss = viewModel::dismissProfileOverflowMenu,
        )
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        uiState.successToast?.let { message ->
            Toast(
                message = message,
                type = ToastType.SUCCESS,
                visible = true,
                onDismiss = viewModel::clearSuccessToast,
                modifier = Modifier
                    .padding(bottom = 96.dp)
                    .testTag("safety.successToast"),
            )
        }
    }
}

private data class OverflowAction(
    val label: String,
    val testTag: String,
    val onClick: () -> Unit,
)

@Composable
private fun SafetyOverflowDialog(
    title: String,
    actions: List<OverflowAction>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = { Text(title) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                actions.forEach { action ->
                    TextButton(
                        onClick = action.onClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(action.testTag),
                    ) {
                        Text(action.label)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("safety.overflowMenu.cancel"),
            ) {
                Text("Cancel")
            }
        },
    )
}
