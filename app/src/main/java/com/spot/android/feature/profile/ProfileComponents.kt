package com.spot.android.feature.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.spot.android.core.design.Dimensions
import com.spot.android.core.design.component.Avatar
import com.spot.android.core.design.component.EmptyFeedView
import com.spot.android.core.design.component.SkeletonSpotCard
import com.spot.android.core.design.component.SpotCard
import com.spot.android.core.design.component.TopNavigationView
import com.spot.android.core.design.theme.SpotColors
import com.spot.android.data.model.FollowRelationship
import com.spot.android.data.model.FollowRequest
import com.spot.android.data.model.Spot
import com.spot.android.data.model.User
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spot.android.feature.map.ProfileMapView
import com.spot.android.feature.safety.LocalSafetyActions
import com.spot.android.navigation.OverlayHostViewModel

@Composable
fun ProfileSpotGrid(
    spots: List<Spot>,
    isLoadingMore: Boolean,
    onSpotClick: (Spot) -> Unit,
    onLoadMore: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val gridState = rememberLazyGridState()
    val lastVisibleIndex by remember(spots.size) {
        derivedStateOf {
            gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        }
    }

    LaunchedEffect(lastVisibleIndex) {
        onLoadMore(lastVisibleIndex)
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = gridState,
        modifier = modifier
            .fillMaxSize()
            .testTag("profile.spotGrid"),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(spots, key = { it.id }) { spot ->
            ProfileGridCover(
                spot = spot,
                onClick = { onSpotClick(spot) },
            )
        }
        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = SpotColors.Accent,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileGridCover(
    spot: Spot,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val aspectRatio = spot.mediaDisplayAspectRatio.takeIf { it > 0.0 }?.toFloat() ?: 1f
    Box(
        modifier = modifier
            .aspectRatio(aspectRatio.coerceIn(0.75f, 1.5f))
            .clip(RoundedCornerShape(Dimensions.Radius.medium))
            .clickable(onClick = onClick)
            .testTag("profile.gridSpot.${spot.id}"),
    ) {
        AsyncImage(
            model = spot.thumbnailURL ?: spot.imageURL,
            contentDescription = spot.caption,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
    }
}

@Composable
fun ProfileHeader(
    user: User,
    followRelationship: FollowRelationship,
    isFollowActionInProgress: Boolean,
    onFollowClick: () -> Unit,
    onOverflowClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("profile.header"),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Avatar(
                imageUrl = user.profileImageURL,
                isPro = user.isPro,
                contentDescription = "${user.username} avatar",
                modifier = Modifier
                    .size(100.dp)
                    .testTag("profile.avatar"),
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = SpotColors.Primary,
                    modifier = Modifier.testTag("profile.username"),
                )
                Text(
                    text = "${user.spotsCount} spots shared",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SpotColors.Primary.copy(alpha = 0.7f),
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .testTag("profile.spotsCount"),
                )
            }

            IconButton(
                onClick = onOverflowClick,
                modifier = Modifier.testTag("profile.overflow"),
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Profile options",
                    tint = SpotColors.Primary,
                )
            }
        }

        if (!user.isCurrentUser) {
            FollowButton(
                relationship = followRelationship,
                isLoading = isFollowActionInProgress,
                onClick = onFollowClick,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .testTag("profile.followButton"),
            )
        }
    }
}

