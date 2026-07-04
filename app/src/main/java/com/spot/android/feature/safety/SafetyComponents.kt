package com.spot.android.feature.safety

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.spot.android.core.design.Dimensions
import com.spot.android.core.design.theme.SpotColors
import com.spot.android.core.design.theme.SpotTheme
import com.spot.android.data.model.enums.ReportReason
import com.spot.android.data.model.enums.ReportTargetType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportSheet(
    state: ReportSheetState,
    isSubmitting: Boolean,
    errorMessage: String?,
    onReasonSelected: (ReportReason) -> Unit,
    onDetailsChanged: (String) -> Unit,
    onBlockRequestedChanged: () -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val title = when (state.targetType) {
        ReportTargetType.PROFILE -> "Report User"
        else -> "Report Spot"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier.testTag("safety.reportSheet"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimensions.Padding.horizontal)
                .padding(bottom = Dimensions.Padding.verticalXL),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = SpotColors.Primary,
                modifier = Modifier.testTag("safety.reportSheet.title"),
            )

            state.reportedUsername?.let { username ->
                Spacer(modifier = Modifier.height(Dimensions.Spacing.small))
                Text(
                    text = "@$username",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SpotColors.Primary.copy(alpha = 0.7f),
                    modifier = Modifier.testTag("safety.reportSheet.username"),
                )
            }

            Spacer(modifier = Modifier.height(Dimensions.Spacing.large))

            Text(
                text = "Why are you reporting this?",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = SpotColors.Primary,
            )

            Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))

            ReportReason.entries.forEach { reason ->
                ReportReasonRow(
                    reason = reason,
                    selected = state.selectedReason == reason,
                    onSelected = { onReasonSelected(reason) },
                )
            }

            Spacer(modifier = Modifier.height(Dimensions.Spacing.large))

            OutlinedTextField(
                value = state.details,
                onValueChange = onDetailsChanged,
                label = { Text("Additional details (optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("safety.reportSheet.details"),
                minLines = 2,
                maxLines = 4,
            )

            Spacer(modifier = Modifier.height(Dimensions.Spacing.large))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("safety.reportSheet.blockToggle"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Also block this user",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SpotColors.Primary,
                    )
                    Text(
                        text = "Remove their content from your feed",
                        style = MaterialTheme.typography.bodySmall,
                        color = SpotColors.Primary.copy(alpha = 0.7f),
                    )
                }
                Switch(
                    checked = state.blockRequested,
                    onCheckedChange = { onBlockRequestedChanged() },
                    modifier = Modifier.semantics {
                        contentDescription = "Also block this user"
                    },
                )
            }

            errorMessage?.let { message ->
                Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag("safety.reportSheet.error"),
                )
            }

            Spacer(modifier = Modifier.height(Dimensions.Spacing.xl))

            Button(
                onClick = onSubmit,
                enabled = state.selectedReason != null && !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("safety.reportSheet.submit"),
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        strokeWidth = 2.dp,
                        color = SpotColors.ButtonText,
                    )
                } else {
                    Text("Submit Report")
                }
            }

            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("safety.reportSheet.cancel"),
            ) {
                Text("Cancel")
            }
        }
    }
}

@Composable
private fun ReportReasonRow(
    reason: ReportReason,
    selected: Boolean,
    onSelected: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onSelected,
            )
            .padding(vertical = Dimensions.Spacing.small)
            .testTag("safety.reportSheet.reason.${reason.value}"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelected,
        )
        Text(
            text = reason.displayName,
            style = MaterialTheme.typography.bodyMedium,
            color = SpotColors.Primary,
            modifier = Modifier.padding(start = Dimensions.Spacing.small),
        )
    }
}

@Composable
fun BlockUserDialog(
    state: BlockDialogState,
    isSubmitting: Boolean,
    errorMessage: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val displayName = state.blockedUsername?.let { "@$it" } ?: "this user"

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.testTag("safety.blockDialog"),
        title = {
            Text(
                text = "Block $displayName?",
                modifier = Modifier.testTag("safety.blockDialog.title"),
            )
        },
        text = {
            Column {
                Text(
                    text = "They won't be able to interact with you and their spots will be removed from your feed.",
                )
                errorMessage?.let { message ->
                    Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.testTag("safety.blockDialog.error"),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isSubmitting,
                modifier = Modifier.testTag("safety.blockDialog.confirm"),
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(16.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Block")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("safety.blockDialog.cancel"),
            ) {
                Text("Cancel")
            }
        },
    )
}

@Preview
@Composable
private fun BlockUserDialogPreview() {
    SpotTheme {
        BlockUserDialog(
            state = BlockDialogState(
                blockedUserId = "user-1",
                blockedUsername = "janedoe",
            ),
            isSubmitting = false,
            errorMessage = null,
            onConfirm = {},
            onDismiss = {},
        )
    }
}
