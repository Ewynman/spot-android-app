package com.spot.android.feature.collections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spot.android.core.design.Dimensions
import com.spot.android.core.design.component.EmptyFeedView
import com.spot.android.core.design.component.Toast
import com.spot.android.core.design.component.ToastType
import com.spot.android.core.design.theme.SpotColors
import com.spot.android.data.model.BookmarkCollection

/**
 * Collection picker sheet for adding a bookmarked spot to collections.
 * 
 * Pro feature that appears when bookmarking a spot.
 * 
 * Reference: PRD/10-profile-social.md, PRD/12-pro-subscription.md
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionPickerSheet(
    spotId: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CollectionPickerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showCreateDialog by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(spotId) {
        viewModel.configure(spotId)
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is CollectionsEffect.ShowToast -> toastMessage = effect.message
                is CollectionsEffect.NavigateToCollection -> { /* Not used here */ }
                is CollectionsEffect.ShowPaywall -> { /* Not used here */ }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier.testTag("collections.pickerSheet"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimensions.Padding.horizontal)
                .padding(bottom = Dimensions.Padding.verticalLarge),
        ) {
            Text(
                text = "Add to Collection",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = SpotColors.Primary,
            )
            Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))

            when (uiState.loadState) {
                CollectionPickerLoadState.LOADING -> EmptyFeedView(
                    title = "Loading...",
                    subtitle = "",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                )
                CollectionPickerLoadState.EMPTY -> {
                    EmptyFeedView(
                        title = "No collections yet",
                        subtitle = "Create your first collection below",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                    )
                }
                CollectionPickerLoadState.ERROR -> EmptyFeedView(
                    title = "Couldn't load collections",
                    subtitle = uiState.errorMessage ?: "",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                )
                CollectionPickerLoadState.READY -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .testTag("collections.pickerList"),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        items(uiState.collections, key = { it.id }) { collection ->
                            CollectionPickerItem(
                                collection = collection,
                                isSelected = uiState.selectedCollectionIds.contains(collection.id),
                                onToggle = { viewModel.toggleCollection(collection.id, spotId) },
                                modifier = Modifier.testTag("collections.pickerItem.${collection.id}"),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))

            Button(
                onClick = { showCreateDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("collections.pickerCreateButton"),
            ) {
                Text("Create New Collection")
            }

            Spacer(modifier = Modifier.height(Dimensions.Spacing.small))

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Done")
            }
        }
    }

    if (showCreateDialog) {
        CreateCollectionInPickerDialog(
            spotId = spotId,
            onDismiss = { showCreateDialog = false },
            onCreate = { name ->
                viewModel.createCollection(name, spotId)
                showCreateDialog = false
            },
            isCreating = uiState.isCreatingCollection,
        )
    }

    toastMessage?.let { message ->
        Toast(
            message = message,
            type = ToastType.INFO,
            onDismiss = { toastMessage = null },
        )
    }
}

@Composable
private fun CollectionPickerItem(
    collection: BookmarkCollection,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggle() },
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
        ) {
            Text(
                text = collection.name,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = "${collection.spotCount} spots",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateCollectionInPickerDialog(
    spotId: String,
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit,
    isCreating: Boolean,
    modifier: Modifier = Modifier,
) {
    var name by remember { mutableStateOf("") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Collection") },
        text = {
            Column {
                Text("Enter a name for your collection")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Collection name") },
                    enabled = !isCreating,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(name) },
                enabled = name.trim().isNotEmpty() && !isCreating,
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isCreating) {
                Text("Cancel")
            }
        },
        modifier = modifier,
    )
}
