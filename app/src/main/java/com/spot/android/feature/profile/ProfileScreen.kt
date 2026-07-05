package com.spot.android.feature.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spot.android.core.design.Dimensions
import com.spot.android.core.design.component.EmptyFeedView
import com.spot.android.core.design.component.Toast
import com.spot.android.core.design.component.ToastType
import com.spot.android.feature.safety.LocalSafetyActions
import com.spot.android.navigation.OverlayHostViewModel
import com.spot.android.navigation.ProfileNavigationBus
import com.spot.android.navigation.SpotTab
import com.spot.android.navigation.TabReselectBus

/**
 * Profile tab showing the signed-in user's profile.
 *
 * Reference: PRD/10-profile-social.md
 */
@Composable
fun ProfileScreen(
    tabReselectBus: TabReselectBus,
    overlayViewModel: OverlayHostViewModel,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(key = "own_profile"),
) {
    LaunchedEffect(tabReselectBus) {
        tabReselectBus.reselectEvents.collect { tab ->
            if (tab == SpotTab.Profile) {
                viewModel.onTabReselected()
            }
        }
    }

    ProfileContentHost(
        userId = null,
        showBackButton = false,
        onBack = {},
        overlayViewModel = overlayViewModel,
        viewModel = viewModel,
        modifier = modifier,
    )
}

/**
 * Full-screen other-user profile launched from feed/map/search.
 */
@Composable
fun ProfileOverlayScreen(
    userId: String,
    profileNavigationBus: ProfileNavigationBus,
    overlayViewModel: OverlayHostViewModel,
    modifier: Modifier = Modifier,
) {
    ProfileContentHost(
        userId = userId,
        showBackButton = true,
        onBack = profileNavigationBus::closeProfile,
        overlayViewModel = overlayViewModel,
        profileNavigationBus = profileNavigationBus,
        viewModel = hiltViewModel(key = "overlay_$userId"),
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FollowRequestsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FollowRequestsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.onFirstAppear()
    }

    val lastVisibleIndex by remember {
        derivedStateOf {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        }
    }

    LaunchedEffect(lastVisibleIndex) {
        viewModel.loadMoreIfNeeded(lastVisibleIndex)
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isRefreshing,
        onRefresh = viewModel::refresh,
    )

    ProfileSubScreenScaffold(
        title = "Follow Requests",
        onBack = onBack,
        modifier = modifier.testTag("profile.followRequestsRoot"),
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pullRefresh(pullRefreshState),
        ) {
            when (uiState.loadState) {
                FollowRequestsLoadState.LOADING -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("profile.followRequestsLoading"),
                )
                FollowRequestsLoadState.EMPTY -> EmptyFeedView(
                    title = "No pending requests",
                    subtitle = "Incoming follow requests will appear here",
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("profile.followRequestsEmpty"),
                )
                FollowRequestsLoadState.ERROR -> EmptyFeedView(
                    title = "Couldn't load requests",
                    subtitle = "Pull to refresh to try again",
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("profile.followRequestsError"),
                )
                FollowRequestsLoadState.READY -> LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("profile.followRequestsList"),
                ) {
                    items(uiState.requests, key = { it.id }) { request ->
                        FollowRequestRow(
                            request = request,
                            isActionInProgress = uiState.actionInProgressIds.contains(request.id),
                            onAccept = {
                                viewModel.acceptRequest(request.id, request.requester.id)
                            },
                            onDeny = { viewModel.denyRequest(request.id) },
                        )
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = uiState.isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
            )

            uiState.errorMessage?.let { message ->
                Toast(
                    message = message,
                    type = ToastType.ERROR,
                    onDismiss = viewModel::clearErrorMessage,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp),
                )
            }
        }
    }
}

@Composable
fun ProfileEngagementScreen(
    kind: ProfileEngagementKind,
    onBack: () -> Unit,
    overlayViewModel: OverlayHostViewModel,
    modifier: Modifier = Modifier,
    viewModel: ProfileEngagementViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val safetyActions = LocalSafetyActions.current

    LaunchedEffect(kind) {
        viewModel.configure(kind)
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ProfileEffect.ShowPaywall -> overlayViewModel.showPaywall(entryPoint = effect.entryPoint)
                ProfileEffect.OpenSettings -> Unit
            }
        }
    }

    val title = when (kind) {
        ProfileEngagementKind.Likes -> "Your Likes"
        ProfileEngagementKind.Bookmarks -> "Your Bookmarks"
    }

    val expandedSpot = uiState.expandedSpot
    if (expandedSpot != null) {
        ProfileExpandedSpotView(
            spot = expandedSpot,
            isOwnProfile = true,
            onBack = viewModel::onBackFromExpandedSpot,
            onLikeClick = { viewModel.toggleLike(expandedSpot) },
            onBookmarkClick = { viewModel.toggleBookmark(expandedSpot) },
            onOverflowClick = { safetyActions?.openSpotOverflowMenu(expandedSpot) },
            onDeleteClick = null,
            modifier = modifier.padding(horizontal = Dimensions.Padding.horizontal),
        )
        return
    }

    ProfileSubScreenScaffold(
        title = title,
        onBack = onBack,
        modifier = modifier.testTag(
            when (kind) {
                ProfileEngagementKind.Likes -> "profile.likesRoot"
                ProfileEngagementKind.Bookmarks -> "profile.bookmarksRoot"
            },
        ),
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (uiState.loadState) {
                ProfileEngagementLoadState.LOADING -> ProfileLoadingContent()
                ProfileEngagementLoadState.EMPTY -> EmptyFeedView(
                    title = when (kind) {
                        ProfileEngagementKind.Likes -> "No liked spots"
                        ProfileEngagementKind.Bookmarks -> "No bookmarked spots"
                    },
                    subtitle = when (kind) {
                        ProfileEngagementKind.Likes -> "Spots you like will appear here"
                        ProfileEngagementKind.Bookmarks -> "Saved spots will appear here"
                    },
                    modifier = Modifier.fillMaxSize(),
                )
                ProfileEngagementLoadState.ERROR -> EmptyFeedView(
                    title = "Couldn't load spots",
                    subtitle = "Go back and try again",
                    modifier = Modifier.fillMaxSize(),
                )
                ProfileEngagementLoadState.READY -> ProfileSpotGrid(
                    spots = uiState.spots,
                    isLoadingMore = uiState.isLoadingMore,
                    onSpotClick = viewModel::onSpotSelected,
                    onLoadMore = viewModel::loadMoreIfNeeded,
                )
            }
        }
    }
}
