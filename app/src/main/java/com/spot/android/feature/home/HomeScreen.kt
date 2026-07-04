package com.spot.android.feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spot.android.core.design.component.Banner
import com.spot.android.core.design.component.BannerType
import com.spot.android.core.design.component.EmptyFeedCaughtUp
import com.spot.android.core.design.component.EmptyFeedNoEligibleSpots
import com.spot.android.core.design.component.EmptyFeedNoSpotsGlobal
import com.spot.android.core.design.component.EmptyFeedView
import com.spot.android.core.design.component.SkeletonSpotCard
import com.spot.android.core.design.component.SpotCard
import com.spot.android.core.design.component.Toast
import com.spot.android.core.design.component.ToastType
import com.spot.android.core.design.component.TopNavigationView
import com.spot.android.data.feed.HomeFeedEmptyReason
import com.spot.android.data.post.PublishCoordinatorState
import com.spot.android.feature.safety.LocalSafetyActions
import com.spot.android.navigation.OverlayHostViewModel
import com.spot.android.navigation.SpotTab
import com.spot.android.navigation.TabReselectBus
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Home feed tab with server-ranked paginated spots.
 *
 * Reference: PRD/06-home-feed.md
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    tabReselectBus: TabReselectBus,
    overlayViewModel: OverlayHostViewModel,
    modifier: Modifier = Modifier,
    viewModel: HomeFeedViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val safetyActions = LocalSafetyActions.current
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.onFirstAppear()
    }

    LaunchedEffect(tabReselectBus) {
        tabReselectBus.reselectEvents.collect { tab ->
            if (tab == SpotTab.Home) {
                viewModel.onTabReselected()
            }
        }
    }

    LaunchedEffect(uiState.scrollToTopTrigger) {
        if (uiState.scrollToTopTrigger > 0) {
            listState.animateScrollToItem(0)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HomeFeedEffect.ShowPaywall -> {
                    overlayViewModel.showPaywall(entryPoint = effect.entryPoint)
                }
            }
        }
    }

    val lastVisibleIndex by remember {
        derivedStateOf {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        }
    }

    LaunchedEffect(lastVisibleIndex) {
        viewModel.loadMoreIfNeeded(lastVisibleIndex)
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.map { it.index } }
            .distinctUntilChanged()
            .collect { indices ->
                val spots = viewModel.uiState.value.spots
                indices.forEach { index ->
                    spots.getOrNull(index)?.let { viewModel.recordImpression(it.id) }
                }
            }
    }

    val isRefreshing = uiState.isRefreshing
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = viewModel::refresh,
    )

    Scaffold(
        modifier = modifier.testTag("home.feedRoot"),
        topBar = { TopNavigationView() },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            PublishBanner(
                state = uiState.publishState,
                onDismiss = viewModel::dismissPublishBanner,
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState),
            ) {
            when (uiState.loadState) {
                FeedLoadState.LOADING_INITIAL -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("home.feedLoading"),
                        contentPadding = PaddingValues(vertical = 8.dp),
                    ) {
                        items(3) { SkeletonSpotCard() }
                    }
                }

                FeedLoadState.EMPTY -> {
                    HomeFeedEmptyContent(
                        reason = uiState.emptyReason,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                FeedLoadState.ERROR -> {
                    EmptyFeedView(
                        title = "Couldn't load feed",
                        subtitle = "Pull to refresh to try again",
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("home.feedError"),
                    )
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("home.feedList"),
                        contentPadding = PaddingValues(vertical = 8.dp),
                    ) {
                        items(
                            items = uiState.spots,
                            key = { it.id },
                        ) { spot ->
                            SpotCard(
                                spot = spot,
                                onLikeClick = { viewModel.toggleLike(spot) },
                                onBookmarkClick = { viewModel.toggleBookmark(spot) },
                                onMoreClick = safetyActions?.let { actions ->
                                    { actions.openSpotOverflowMenu(spot) }
                                },
                                modifier = Modifier.padding(bottom = 16.dp),
                            )
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .testTag("home.pullRefresh"),
            )

            uiState.errorToast?.let { message ->
                Toast(
                    message = message,
                    type = ToastType.ERROR,
                    visible = true,
                    onDismiss = viewModel::clearErrorToast,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                        .testTag("home.errorToast"),
                )
            }

            uiState.successToast?.let { message ->
                Toast(
                    message = message,
                    type = ToastType.SUCCESS,
                    visible = true,
                    onDismiss = viewModel::clearSuccessToast,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                        .testTag("home.successToast"),
                )
            }
        }
        }
    }
}

@Composable
private fun PublishBanner(
    state: PublishCoordinatorState,
    onDismiss: () -> Unit,
) {
    when (state) {
        PublishCoordinatorState.Uploading -> Banner(
            message = "Uploading your spot…",
            type = BannerType.LOADING,
            showProgress = true,
            modifier = Modifier.testTag("home.publishBanner"),
        )
        PublishCoordinatorState.Moderating -> Banner(
            message = "Checking your images…",
            type = BannerType.LOADING,
            showProgress = true,
            modifier = Modifier.testTag("home.publishBanner"),
        )
        PublishCoordinatorState.Publishing -> Banner(
            message = "Publishing your spot…",
            type = BannerType.LOADING,
            showProgress = true,
            modifier = Modifier.testTag("home.publishBanner"),
        )
        is PublishCoordinatorState.Failed -> {
            Banner(
                message = state.message,
                type = BannerType.ERROR,
                modifier = Modifier.testTag("home.publishBanner"),
            )
            LaunchedEffect(state) {
                kotlinx.coroutines.delay(5000)
                onDismiss()
            }
        }
        PublishCoordinatorState.Idle,
        is PublishCoordinatorState.Success,
        -> Unit
    }
}

@Composable
private fun HomeFeedEmptyContent(
    reason: HomeFeedEmptyReason?,
    modifier: Modifier = Modifier,
) {
    when (reason) {
        HomeFeedEmptyReason.CAUGHT_UP -> EmptyFeedCaughtUp(modifier = modifier.testTag("home.feedEmpty.caughtUp"))
        HomeFeedEmptyReason.NO_SPOTS_GLOBAL -> EmptyFeedNoSpotsGlobal(modifier = modifier.testTag("home.feedEmpty.noSpotsGlobal"))
        HomeFeedEmptyReason.NO_ELIGIBLE_SPOTS,
        null,
        -> EmptyFeedNoEligibleSpots(modifier = modifier.testTag("home.feedEmpty.noEligible"))
    }
}
