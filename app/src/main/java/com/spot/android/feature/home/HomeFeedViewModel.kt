package com.spot.android.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spot.android.core.logging.LogCategory
import com.spot.android.core.logging.SpotLogger
import com.spot.android.core.util.Constants
import com.spot.android.data.auth.UserSessionHolder
import com.spot.android.data.content.ContentRemovalEvent
import com.spot.android.data.content.LocalContentRemovalBus
import com.spot.android.data.feed.EngagementRepository
import com.spot.android.data.feed.FeedDiversity
import com.spot.android.data.feed.FeedEventService
import com.spot.android.data.feed.FeedRepository
import com.spot.android.data.feed.FeedSpotHydrator
import com.spot.android.data.feed.HomeFeedEmptyReason
import com.spot.android.data.location.ViewerLocationProvider
import com.spot.android.data.model.Spot
import com.spot.android.data.model.enums.FeedEventType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel for the home feed tab.
 *
 * Reference: PRD/06-home-feed.md, PRD/16-feed-ranking-algorithm.md
 */
@HiltViewModel
class HomeFeedViewModel @Inject constructor(
    private val feedRepository: FeedRepository,
    private val engagementRepository: EngagementRepository,
    private val feedSpotHydrator: FeedSpotHydrator,
    private val feedEventService: FeedEventService,
    private val userSessionHolder: UserSessionHolder,
    private val localContentRemovalBus: LocalContentRemovalBus,
    private val viewerLocationProvider: ViewerLocationProvider,
    private val logger: SpotLogger,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeFeedUiState())
    val uiState: StateFlow<HomeFeedUiState> = _uiState.asStateFlow()

    private val _effects = Channel<HomeFeedEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var batchId: String = UUID.randomUUID().toString()
    private var hasMore = true
    private var loadMoreJob: Job? = null
    private var initialLoadStarted = false

    init {
        observeContentRemovals()
        observeSessionEngagement()
    }

    fun onFirstAppear() {
        if (initialLoadStarted) return
        initialLoadStarted = true
        loadInitialFeed()
    }

    fun refresh() {
        loadMoreJob?.cancel()
        batchId = UUID.randomUUID().toString()
        hasMore = true
        fetchFeed(
            isRefresh = true,
            isInitial = _uiState.value.spots.isEmpty(),
        )
    }

    fun onTabReselected() {
        _uiState.update { it.copy(scrollToTopTrigger = it.scrollToTopTrigger + 1) }
        refresh()
    }

    fun loadMoreIfNeeded(lastVisibleIndex: Int) {
        val state = _uiState.value
        if (state.loadState == FeedLoadState.LOADING_INITIAL ||
            state.loadState == FeedLoadState.LOADING_MORE ||
            !hasMore
        ) {
            return
        }

        val threshold = state.spots.lastIndex - 4
        if (lastVisibleIndex >= threshold) {
            loadMoreJob?.cancel()
            loadMoreJob = viewModelScope.launch {
                fetchFeed(isRefresh = false, isInitial = false, append = true)
            }
        }
    }

    fun toggleLike(spot: Spot) {
        val currentlyLiked = spot.isLiked
        val optimisticCount = if (currentlyLiked) {
            (spot.likes - 1).coerceAtLeast(0)
        } else {
            spot.likes + 1
        }

        updateSpot(spot.id) {
            it.copy(isLiked = !currentlyLiked, likes = optimisticCount)
        }

        if (currentlyLiked) {
            userSessionHolder.removeLike(spot.id)
        } else {
            userSessionHolder.addLike(spot.id)
        }

        viewModelScope.launch {
            val result = if (currentlyLiked) {
                engagementRepository.unlikeSpot(spot.id)
            } else {
                engagementRepository.likeSpot(spot.id)
            }

            result.fold(
                onSuccess = {
                    feedEventService.recordEvent(
                        spotId = spot.id,
                        eventType = if (currentlyLiked) FeedEventType.UNLIKE else FeedEventType.LIKE,
                    )
                },
                onFailure = {
                    logger.w(LogCategory.Feed, TAG, "Like toggle failed; rolling back", it)
                    if (currentlyLiked) {
                        userSessionHolder.addLike(spot.id)
                    } else {
                        userSessionHolder.removeLike(spot.id)
                    }
                    updateSpot(spot.id) {
                        it.copy(isLiked = currentlyLiked, likes = spot.likes)
                    }
                },
            )
        }
    }

    fun toggleBookmark(spot: Spot) {
        val currentlySaved = spot.isSaved

        if (!currentlySaved && !userSessionHolder.isPro.value) {
            val bookmarkCount = userSessionHolder.bookmarkedSpots.value.size
            if (bookmarkCount >= Constants.ContentLimits.FREE_BOOKMARK_CAP) {
                viewModelScope.launch {
                    _effects.send(HomeFeedEffect.ShowPaywall(entryPoint = "bookmark_cap"))
                }
                return
            }
        }

        updateSpot(spot.id) { it.copy(isSaved = !currentlySaved) }

        if (currentlySaved) {
            userSessionHolder.removeBookmark(spot.id)
        } else {
            userSessionHolder.addBookmark(spot.id)
        }

        viewModelScope.launch {
            val result = if (currentlySaved) {
                engagementRepository.unbookmarkSpot(spot.id)
            } else {
                engagementRepository.bookmarkSpot(spot.id)
            }

            result.fold(
                onSuccess = {
                    feedEventService.recordEvent(
                        spotId = spot.id,
                        eventType = if (currentlySaved) FeedEventType.UNSAVE else FeedEventType.SAVE,
                    )
                },
                onFailure = {
                    logger.w(LogCategory.Feed, TAG, "Bookmark toggle failed; rolling back", it)
                    if (currentlySaved) {
                        userSessionHolder.addBookmark(spot.id)
                    } else {
                        userSessionHolder.removeBookmark(spot.id)
                    }
                    updateSpot(spot.id) { it.copy(isSaved = currentlySaved) }
                },
            )
        }
    }

    fun recordImpression(spotId: String) {
        feedEventService.recordEvent(
            spotId = spotId,
            eventType = FeedEventType.IMPRESSION,
            coalesceKey = "impression:$spotId",
        )
    }

    fun clearErrorToast() {
        _uiState.update { it.copy(errorToast = null) }
    }

    private fun loadInitialFeed() {
        fetchFeed(isRefresh = false, isInitial = true)
    }

    private fun fetchFeed(
        isRefresh: Boolean,
        isInitial: Boolean,
        append: Boolean = false,
        forceSeenFallback: Boolean = false,
    ) {
        viewModelScope.launch {
            if (append) {
                _uiState.update { it.copy(loadState = FeedLoadState.LOADING_MORE) }
            } else if (isInitial && _uiState.value.spots.isEmpty()) {
                _uiState.update { it.copy(loadState = FeedLoadState.LOADING_INITIAL) }
            } else {
                _uiState.update { it.copy(isRefreshing = true) }
            }

            val location = viewerLocationProvider.getLocation()
            val feedResult = feedRepository.getHomeFeed(
                limit = Constants.Pagination.DEFAULT_PAGE_SIZE,
                batchId = batchId,
                viewerLat = location?.latitude,
                viewerLng = location?.longitude,
                forceSeenFallback = forceSeenFallback,
            )

            feedResult.fold(
                onSuccess = { rows ->
                    handleFeedSuccess(
                        rows = rows,
                        isRefresh = isRefresh,
                        isInitial = isInitial,
                        append = append,
                        forceSeenFallback = forceSeenFallback,
                    )
                },
                onFailure = { error ->
                    handleFeedFailure(error, isInitial = isInitial && _uiState.value.spots.isEmpty())
                },
            )
        }
    }

    private suspend fun handleFeedSuccess(
        rows: List<com.spot.android.data.dto.HomeFeedRowDto>,
        isRefresh: Boolean,
        isInitial: Boolean,
        append: Boolean,
        forceSeenFallback: Boolean,
    ) {
        if (rows.isEmpty()) {
            if (append) {
                hasMore = false
                _uiState.update {
                    it.copy(
                        loadState = FeedLoadState.LOADED,
                        isRefreshing = false,
                    )
                }
                return
            }

            if (!forceSeenFallback) {
                val statusResult = feedRepository.getHomeFeedStatus()
                val emptyReason = statusResult.getOrNull()
                    ?.status
                    ?.let(HomeFeedEmptyReason::fromStatus)

                if (emptyReason == HomeFeedEmptyReason.CAUGHT_UP) {
                    fetchFeed(
                        isRefresh = isRefresh,
                        isInitial = isInitial,
                        append = append,
                        forceSeenFallback = true,
                    )
                    return
                }

                _uiState.update {
                    it.copy(
                        loadState = FeedLoadState.EMPTY,
                        emptyReason = emptyReason ?: HomeFeedEmptyReason.NO_ELIGIBLE_SPOTS,
                        spots = if (isRefresh) emptyList() else it.spots,
                        isRefreshing = false,
                    )
                }
                return
            }

            _uiState.update {
                it.copy(
                    loadState = FeedLoadState.EMPTY,
                    emptyReason = HomeFeedEmptyReason.CAUGHT_UP,
                    spots = if (isRefresh) emptyList() else it.spots,
                    isRefreshing = false,
                )
            }
            return
        }

        val liked = userSessionHolder.likedSpots.value
        val bookmarked = userSessionHolder.bookmarkedSpots.value
        var hydrated = feedSpotHydrator.hydrateAll(
            rows = rows,
            likedSpotIds = liked,
            bookmarkedSpotIds = bookmarked,
        )

        if (isInitial || isRefresh) {
            val topVibes = feedRepository.getTopVibeNamesForDiversity().getOrElse { emptyList() }
            hydrated = FeedDiversity.apply(hydrated, topVibes)
            if (topVibes.isNotEmpty()) {
                logger.i(LogCategory.Feed, TAG, "Home feed diversity pass applied")
            }
        }

        val merged = if (append) {
            dedupeAppend(_uiState.value.spots, hydrated)
        } else {
            hydrated
        }

        hasMore = rows.size >= Constants.Pagination.DEFAULT_PAGE_SIZE

        _uiState.update {
            it.copy(
                loadState = FeedLoadState.LOADED,
                spots = merged,
                emptyReason = null,
                isRefreshing = false,
                errorToast = null,
            )
        }
    }

    private fun handleFeedFailure(error: Throwable, isInitial: Boolean) {
        logger.e(LogCategory.Feed, TAG, "Feed load failed", error)
        if (isInitial) {
            _uiState.update {
                it.copy(
                    loadState = FeedLoadState.ERROR,
                    isRefreshing = false,
                    errorToast = "Couldn't load feed. Pull to refresh.",
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    isRefreshing = false,
                    errorToast = "Couldn't refresh feed. Please try again.",
                )
            }
        }
    }

    private fun dedupeAppend(existing: List<Spot>, incoming: List<Spot>): List<Spot> {
        val existingIds = existing.map { it.id }.toSet()
        val newSpots = incoming.filterNot { existingIds.contains(it.id) }
        return existing + newSpots
    }

    private fun observeContentRemovals() {
        viewModelScope.launch {
            localContentRemovalBus.removals.collect { event ->
                when (event) {
                    is ContentRemovalEvent.ByAuthor -> removeByAuthor(event.authorUserId)
                    is ContentRemovalEvent.BySpotId -> removeBySpotId(event.spotId)
                }
            }
        }
    }

    private fun observeSessionEngagement() {
        viewModelScope.launch {
            combine(
                userSessionHolder.likedSpots,
                userSessionHolder.bookmarkedSpots,
            ) { liked, bookmarked -> liked to bookmarked }
                .collect { (liked, bookmarked) ->
                    _uiState.update { state ->
                        if (state.spots.isEmpty()) return@update state
                        state.copy(
                            spots = state.spots.map { spot ->
                                spot.copy(
                                    isLiked = liked.contains(spot.id),
                                    isSaved = bookmarked.contains(spot.id),
                                )
                            },
                        )
                    }
                }
        }
    }

    private fun removeByAuthor(authorUserId: String) {
        _uiState.update { state ->
            state.copy(spots = state.spots.filterNot { it.userId == authorUserId })
        }
    }

    private fun removeBySpotId(spotId: String) {
        _uiState.update { state ->
            state.copy(spots = state.spots.filterNot { it.id == spotId })
        }
    }

    private fun updateSpot(spotId: String, transform: (Spot) -> Spot) {
        _uiState.update { state ->
            state.copy(
                spots = state.spots.map { spot ->
                    if (spot.id == spotId) transform(spot) else spot
                },
            )
        }
    }

    private companion object {
        const val TAG = "HomeFeedViewModel"
    }
}