@Composable
private fun FollowButton(
    relationship: FollowRelationship,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (label, enabled) = when (relationship) {
        FollowRelationship.NotFollowing -> "Follow" to true
        FollowRelationship.Following,
        FollowRelationship.FollowingPrivate,
        -> "Unfollow" to true
        FollowRelationship.CanRequest -> "Request to Follow" to true
        FollowRelationship.Requested -> "Requested" to false
        FollowRelationship.Self -> return
    }

    if (relationship == FollowRelationship.Following ||
        relationship == FollowRelationship.FollowingPrivate
    ) {
        OutlinedButton(
            onClick = onClick,
            enabled = !isLoading,
            modifier = modifier,
        ) {
            Text(label)
        }
    } else {
        Button(
            onClick = onClick,
            enabled = enabled && !isLoading,
            modifier = modifier,
            colors = ButtonDefaults.buttonColors(
                containerColor = SpotColors.Accent,
                contentColor = SpotColors.Primary,
            ),
        ) {
            Text(label)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTabControl(
    selectedTab: ProfileTab,
    onTabSelected: (ProfileTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    SingleChoiceSegmentedButtonRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("profile.tabControl"),
    ) {
        ProfileTab.entries.forEachIndexed { index, tab ->
            SegmentedButton(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = ProfileTab.entries.size,
                ),
                modifier = Modifier.testTag("profile.tab.${tab.name.lowercase()}"),
            ) {
                Text(
                    text = when (tab) {
                        ProfileTab.Spots -> "Spots"
                        ProfileTab.Map -> "Map"
                    },
                )
            }
        }
    }
}

@Composable
fun ProfileOverflowMenu(
    expanded: Boolean,
    isOwnProfile: Boolean,
    isPro: Boolean,
    isPrivate: Boolean,
    pendingFollowRequestCount: Int,
    onDismiss: () -> Unit,
    onAction: (ProfileOverflowAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = modifier.testTag("profile.overflowMenu"),
    ) {
        if (isOwnProfile) {
            if (!isPro) {
                DropdownMenuItem(
                    text = { Text("Go Pro") },
                    onClick = { onAction(ProfileOverflowAction.GoPro) },
                    modifier = Modifier.testTag("profile.menu.goPro"),
                )
            }
            DropdownMenuItem(
                text = { Text("Your Likes") },
                onClick = { onAction(ProfileOverflowAction.YourLikes) },
                modifier = Modifier.testTag("profile.menu.likes"),
            )
            DropdownMenuItem(
                text = { Text("Your Bookmarks") },
                onClick = { onAction(ProfileOverflowAction.YourBookmarks) },
                modifier = Modifier.testTag("profile.menu.bookmarks"),
            )
            if (isPrivate) {
                DropdownMenuItem(
                    text = {
                        Text(
                            if (pendingFollowRequestCount > 0) {
                                "Follow Requests ($pendingFollowRequestCount)"
                            } else {
                                "Follow Requests"
                            },
                        )
                    },
                    onClick = { onAction(ProfileOverflowAction.FollowRequests) },
                    modifier = Modifier.testTag("profile.menu.followRequests"),
                )
            }
            DropdownMenuItem(
                text = { Text("Settings") },
                onClick = { onAction(ProfileOverflowAction.Settings) },
                modifier = Modifier.testTag("profile.menu.settings"),
            )
        } else {
            Unit
        }
    }
}

@Composable
fun ProfileExpandedSpotView(
    spot: Spot,
    isOwnProfile: Boolean,
    onBack: () -> Unit,
    onLikeClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onOverflowClick: (() -> Unit)?,
    onDeleteClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile.expandedSpot"),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = onBack,
                modifier = Modifier.testTag("profile.expandedSpotBack"),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = SpotColors.Primary,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Back to grid", color = SpotColors.Primary)
            }
            if (isOwnProfile && onDeleteClick != null) {
                Spacer(modifier = Modifier.weight(1f))
                TextButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.testTag("profile.deleteSpot"),
                ) {
                    Text("Delete", color = SpotColors.Primary)
                }
            }
        }
        SpotCard(
            spot = spot,
            onLikeClick = onLikeClick,
            onBookmarkClick = onBookmarkClick,
            onMoreClick = onOverflowClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun ProfileDeleteSpotDialog(
    visible: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete spot?") },
        text = { Text("This can't be undone.") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                modifier = Modifier.testTag("profile.deleteConfirm"),
            ) {
                Text("Delete", color = SpotColors.Primary)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("profile.deleteCancel"),
            ) {
                Text("Cancel")
            }
        },
        modifier = Modifier.testTag("profile.deleteDialog"),
    )
}

@Composable
fun ProfileLoadingContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile.loading"),
    ) {
        repeat(2) { SkeletonSpotCard() }
    }
}

@Composable
fun ProfileEmptySpotsView(modifier: Modifier = Modifier) {
    EmptyFeedView(
        title = "No spots yet",
        subtitle = "Shared spots will appear here",
        modifier = modifier
            .fillMaxSize()
            .testTag("profile.emptySpots"),
    )
}

@Composable
fun FollowRequestRow(
    request: FollowRequest,
    isActionInProgress: Boolean,
    onAccept: () -> Unit,
    onDeny: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("profile.followRequest.${request.id}"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(
            imageUrl = request.requester.profileImageURL,
            isPro = request.requester.isPro,
            contentDescription = "${request.requester.username} avatar",
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = request.requester.username,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = SpotColors.Primary,
        )
        TextButton(
            onClick = onDeny,
            enabled = !isActionInProgress,
            modifier = Modifier.testTag("profile.followRequestDeny.${request.id}"),
        ) {
            Text("Deny")
        }
        Button(
            onClick = onAccept,
            enabled = !isActionInProgress,
            colors = ButtonDefaults.buttonColors(
                containerColor = SpotColors.Accent,
                contentColor = SpotColors.Primary,
            ),
            modifier = Modifier.testTag("profile.followRequestAccept.${request.id}"),
        ) {
            Text("Accept")
        }
    }
}

