package com.spot.android.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spot.android.core.logging.LogCategory
import com.spot.android.core.logging.SpotLogger
import com.spot.android.core.util.Constants
import com.spot.android.data.auth.UserSessionHolder
import com.spot.android.data.feed.EngagementRepository
import com.spot.android.data.model.Spot
import com.spot.android.data.model.User
import com.spot.android.data.model.VibeTag
import com.spot.android.data.search.SearchGridPageLoader
import com.spot.android.data.search.SearchGridRequest
import com.spot.android.data.search.SearchHistoryItem
import com.spot.android.data.search.SearchHistoryStore
import com.spot.android.data.search.SearchHistoryType
import com.spot.android.data.search.SearchRepository
import com.spot.android.data.search.SearchSegment
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the search tab.
 *
 * Reference: PRD/09-search.md
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val searchHistoryStore: SearchHistoryStore,
    private val searchGridPageLoader: SearchGridPageLoader,
    private val engagementRepository: EngagementRepository,
    private val userSessionHolder: UserSessionHolder,
    private val logger: SpotLogger,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _queryInput = MutableStateFlow("")
    private val _effects = Channel<SearchEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var searchJob: Job? = null
    private var gridLoadJob: Job? = null
    private var initialLoadStarted = false

    init {
        observeQueryChanges()
        observeSession()
    }

    fun onFirstAppear() {
        if (initialLoadStarted) return
        initialLoadStarted = true
        viewModelScope.launch {
            loadHistory(_uiState.value.segment)
            searchRepository.listAllVibeTags()
                .onSuccess { tags ->
                    _uiState.update { it.copy(availableVibeTags = tags) }
                }
        }
    }

    fun onTabReselected() {
        _queryInput.value = ""
        _uiState.update {
            SearchUiState(
                segment = it.segment,
                isPro = it.isPro,
                availableVibeTags = it.availableVibeTags,
            )
        }
        viewModelScope.launch {
            loadHistory(_uiState.value.segment)
            runSearch(query = "", segment = _uiState.value.segment)
        }
    }

    fun onQueryChanged(query: String) {
        _queryInput.value = query
        _uiState.update {
            it.copy(
                query = query,
                mode = SearchScreenMode.Main,
                grid = null,
                expandedSpot = null,
                selectedUser = null,
            )
        }
    }

    fun onSegmentSelected(segment: SearchSegment) {
        if (_uiState.value.segment == segment) return
        _uiState.update {
            it.copy(
                segment = segment,
                mode = SearchScreenMode.Main,
                grid = null,
                expandedSpot = null,
                selectedUser = null,
            )
        }
        viewModelScope.launch {
            loadHistory(segment)
            runSearch(query = _queryInput.value, segment = segment)
        }
    }

    fun onHistoryItemSelected(item: SearchHistoryItem) {
        when (item.type) {
            SearchHistoryType.User -> {
                viewModelScope.launch {
                    searchRepository.searchUsers(item.query)
                        .onSuccess { users ->
                            users.firstOrNull()?.let { openUserProfile(it) }
                        }
                }
            }
            SearchHistoryType.Location -> openLocationGrid(item.displayText)
            SearchHistoryType.Vibe -> {
                viewModelScope.launch {
                    searchRepository.searchVibes(item.query)
                        .onSuccess { vibes ->
                            vibes.firstOrNull()?.let { openVibeGrid(it) }
                        }
                }
            }
        }
    }

    fun onUserSelected(user: User) {
        viewModelScope.launch {
            searchHistoryStore.addItem(
                segment = SearchSegment.Users,
                item = SearchHistoryItem(
                    type = SearchHistoryType.User,
                    query = user.username,
                    displayText = user.username,
                ),
            )
            loadHistory(SearchSegment.Users)
        }
        openUserProfile(user)
    }

    fun onLocationSelected(locationName: String) {
        viewModelScope.launch {
            searchHistoryStore.addItem(
                segment = SearchSegment.Locations,
                item = SearchHistoryItem(
                    type = SearchHistoryType.Location,
                    query = locationName,
                    displayText = locationName,
                ),
            )
            loadHistory(SearchSegment.Locations)
        }
        openLocationGrid(locationName)
    }

    fun onVibeSelected(vibe: VibeTag) {
        viewModelScope.launch {
            searchHistoryStore.addItem(
                segment = SearchSegment.Vibes,
                item = SearchHistoryItem(
                    type = SearchHistoryType.Vibe,
                    query = vibe.name,
                    displayText = vibe.name,
                    vibeTagIds = listOf(vibe.id),
                ),
            )
            loadHistory(SearchSegment.Vibes)
        }
        openVibeGrid(vibe)
    }

    fun onBackFromGrid() {
        _uiState.update {
            it.copy(
                mode = SearchScreenMode.Main,
                grid = null,
                expandedSpot = null,
            )
        }
    }

    fun onBackFromExpandedSpot() {
        _uiState.update {
            it.copy(
                mode = SearchScreenMode.Grid,
                expandedSpot = null,
            )
        }
    }

    fun onBackFromUserProfile() {
        _uiState.update {
            it.copy(
                mode = SearchScreenMode.Main,
                selectedUser = null,
            )
        }
    }

    fun onGridSpotSelected(spot: Spot) {
        _uiState.update {
            it.copy(
                mode = SearchScreenMode.ExpandedSpot,
                expandedSpot = spot,
            )
        }
    }

    fun onGridLoadMore(lastVisibleIndex: Int) {
        val grid = _uiState.value.grid ?: return
        if (grid.loadState == SearchLoadState.Loading ||
            grid.isLoadingMore ||
            !grid.hasMore
        ) {
            return
        }
        val threshold = grid.spots.lastIndex - 3
        if (lastVisibleIndex >= threshold) {
            loadMoreGrid()
        }
    }

    fun openVibeFilterSheet() {
        _uiState.update { state ->
            state.copy(
                grid = state.grid?.copy(showVibeFilterSheet = true),
            )
        }
    }

    fun dismissVibeFilterSheet() {
        _uiState.update { state ->
            state.copy(
                grid = state.grid?.copy(showVibeFilterSheet = false),
            )
        }
    }

    fun toggleGridVibeFilter(vibeId: String) {
        _uiState.update { state ->
            val grid = state.grid ?: return@update state
            val updated = grid.selectedVibeFilterIds.toMutableSet()
            if (updated.contains(vibeId)) {
                updated.remove(vibeId)
            } else {
                updated.add(vibeId)
            }
            state.copy(
                grid = grid.copy(selectedVibeFilterIds = updated),
            )
        }
    }

    fun applyGridVibeFilters() {
        val grid = _uiState.value.grid ?: return
        val locationPattern = grid.request.locationPattern ?: return
        dismissVibeFilterSheet()
        val request = SearchGridRequest.Location(
            locationPattern = locationPattern,
            vibeTagIds = grid.selectedVibeFilterIds.toList(),
        )
        reloadGrid(
            title = grid.title,
            request = request,
            isVibeGrid = false,
            selectedVibeFilterIds = grid.selectedVibeFilterIds,
        )
    }

    fun toggleLike(spot: Spot) {
        val currentlyLiked = spot.isLiked
        updateSpotEverywhere(spot.copy(isLiked = !currentlyLiked, likes = spot.likes + if (currentlyLiked) -1 else 1))

        viewModelScope.launch {
            val result = if (currentlyLiked) {
                engagementRepository.unlikeSpot(spot.id)
            } else {
                engagementRepository.likeSpot(spot.id)
            }
            if (result.isSuccess) {
                if (currentlyLiked) userSessionHolder.removeLike(spot.id) else userSessionHolder.addLike(spot.id)
            } else {
                updateSpotEverywhere(spot)
            }
        }
    }

    fun toggleBookmark(spot: Spot) {
        val currentlySaved = spot.isSaved
        if (!currentlySaved && !userSessionHolder.isPro.value) {
            val bookmarkCount = userSessionHolder.bookmarkedSpots.value.size
            if (bookmarkCount >= Constants.ContentLimits.FREE_BOOKMARK_CAP) {
                viewModelScope.launch {
                    _effects.send(SearchEffect.ShowPaywall(entryPoint = "bookmark_cap"))
                }
                return
            }
        }

        updateSpotEverywhere(spot.copy(isSaved = !currentlySaved))

        viewModelScope.launch {
            val result = if (currentlySaved) {
                engagementRepository.unbookmarkSpot(spot.id)
            } else {
                engagementRepository.bookmarkSpot(spot.id)
            }
            if (result.isSuccess) {
                if (currentlySaved) userSessionHolder.removeBookmark(spot.id) else userSessionHolder.addBookmark(spot.id)
            } else {
                updateSpotEverywhere(spot)
            }
        }
    }

    fun clearErrorToast() {
        _uiState.update { it.copy(errorToast = null) }
    }

    private fun observeQueryChanges() {
        _queryInput
            .debounce(Constants.Search.DEBOUNCE_MS)
            .distinctUntilChanged()
            .onEach { query ->
                runSearch(query = query, segment = _uiState.value.segment)
            }
            .launchIn(viewModelScope)
    }

    private fun observeSession() {
        combine(
            userSessionHolder.isPro,
            userSessionHolder.likedSpots,
            userSessionHolder.bookmarkedSpots,
        ) { isPro, liked, bookmarked ->
            Triple(isPro, liked, bookmarked)
        }.onEach { (isPro, liked, bookmarked) ->
            _uiState.update { state ->
                state.copy(
                    isPro = isPro,
                    grid = state.grid?.let { grid ->
                        grid.copy(
                            spots = grid.spots.map { spot ->
                                spot.copy(
                                    isLiked = liked.contains(spot.id),
                                    isSaved = bookmarked.contains(spot.id),
                                )
                            },
                        )
                    },
                    expandedSpot = state.expandedSpot?.let { spot ->
                        spot.copy(
                            isLiked = liked.contains(spot.id),
                            isSaved = bookmarked.contains(spot.id),
                        )
                    },
                )
            }
        }.launchIn(viewModelScope)
    }

    private suspend fun loadHistory(segment: SearchSegment) {
        val history = searchHistoryStore.getHistory(segment)
        _uiState.update { it.copy(history = history) }
    }

    private fun runSearch(query: String, segment: SearchSegment) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            val trimmed = query.trim()
            if (trimmed.isEmpty()) {
                when (segment) {
                    SearchSegment.Users, SearchSegment.Locations -> {
                        _uiState.update {
                            it.copy(
                                loadState = SearchLoadState.Loaded,
                                users = emptyList(),
                                locations = emptyList(),
                                vibes = emptyList(),
                            )
                        }
                    }
                    SearchSegment.Vibes -> {
                        _uiState.update { it.copy(loadState = SearchLoadState.Loading) }
                        searchRepository.listAllVibeTags()
                            .onSuccess { tags ->
                                _uiState.update {
                                    it.copy(
                                        loadState = if (tags.isEmpty()) SearchLoadState.Empty else SearchLoadState.Loaded,
                                        vibes = tags,
                                    )
                                }
                            }
                            .onFailure {
                                _uiState.update {
                                    it.copy(
                                        loadState = SearchLoadState.Error,
                                        errorToast = "Network error. Please try again.",
                                    )
                                }
                            }
                    }
                }
                return@launch
            }

            _uiState.update { it.copy(loadState = SearchLoadState.Loading) }
            when (segment) {
                SearchSegment.Users -> {
                    searchRepository.searchUsers(trimmed)
                        .onSuccess { users ->
                            _uiState.update {
                                it.copy(
                                    loadState = if (users.isEmpty()) SearchLoadState.Empty else SearchLoadState.Loaded,
                                    users = users,
                                    locations = emptyList(),
                                    vibes = emptyList(),
                                )
                            }
                        }
                        .onFailure { handleSearchFailure(it) }
                }
                SearchSegment.Locations -> {
                    searchRepository.searchLocations(trimmed)
                        .onSuccess { locations ->
                            _uiState.update {
                                it.copy(
                                    loadState = if (locations.isEmpty()) SearchLoadState.Empty else SearchLoadState.Loaded,
                                    users = emptyList(),
                                    locations = locations,
                                    vibes = emptyList(),
                                )
                            }
                        }
                        .onFailure { handleSearchFailure(it) }
                }
                SearchSegment.Vibes -> {
                    searchRepository.searchVibes(trimmed)
                        .onSuccess { vibes ->
                            _uiState.update {
                                it.copy(
                                    loadState = if (vibes.isEmpty()) SearchLoadState.Empty else SearchLoadState.Loaded,
                                    users = emptyList(),
                                    locations = emptyList(),
                                    vibes = vibes,
                                )
                            }
                        }
                        .onFailure { handleSearchFailure(it) }
                }
            }
        }
    }

    private fun openUserProfile(user: User) {
        _uiState.update {
            it.copy(
                mode = SearchScreenMode.UserProfile,
                selectedUser = user,
            )
        }
    }

    private fun openLocationGrid(locationName: String) {
        reloadGrid(
            title = locationName,
            request = SearchGridRequest.Location(locationPattern = locationName),
            isVibeGrid = false,
        )
    }

    private fun openVibeGrid(vibe: VibeTag) {
        reloadGrid(
            title = vibe.name,
            request = SearchGridRequest.Vibe(vibeTagIds = listOf(vibe.id)),
            isVibeGrid = true,
        )
    }

    private fun reloadGrid(
        title: String,
        request: SearchGridRequest,
        isVibeGrid: Boolean,
        selectedVibeFilterIds: Set<String> = emptySet(),
    ) {
        gridLoadJob?.cancel()
        _uiState.update {
            it.copy(
                mode = SearchScreenMode.Grid,
                grid = SearchGridUiState(
                    title = title,
                    request = request,
                    isVibeGrid = isVibeGrid,
                    loadState = SearchLoadState.Loading,
                    selectedVibeFilterIds = selectedVibeFilterIds,
                ),
                expandedSpot = null,
            )
        }
        gridLoadJob = viewModelScope.launch {
            searchGridPageLoader.loadInitialPage(request)
                .onSuccess { page ->
                    _uiState.update { state ->
                        state.copy(
                            grid = state.grid?.copy(
                                spots = page.spots,
                                loadState = if (page.spots.isEmpty()) SearchLoadState.Empty else SearchLoadState.Loaded,
                                nextOffset = page.nextOffset,
                                hasMore = page.hasMore,
                            ),
                        )
                    }
                }
                .onFailure {
                    _uiState.update { state ->
                        state.copy(
                            grid = state.grid?.copy(loadState = SearchLoadState.Error),
                            errorToast = "Network error. Please try again.",
                        )
                    }
                }
        }
    }

    private fun loadMoreGrid() {
        val grid = _uiState.value.grid ?: return
        gridLoadJob?.cancel()
        _uiState.update { state ->
            state.copy(grid = grid.copy(isLoadingMore = true))
        }
        gridLoadJob = viewModelScope.launch {
            searchGridPageLoader.loadNextPage(
                request = grid.request,
                currentOffset = grid.nextOffset,
            ).onSuccess { page ->
                _uiState.update { state ->
                    val currentGrid = state.grid ?: return@update state
                    state.copy(
                        grid = currentGrid.copy(
                            spots = currentGrid.spots + page.spots,
                            nextOffset = page.nextOffset,
                            hasMore = page.hasMore,
                            isLoadingMore = false,
                            loadState = if (currentGrid.spots.isEmpty() && page.spots.isEmpty()) {
                                SearchLoadState.Empty
                            } else {
                                SearchLoadState.Loaded
                            },
                        ),
                    )
                }
            }.onFailure {
                _uiState.update { state ->
                    state.copy(
                        grid = state.grid?.copy(isLoadingMore = false),
                        errorToast = "Network error. Please try again.",
                    )
                }
            }
        }
    }

    private fun handleSearchFailure(error: Throwable) {
        logger.e(LogCategory.Network, TAG, "Search query failed", error)
        _uiState.update {
            it.copy(
                loadState = SearchLoadState.Error,
                errorToast = "Network error. Please try again.",
            )
        }
    }

    private fun updateSpotEverywhere(spot: Spot) {
        _uiState.update { state ->
            state.copy(
                grid = state.grid?.copy(
                    spots = state.grid.spots.map { if (it.id == spot.id) spot else it },
                ),
                expandedSpot = if (state.expandedSpot?.id == spot.id) spot else state.expandedSpot,
            )
        }
    }

    private companion object {
        const val TAG = "SearchViewModel"
    }
}
