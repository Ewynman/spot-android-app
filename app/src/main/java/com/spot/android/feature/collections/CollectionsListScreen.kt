package com.spot.android.feature.collections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import com.spot.android.navigation.OverlayHostViewModel

/**
 * Collections list screen (Pro feature).
 * 
 * Shows all bookmark collections and allows creating/deleting them.
 * 
 * Reference: PRD/10-profile-social.md, PRD/12-pro-subscription.md
 */
@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CollectionsListScreen(
    onBack: () -> Unit,
    onCollectionClick: (String) -> Unit,
    overlayViewModel: OverlayHostViewModel,
    modifier: Modifier = Modifier,
    viewModel: CollectionsListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }
    var collectionToDelete by remember { mutableStateOf<BookmarkCollection?>(null) }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.onFirstAppear()
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is CollectionsEffect.ShowToast -> toastMessage = effect.message
                is CollectionsEffect.NavigateToCollection -> onCollectionClick(effect.collectionId)
                is CollectionsEffect.ShowPaywall -> overlayViewModel.showPaywall(effect.entryPoint)
            }
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = false,
        onRefresh = viewModel::refresh,
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Collections") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = SpotColors.Accent,
                modifier = Modifier.testTag("collections.createButton"),
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Create collection")
            }
        },
        modifier = modifier.testTag("collections.listRoot"),
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pullRefresh(pullRefreshState),
        ) {
            when (uiState.loadState) {
                CollectionsLoadState.LOADING -> EmptyFeedView(
                    title = "Loading collections...",
                    subtitle = "",
                    modifier = Modifier.fillMaxSize(),
                )
                CollectionsLoadState.EMPTY -> EmptyFeedView(
                    title = "No collections yet",
                    subtitle = "Tap + to create your first collection",
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("collections.empty"),
                )
                CollectionsLoadState.ERROR -> EmptyFeedView(
                    title = "Couldn't load collections",
                    subtitle = uiState.errorMessage ?: "Pull to refresh to try again",
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("collections.error"),
                )
                CollectionsLoadState.READY -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("collections.list"),
                    contentPadding = Dimensions.Spacing.contentPadding,
                    verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small),
                ) {
                    items(uiState.collections, key = { it.id }) { collection ->
                        CollectionListItem(
                            collection = collection,
                            onClick = { viewModel.onCollectionSelected(collection.id) },
                            onDelete = { collectionToDelete = collection },
                            modifier = Modifier.testTag("collections.item.${collection.id}"),
                        )
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = false,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = MaterialTheme.colorScheme.surface,
                contentColor = SpotColors.Accent,
            )
        }
    }

    if (showCreateDialog) {
        CreateCollectionDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name ->
                viewModel.createCollection(name)
                showCreateDialog = false
            },
            isCreating = uiState.isCreatingCollection,
        )
    }

    collectionToDelete?.let { collection ->
        AlertDialog(
            onDismissRequest = { collectionToDelete = null },
            title = { Text("Delete Collection") },
            text = { Text("Are you sure you want to delete \"${collection.name}\"? All spots will be removed from this collection.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCollection(collection.id)
                        collectionToDelete = null
                    },
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { collectionToDelete = null }) {
                    Text("Cancel")
                }
            },
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
private fun CollectionListItem(
    collection: BookmarkCollection,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.Spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = collection.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${collection.spotCount} spots",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Menu")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateCollectionDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit,
    isCreating: Boolean,
    modifier: Modifier = Modifier,
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("collections.createDialog.nameField"),
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
        modifier = modifier.testTag("collections.createDialog"),
    )
}