@Composable
fun ProfileSubScreenScaffold(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("profile.subScreenBack"),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = SpotColors.Primary,
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = SpotColors.Primary,
                    modifier = Modifier.testTag("profile.subScreenTitle"),
                )
            }
        },
        content = content,
    )
}

@Composable
fun ProfileContentHost(
    userId: String?,
    showBackButton: Boolean,
    onBack: () -> Unit,
    overlayViewModel: OverlayHostViewModel,
    profileNavigationBus: com.spot.android.navigation.ProfileNavigationBus? = null,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(key = userId ?: "own_profile"),
    followRequestsViewModel: FollowRequestsViewModel = hiltViewModel(
        key = "${userId ?: "own"}_follow_requests",
    ),
    engagementViewModel: ProfileEngagementViewModel = hiltViewModel(
        key = "${userId ?: "own"}_engagement",
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val safetyActions = LocalSafetyActions.current

    LaunchedEffect(userId) {
        viewModel.configure(userId)
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ProfileEffect.ShowPaywall -> overlayViewModel.showPaywall(entryPoint = effect.entryPoint)
                ProfileEffect.OpenSettings -> Unit
            }
        }
    }

    LaunchedEffect(uiState.user?.isPrivate, userId) {
        val isOwnPrivateProfile = userId == null && uiState.user?.isPrivate == true
        viewModel.onBadgePollingVisible(isOwnPrivateProfile && uiState.mode == ProfileScreenMode.Main)
    }

    when (uiState.mode) {
        ProfileScreenMode.Main,
        ProfileScreenMode.ExpandedSpot,
        -> ProfileMainContent(
            uiState = uiState,
            showBackButton = showBackButton,
            onBack = onBack,
            onTabSelected = viewModel::onTabSelected,
            onSpotSelected = viewModel::onSpotSelected,
            onLoadMoreSpots = viewModel::loadMoreSpotsIfNeeded,
            onFollowClick = viewModel::onFollowButtonClick,
            onOverflowClick = {
                val user = uiState.user
                if (user != null && !user.isCurrentUser) {
                    safetyActions?.openProfileOverflowMenu(user.id, user.username)
                } else {
                    viewModel.onOverflowClick()
                }
            },
            onOverflowAction = viewModel::onOverflowAction,
            onDismissOverflow = viewModel::dismissOverflowMenu,
            onBackFromExpandedSpot = viewModel::onBackFromExpandedSpot,
            onLikeClick = viewModel::toggleLike,
            onBookmarkClick = viewModel::toggleBookmark,
            onSpotOverflowClick = { spot -> safetyActions?.openSpotOverflowMenu(spot) },
            onDeleteSpotClick = viewModel::requestDeleteSpot,
            profileNavigationBus = profileNavigationBus,
            modifier = modifier,
        )

        ProfileScreenMode.FollowRequests -> FollowRequestsScreen(
            viewModel = followRequestsViewModel,
            onBack = viewModel::onBackFromSubScreen,
            modifier = modifier,
        )

        ProfileScreenMode.Likes -> ProfileEngagementScreen(
            kind = ProfileEngagementKind.Likes,
            viewModel = engagementViewModel,
            overlayViewModel = overlayViewModel,
            onBack = viewModel::onBackFromSubScreen,
            modifier = modifier,
        )

        ProfileScreenMode.Bookmarks -> ProfileEngagementScreen(
            kind = ProfileEngagementKind.Bookmarks,
            viewModel = engagementViewModel,
            overlayViewModel = overlayViewModel,
            onBack = viewModel::onBackFromSubScreen,
            modifier = modifier,
        )

        ProfileScreenMode.Collections -> com.spot.android.feature.collections.CollectionsListScreen(
            onBack = viewModel::onBackFromSubScreen,
            onCollectionClick = viewModel::onCollectionSelected,
            overlayViewModel = overlayViewModel,
            modifier = modifier,
        )

        ProfileScreenMode.CollectionDetail -> {
            val collectionId = uiState.selectedCollectionId
            if (collectionId != null) {
                com.spot.android.feature.collections.CollectionDetailScreen(
                    collectionId = collectionId,
                    onBack = viewModel::onBackFromCollectionDetail,
                    overlayViewModel = overlayViewModel,
                    modifier = modifier,
                )
            }
        }

        ProfileScreenMode.Settings -> com.spot.android.feature.settings.SettingsNavigationHost(
            onNavigateBack = viewModel::onBackFromSubScreen,
            onNavigateToWelcome = { /* TODO: handle sign out navigation */ },
            onShowPaywall = { overlayViewModel.showPaywall(entryPoint = "settings") },
            modifier = modifier,
        )
    }

    ProfileDeleteSpotDialog(
        visible = uiState.showDeleteSpotDialog,
        onConfirm = viewModel::confirmDeleteSpot,
        onDismiss = viewModel::dismissDeleteSpotDialog,
    )
}

