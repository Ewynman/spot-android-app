package com.spot.android.feature.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spot.android.core.design.Dimensions
import com.spot.android.core.design.component.Toast
import com.spot.android.core.design.component.ToastType
import com.spot.android.core.design.component.TopNavigationView
import com.spot.android.data.search.SearchSegment
import com.spot.android.feature.profile.ProfileContentHost
import com.spot.android.feature.safety.LocalSafetyActions
import com.spot.android.navigation.OverlayHostViewModel
import com.spot.android.navigation.ProfileNavigationBus
import com.spot.android.navigation.SpotTab
import com.spot.android.navigation.TabReselectBus

/**
 * Search tab with users/locations/vibes segments, history, and spot grids.
 *
 * Reference: PRD/09-search.md
 */
@Composable
fun SearchScreen(
    tabReselectBus: TabReselectBus,
    overlayViewModel: OverlayHostViewModel,
    profileNavigationBus: ProfileNavigationBus,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val safetyActions = LocalSafetyActions.current

    LaunchedEffect(Unit) {
        viewModel.onFirstAppear()
    }

    LaunchedEffect(tabReselectBus) {
        tabReselectBus.reselectEvents.collect { tab ->
            if (tab == SpotTab.Search) {
                viewModel.onTabReselected()
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is SearchEffect.ShowPaywall -> {
                    overlayViewModel.showPaywall(entryPoint = effect.entryPoint)
                }
            }
        }
    }

    when (uiState.mode) {
        SearchScreenMode.Main -> SearchMainContent(
            uiState = uiState,
            modifier = modifier,
            onQueryChanged = viewModel::onQueryChanged,
            onSegmentSelected = viewModel::onSegmentSelected,
            onHistoryItemSelected = viewModel::onHistoryItemSelected,
            onUserSelected = viewModel::onUserSelected,
            onLocationSelected = viewModel::onLocationSelected,
            onVibeSelected = viewModel::onVibeSelected,
            onDismissToast = viewModel::clearErrorToast,
        )

        SearchScreenMode.Grid -> {
            val grid = uiState.grid
            if (grid != null) {
                SearchGridContent(
                    uiState = uiState,
                    grid = grid,
                    modifier = modifier,
                    onBack = viewModel::onBackFromGrid,
                    onSpotSelected = viewModel::onGridSpotSelected,
                    onLoadMore = viewModel::onGridLoadMore,
                    onFilterClick = viewModel::openVibeFilterSheet,
                    onDismissToast = viewModel::clearErrorToast,
                )
            }
        }

        SearchScreenMode.ExpandedSpot -> {
            val spot = uiState.expandedSpot
            if (spot != null) {
                SearchExpandedSpotView(
                    spot = spot,
                    onBack = viewModel::onBackFromExpandedSpot,
                    onLikeClick = { viewModel.toggleLike(spot) },
                    onBookmarkClick = { viewModel.toggleBookmark(spot) },
                    onOverflowClick = safetyActions?.let { actions ->
                        { actions.openSpotOverflowMenu(spot) }
                    },
                    modifier = modifier.padding(horizontal = Dimensions.Padding.horizontal),
                )
            }
        }

        SearchScreenMode.UserProfile -> {
            val user = uiState.selectedUser
            if (user != null) {
                ProfileContentHost(
                    userId = user.id,
                    showBackButton = true,
                    onBack = viewModel::onBackFromUserProfile,
                    overlayViewModel = overlayViewModel,
                    profileNavigationBus = profileNavigationBus,
                    modifier = modifier.testTag("search.userProfile"),
                )
            }
        }
    }

    if (gridShowsFilterSheet(uiState)) {
        SearchVibeFilterSheet(
            availableVibes = uiState.availableVibeTags,
            selectedVibeIds = uiState.grid?.selectedVibeFilterIds.orEmpty(),
            onVibeToggle = viewModel::toggleGridVibeFilter,
            onApply = viewModel::applyGridVibeFilters,
            onDismiss = viewModel::dismissVibeFilterSheet,
        )
    }
}

