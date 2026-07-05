package com.spot.android.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spot.android.core.logging.LogCategory
import com.spot.android.core.logging.SpotLogger
import com.spot.android.core.supabase.SessionBridge
import com.spot.android.core.util.Constants
import com.spot.android.data.auth.UserSessionHolder
import com.spot.android.data.feed.EngagementRepository
import com.spot.android.data.model.Spot
import com.spot.android.data.profile.ProfileRepository
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
 * ViewModel for likes/bookmarks grids on own profile.
 *
 * Reference: PRD/10-profile-social.md
 */
@HiltViewModel
class ProfileEngagementViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val engagementRepository: EngagementRepository,
    private val userSessionHolder: UserSessionHolder,
    private val sessionBridge: SessionBridge,
    private val logger: SpotLogger,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ProfileEngagementUiState(kind = ProfileEngagementKind.Likes),
    )
    val uiState: StateFlow<ProfileEngagementUiState> = _uiState.asStateFlow()

    private val _effects = Channel<ProfileEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var offset = 0
    private var hasMore = true
    private var initialLoadStarted = false

    fun configure(kind: ProfileEngagementKind) {
        if (_uiState.value.kind == kind && initialLoadStarted) return
        initialLoadStarted = false
        offset = 0
        hasMore = true
        _uiState.value = ProfileEngagementUiState(kind = kind)
        onFirstAppear()
    }

    fun onFirstAppear() {
        if (initialLoadStarted) return
        initialLoadStarted = true
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
        if (!currentlySaved && !userSessionHolder.isPro.value) {
            val bookmarkCount = userSessionHolder.bookmarkedSpots.value.size
            if (bookmarkCount >= Constants.ContentLimits.FREE_BOOKMARK_CAP) {
                viewModelScope.launch {
                    _effects.send(ProfileEffect.ShowPaywall(entryPoint = "bookmark_cap"))
                }
                return
            }
        }

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

    private fun loadSpots(reset: Boolean) {
        viewModelScope.launch {
            val userId = sessionBridge.currentUserId
                ?: run {
                    _uiState.update {
                        it.copy(
                            loadState = ProfileEngagementLoadState.ERROR,
                            errorMessage = "Not signed in",
                        )
                    }
                    return@launch
                }

            if (reset) {
                _uiState.update {
                    it.copy(loadState = ProfileEngagementLoadState.LOADING, errorMessage = null)
                }
            } else {
                _uiState.update { it.copy(isLoadingMore = true) }
            }

            val currentOffset = if (reset) 0 else offset
            val kind = _uiState.value.kind
            val idsResult = when (kind) {
                ProfileEngagementKind.Likes -> profileRepository.getLikedSpotIds(
                    userId = userId,
                    offset = currentOffset,
                    limit = Constants.Pagination.DEFAULT_PAGE_SIZE,
                )
                ProfileEngagementKind.Bookmarks -> profileRepository.getBookmarkedSpotIds(
                    userId = userId,
                    offset = currentOffset,
                    limit = Constants.Pagination.DEFAULT_PAGE_SIZE,
                )
            }

            idsResult.fold(
                onSuccess = { spotIds ->
                    hasMore = spotIds.size >= Constants.Pagination.DEFAULT_PAGE_SIZE
                    offset = currentOffset + spotIds.size
                    profileRepository.hydrateSpots(spotIds).fold(
                        onSuccess = { spots ->
                            _uiState.update { state ->
                                val merged = if (reset) spots else state.spots + spots
                                state.copy(
                                    spots = merged,
                                    loadState = if (merged.isEmpty()) {
                                        ProfileEngagementLoadState.EMPTY
                                    } else {
                                        ProfileEngagementLoadState.READY
                                    },
                                    isLoadingMore = false,
                                    hasMore = hasMore,
                                )
                            }
                        },
                        onFailure = {
                            _uiState.update { state ->
                                state.copy(
                                    loadState = ProfileEngagementLoadState.ERROR,
                                    isLoadingMore = false,
                                    errorMessage = "Couldn't load spots",
                                )
                            }
                        },
                    )
                },
                onFailure = {
                    logger.w(LogCategory.Network, TAG, "Load engagement ids failed", it)
                    _uiState.update { state ->
                        state.copy(
                            loadState = ProfileEngagementLoadState.ERROR,
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
        const val TAG = "ProfileEngagementViewModel"
    }
}