@Composable
private fun ProfileMainContent(
    uiState: ProfileUiState,
    showBackButton: Boolean,
    onBack: () -> Unit,
    onTabSelected: (ProfileTab) -> Unit,
    onSpotSelected: (Spot) -> Unit,
    onLoadMoreSpots: (Int) -> Unit,
    onFollowClick: () -> Unit,
    onOverflowClick: () -> Unit,
    onOverflowAction: (ProfileOverflowAction) -> Unit,
    onDismissOverflow: () -> Unit,
    onBackFromExpandedSpot: () -> Unit,
    onLikeClick: (Spot) -> Unit,
    onBookmarkClick: (Spot) -> Unit,
    onSpotOverflowClick: (Spot) -> Unit,
    onDeleteSpotClick: (Spot) -> Unit,
    profileNavigationBus: com.spot.android.navigation.ProfileNavigationBus? = null,
    modifier: Modifier = Modifier,
) {
    if (uiState.mode == ProfileScreenMode.ExpandedSpot && uiState.expandedSpot != null) {
        ProfileExpandedSpotView(
            spot = uiState.expandedSpot,
            isOwnProfile = uiState.user?.isCurrentUser == true,
            onBack = onBackFromExpandedSpot,
            onLikeClick = { onLikeClick(uiState.expandedSpot) },
            onBookmarkClick = { onBookmarkClick(uiState.expandedSpot) },
            onOverflowClick = { onSpotOverflowClick(uiState.expandedSpot) },
            onDeleteClick = if (uiState.user?.isCurrentUser == true) {
                { onDeleteSpotClick(uiState.expandedSpot) }
            } else {
                null
            },
            modifier = modifier.padding(horizontal = Dimensions.Padding.horizontal),
        )
        return
    }

    Scaffold(
        modifier = modifier.testTag("profile.profileRoot"),
        topBar = {
            TopNavigationView(
                showBackButton = showBackButton,
                onBackClick = onBack,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (uiState.loadState) {
                ProfileLoadState.LOADING -> ProfileLoadingContent()
                ProfileLoadState.ERROR -> EmptyFeedView(
                    title = "Couldn't load profile",
                    subtitle = "Pull to refresh to try again",
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("profile.error"),
                )
                else -> {
                    uiState.user?.let { user ->
                        ProfileHeader(
                            user = user,
                            followRelationship = uiState.followRelationship,
                            isFollowActionInProgress = uiState.isFollowActionInProgress,
                            onFollowClick = onFollowClick,
                            onOverflowClick = onOverflowClick,
                            modifier = Modifier.padding(horizontal = Dimensions.Padding.horizontal),
                        )
                    }

                    ProfileTabControl(
                        selectedTab = uiState.selectedTab,
                        onTabSelected = onTabSelected,
                    )

                    Box(modifier = Modifier.fillMaxSize()) {
                        when (uiState.selectedTab) {
                            ProfileTab.Spots -> when (uiState.loadState) {
                                ProfileLoadState.EMPTY -> ProfileEmptySpotsView()
                                else -> ProfileSpotGrid(
                                    spots = uiState.spots,
                                    isLoadingMore = uiState.isLoadingMoreSpots,
                                    onSpotClick = onSpotSelected,
                                    onLoadMore = onLoadMoreSpots,
                                )
                            }
                            ProfileTab.Map -> uiState.user?.let { user ->
                                ProfileMapView(
                                    userId = user.id,
                                    profileNavigationBus = profileNavigationBus,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    ProfileOverflowMenu(
        expanded = uiState.showOverflowMenu,
        isOwnProfile = uiState.user?.isCurrentUser == true,
        isPro = uiState.user?.isPro == true,
        isPrivate = uiState.user?.isPrivate == true,
        pendingFollowRequestCount = uiState.pendingFollowRequestCount,
        onDismiss = onDismissOverflow,
        onAction = onOverflowAction,
    )
}
