package com.spot.android.feature.collections

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spot.android.core.logging.LogCategory
import com.spot.android.core.logging.SpotLogger
import com.spot.android.core.util.Constants
import com.spot.android.data.auth.UserSessionHolder
import com.spot.android.data.collections.CollectionsRepository
import com.spot.android.data.feed.EngagementRepository
import com.spot.android.data.feed.FeedEventService
import com.spot.android.data.model.Spot
import com.spot.android.data.model.enums.FeedEventType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for viewing a specific collection's spots.
 * 
 * Reference: PRD/10-profile-social.md, PRD/12-pro-subscription.md
 */
@HiltViewModel
class CollectionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val collectionsRepository: CollectionsRepository,
    private val engagementRepository: EngagementRepository,
    private val feedEventService: FeedEventService,
    private val userSessionHolder: UserSessionHolder,
    private val logger: SpotLogger,
) : ViewModel() {

    private val collectionId: String = savedStateHandle["collectionId"] ?: ""

    private val _uiState = MutableStateFlow(CollectionDetailUiState())
    val uiState: StateFlow<CollectionDetailUiState> = _uiState.asStateFlow()

    private val _effects = Channel<CollectionsEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var offset = 0
    private var hasMore = true
    private var initialLoadStarted = false

    fun onFirstAppear() {
        if (initialLoadStarted) return
        initialLoadStarted = true
        loadCollection()
        loadSpots(reset = true)
    }

    fun refresh() {
        loadCollection()
        offset = 0
        hasMore = true
        loadSpots(reset = true)
    }

    fun loadMoreIfNeeded(lastVisibleIndex: Int) {
        val state = _uiState.value
        if (state.isLoadingMore || !hasMore) return
        if (lastVisibleIndex >= state.spots.lastIndex - 4) {
            loadSpots(reset = false)
        }
    }

    fun onSpotSelected(spot: Spot) {
        feedEventService.recordEvent(
            spotId = spot.id,
            eventType = FeedEventType.DETAIL_OPEN,
        )
        _uiState.update { it.copy(expandedSpot = spot) }
    }

    fun onBackFromExpandedSpot() {
        _uiState.update { it.copy(expandedSpot = null) }
    }

    fun toggleLike(spot: Spot) {
        val currentlyLiked = spot.isLiked
        updateSpot(spot.id) { it.copy(isLiked = !currentlyLiked) }
        if (currentlyLiked) userSessionHolder.removeLike(spot.id)
        else userSessionHolder.addLike(spot.id)

        viewModelScope.launch {
            val result = if (currentlyLiked) {
                engagementRepository.unlikeSpot(spot.id)
            } else {
                engagementRepository.likeSpot(spot.id)
            }
            result.onFailure {
                if (currentlyLiked) userSessionHolder.addLike(spot.id)
                else userSessionHolder.removeLike(spot.id)
                updateSpot(spot.id) { s -> s.copy(isLiked = currentlyLiked) }
            }
        }
    }

    fun toggleBookmark(spot: Spot) {
        val currentlySaved = spot.isSaved
        updateSpot(spot.id) { it.copy(isSaved = !currentlySaved) }
        if (currentlySaved) userSessionHolder.removeBookmark(spot.id)
        else userSessionHolder.addBookmark(spot.id)

        viewModelScope.launch {
            val result = if (currentlySaved) {
                engagementRepository.unbookmarkSpot(spot.id)
            } else {
                engagementRepository.bookmarkSpot(spot.id)
            }
            result.onFailure {
                if (currentlySaved) userSessionHolder.addBookmark(spot.id)
                else userSessionHolder.removeBookmark(spot.id)
                updateSpot(spot.id) { s -> s.copy(isSaved = currentlySaved) }
            }
        }
    }

    fun removeSpotFromCollection(spotId: String) {
        viewModelScope.launch {
            collectionsRepository.removeSpotFromCollection(collectionId, spotId).fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(spots = state.spots.filterNot { it.id == spotId })
                    }
                    _effects.send(CollectionsEffect.ShowToast("Removed from collection"))
                },
                onFailure = { error ->
                    logger.e(LogCategory.Network, TAG, "Failed to remove spot", error)
                    _effects.send(CollectionsEffect.ShowToast("Failed to remove spot"))
                },
            )
        }
    }

    fun startEditingName() {
        _uiState.update { it.copy(isEditingName = true) }
    }

    fun cancelEditingName() {
        _uiState.update { it.copy(isEditingName = false) }
    }

    fun updateCollectionName(newName: String) {
        val trimmedName = newName.trim()
        if (trimmedName.isEmpty()) {
            viewModelScope.launch {
                _effects.send(CollectionsEffect.ShowToast("Name cannot be empty"))
            }
            return
        }

        viewModelScope.launch {
            collectionsRepository.updateCollectionName(collectionId, trimmedName).fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            collection = state.collection?.copy(name = trimmedName),
                            isEditingName = false,
                        )
                    }
                    _effects.send(CollectionsEffect.ShowToast("Collection renamed"))
                },
                onFailure = { error ->
                    logger.e(LogCategory.Network, TAG, "Failed to rename collection", error)
                    _effects.send(CollectionsEffect.ShowToast("Failed to rename"))
                },
            )
        }
    }

    private fun loadCollection() {
        viewModelScope.launch {
            collectionsRepository.getCollection(collectionId).fold(
                onSuccess = { collection ->
                    _uiState.update { it.copy(collection = collection) }
                },
                onFailure = { error ->
                    logger.e(LogCategory.Network, TAG, "Failed to load collection", error)
                },
            )
        }
    }

    private fun loadSpots(reset: Boolean) {
        viewModelScope.launch {
            if (reset) {
                _uiState.update {
                    it.copy(loadState = CollectionDetailLoadState.LOADING, errorMessage = null)
                }
            } else {
                _uiState.update { it.copy(isLoadingMore = true) }
            }

            val currentOffset = if (reset) 0 else offset
            val idsResult = collectionsRepository.getCollectionSpotIds(
                collectionId = collectionId,
                offset = currentOffset,
                limit = Constants.Pagination.DEFAULT_PAGE_SIZE,
            )

            idsResult.fold(
                onSuccess = { spotIds ->
                    hasMore = spotIds.size >= Constants.Pagination.DEFAULT_PAGE_SIZE
                    offset = currentOffset + spotIds.size
                    collectionsRepository.hydrateSpots(spotIds).fold(
                        onSuccess = { spots ->
                            _uiState.update { state ->
                                val merged = if (reset) spots else state.spots + spots
                                state.copy(
                                    spots = merged,
                                    loadState = if (merged.isEmpty()) {
                                        CollectionDetailLoadState.EMPTY
                                    } else {
                                        CollectionDetailLoadState.READY
                                    },
                                    isLoadingMore = false,
                                    hasMore = hasMore,
                                )
                            }
                        },
                        onFailure = {
                            _uiState.update { state ->
                                state.copy(
                                    loadState = CollectionDetailLoadState.ERROR,
                                    isLoadingMore = false,
                                    errorMessage = "Couldn't load spots",
                                )
                            }
                        },
                    )
                },
                onFailure = {
                    logger.e(LogCategory.Network, TAG, "Load collection spot ids failed", it)
                    _uiState.update { state ->
                        state.copy(
                            loadState = CollectionDetailLoadState.ERROR,
                            isLoadingMore = false,
                            errorMessage = "Couldn't load spots",
                        )
                    }
                },
            )
        }
    }

    private fun updateSpot(spotId: String, transform: (Spot) -> Spot) {
        _uiState.update { state ->
            state.copy(
                spots = state.spots.map { if (it.id == spotId) transform(it) else it },
                expandedSpot = state.expandedSpot?.let { spot ->
                    if (spot.id == spotId) transform(spot) else spot
                },
            )
        }
    }

    private companion object {
        const val TAG = "CollectionDetailViewModel"
    }
}
