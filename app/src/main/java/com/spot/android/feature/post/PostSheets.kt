package com.spot.android.feature.post

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.spot.android.core.design.Dimensions
import com.spot.android.core.design.theme.SpotColors
import com.spot.android.data.post.PostDraftSummary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDraftsSheet(
    drafts: List<PostDraftSummary>,
    onLoadDraft: (String) -> Unit,
    onDeleteDraft: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier.testTag("post.draftsSheet"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimensions.Padding.horizontal)
                .padding(bottom = Dimensions.Padding.verticalLarge),
        ) {
            Text(
                text = "Drafts",
                color = SpotColors.Primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.testTag("post.draftsTitle"),
            )
            Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))

            if (drafts.isEmpty()) {
                Text(
                    text = "No saved drafts yet",
                    color = SpotColors.Primary,
                    modifier = Modifier.testTag("post.draftsEmpty"),
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small),
                ) {
                    items(drafts, key = { it.id }) { draft ->
                        DraftRow(
                            draft = draft,
                            onLoad = { onLoadDraft(draft.id) },
                            onDelete = { onDeleteDraft(draft.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DraftRow(
    draft: PostDraftSummary,
    onLoad: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("post.draftRow.${draft.id}"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = draft.placeName ?: "Untitled draft",
                color = SpotColors.Primary,
                fontWeight = FontWeight.Medium,
            )
            if (draft.vibeTags.isNotEmpty()) {
                Text(
                    text = draft.vibeTags.joinToString(", "),
                    color = SpotColors.Primary,
                )
            }
        }
        TextButton(onClick = onLoad) {
            Text("Open", color = SpotColors.Primary)
        }
        TextButton(onClick = onDelete) {
            Text("Delete", color = SpotColors.Primary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostingRulesSheet(
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier.testTag("post.postingRulesSheet"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimensions.Padding.horizontal)
                .padding(bottom = Dimensions.Padding.verticalLarge),
        ) {
            Text(
                text = "Posting Rules",
                color = SpotColors.Primary,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))
            Text(
                text = "Share real places you love. Only post photos you took or have rights to use. " +
                    "No nudity, harassment, spam, or illegal content. Reports are reviewed within 24 hours.",
                color = SpotColors.Primary,
            )
            Spacer(modifier = Modifier.height(Dimensions.Spacing.large))
            PostPrimaryButton(
                text = "I understand",
                enabled = true,
                onClick = onAccept,
                modifier = Modifier.testTag("post.postingRulesAccept"),
            )
        }
    }
}

@Composable
fun SaveDraftDialog(
    name: String,
    onNameChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save draft") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Draft name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("post.saveDraftName"),
            )
        },
        confirmButton = {
            TextButton(
                onClick = onSave,
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("post.saveDraftConfirm"),
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        modifier = Modifier.testTag("post.saveDraftDialog"),
    )
}

@Composable
fun PostEmailVerificationGate(
    onVerifyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Dimensions.Padding.horizontal)
            .testTag("post.emailNotVerified"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Verify your email to post",
            color = SpotColors.Primary,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(Dimensions.Spacing.large))
        PostPrimaryButton(
            text = "Verify email",
            enabled = true,
            onClick = onVerifyClick,
            modifier = Modifier.testTag("post.verifyEmailButton"),
        )
    }
}

@Composable
fun PostLoadingGate(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("post.checkingEmail"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(color = SpotColors.Primary)
    }
}
