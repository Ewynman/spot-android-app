package com.spot.android.feature.collections

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spot.android.core.design.component.EmptyFeedView
import com.spot.android.core.design.component.SpotCard
import com.spot.android.core.design.component.Toast
import com.spot.android.core.design.component.ToastType
import com.spot.android.core.design.theme.SpotColors
import com.spot.android.feature.profile.ProfileExpandedSpotView
import com.spot.android.feature.profile.ProfileSpotGrid
import com.spot.android.navigation.OverlayHostViewModel

/**
 * Collection detail screen showing spots in a specific collection.
 * 
 * Reference: PRD/10-profile-social.md, PRD/12-pro-subscription.md
 */
@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CollectionDetailScreen(
    collectionId: String,
    onBack: () -> Unit,
    overlayViewModel: OverlayHostViewModel,
    modifier: Modifier = Modifier,
    viewModel: CollectionDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var toastMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.onFirstAppear()
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is CollectionsEffect.ShowToast -> toastMessage = effect.message
                is CollectionsEffect.NavigateToCollection -> { /* Not used here */ }
                is CollectionsEffect.ShowPaywall -> overlayViewModel.showPaywall(effect.entryPoint)
            }
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = false,
        onRefresh = viewModel::refresh,
    )

    val expandedSpot = uiState.expandedSpot
    if (expandedSpot != null) {
        ProfileExpandedSpotView(
            spot = expandedSpot,
            isOwnProfile = false,
            onBack = viewModel::onBackFromExpandedSpot,
            onLikeClick = { viewModel.toggleLike(expandedSpot) },
            onBookmarkClick = { viewModel.toggleBookmark(expandedSpot) },
            onOverflowClick = null,
            onDeleteClick = null,
            modifier = modifier,
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.collection?.name ?: "Collection") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::startEditingName) {
                        Icon(Icons.Filled.Edit, contentDescription = "Rename")
                    }
                },
            )
        },
        modifier = modifier.testTag("collection.detailRoot"),
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pullRefresh(pullRefreshState),
        ) {
            when (uiState.loadState) {
                CollectionDetailLoadState.LOADING -> EmptyFeedView(
                    title = "Loading...",
                    subtitle = "",
                    modifier = Modifier.fillMaxSize(),
                )
                CollectionDetailLoadState.EMPTY -> EmptyFeedView(
                    title = "No spots in this collection",
                    subtitle = "Bookmark spots and add them to this collection",
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("collection.empty"),
                )
                CollectionDetailLoadState.ERROR -> EmptyFeedView(
                    title = "Couldn't load spots",
                    subtitle = uiState.errorMessage ?: "Pull to refresh to try again",
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("collection.error"),
                )
                CollectionDetailLoadState.READY -> ProfileSpotGrid(
                    spots = uiState.spots,
                    isLoadingMore = uiState.isLoadingMore,
                    onSpotClick = viewModel::onSpotSelected,
                    onLoadMore = viewModel::loadMoreIfNeeded,
                    modifier = Modifier.fillMaxSize(),
                )
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

    if (uiState.isEditingName) {
        RenameCollectionDialog(
            currentName = uiState.collection?.name ?: "",
            onDismiss = viewModel::cancelEditingName,
            onRename = viewModel::updateCollectionName,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RenameCollectionDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename Collection") },
        text = {
            Column {
                Text("Enter a new name for your collection")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Collection name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onRename(name) },
                enabled = name.trim().isNotEmpty(),
            ) {
                Text("Rename")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        modifier = modifier,
    )
}