@Composable
private fun SearchMainContent(
    uiState: SearchUiState,
    onQueryChanged: (String) -> Unit,
    onSegmentSelected: (SearchSegment) -> Unit,
    onHistoryItemSelected: (com.spot.android.data.search.SearchHistoryItem) -> Unit,
    onUserSelected: (com.spot.android.data.model.User) -> Unit,
    onLocationSelected: (String) -> Unit,
    onVibeSelected: (com.spot.android.data.model.VibeTag) -> Unit,
    onDismissToast: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.testTag("search.searchRoot"),
        topBar = { TopNavigationView() },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Dimensions.Padding.horizontal),
        ) {
            SearchSegmentControl(
                selectedSegment = uiState.segment,
                onSegmentSelected = onSegmentSelected,
                modifier = Modifier.padding(top = 8.dp),
            )

            SearchQueryField(
                query = uiState.query,
                onQueryChanged = onQueryChanged,
                modifier = Modifier.padding(top = 16.dp),
            )

            when {
                uiState.loadState == SearchLoadState.Loading -> {
                    SearchLoadingIndicator(modifier = Modifier.weight(1f))
                }

                uiState.query.isBlank() &&
                    uiState.segment != SearchSegment.Vibes &&
                    uiState.history.isNotEmpty() -> {
                    SearchHistoryList(
                        items = uiState.history,
                        onItemClick = onHistoryItemSelected,
                        modifier = Modifier.weight(1f),
                    )
                }

                uiState.loadState == SearchLoadState.Empty -> {
                    SearchEmptyMessage(
                        message = emptyMessageForSegment(uiState.segment),
                        modifier = Modifier.weight(1f),
                    )
                }

                uiState.segment == SearchSegment.Users && uiState.users.isNotEmpty() -> {
                    SearchUserResults(
                        users = uiState.users,
                        onUserClick = onUserSelected,
                        modifier = Modifier.weight(1f),
                    )
                }

                uiState.segment == SearchSegment.Locations && uiState.locations.isNotEmpty() -> {
                    SearchLocationResults(
                        locations = uiState.locations,
                        onLocationClick = onLocationSelected,
                        modifier = Modifier.weight(1f),
                    )
                }

                uiState.segment == SearchSegment.Vibes && uiState.vibes.isNotEmpty() -> {
                    SearchVibeResults(
                        vibes = uiState.vibes,
                        onVibeClick = onVibeSelected,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        uiState.errorToast?.let { message ->
            Toast(
                message = message,
                type = ToastType.ERROR,
                onDismiss = onDismissToast,
                modifier = Modifier.testTag("search.errorToast"),
            )
        }
    }
}

@Composable
private fun SearchGridContent(
    uiState: SearchUiState,
    grid: SearchGridUiState,
    onBack: () -> Unit,
    onSpotSelected: (com.spot.android.data.model.Spot) -> Unit,
    onLoadMore: (Int) -> Unit,
    onFilterClick: () -> Unit,
    onDismissToast: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val showFilter = uiState.isPro && !grid.isVibeGrid && grid.title.isNotBlank()

    SearchGridScaffold(
        title = grid.title,
        showFilterButton = showFilter,
        onBack = onBack,
        onFilterClick = onFilterClick,
        modifier = modifier,
    ) { innerPadding ->
        when {
            grid.loadState == SearchLoadState.Loading -> {
                SearchLoadingIndicator(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                )
            }

            grid.loadState == SearchLoadState.Empty -> {
                SearchEmptyMessage(
                    message = if (grid.isVibeGrid) "No spots found" else "No spots at this location",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                )
            }

            else -> {
                SearchSpotGrid(
                    spots = grid.spots,
                    isLoadingMore = grid.isLoadingMore,
                    onSpotClick = onSpotSelected,
                    onLoadMore = onLoadMore,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }

        uiState.errorToast?.let { message ->
            Toast(
                message = message,
                type = ToastType.ERROR,
                onDismiss = onDismissToast,
                modifier = Modifier.testTag("search.gridErrorToast"),
            )
        }
    }
}

private fun emptyMessageForSegment(segment: SearchSegment): String = when (segment) {
    SearchSegment.Users -> "No users found"
    SearchSegment.Locations -> "No locations found"
    SearchSegment.Vibes -> "No vibes found"
}

private fun gridShowsFilterSheet(uiState: SearchUiState): Boolean {
    return uiState.grid?.showVibeFilterSheet == true
}
