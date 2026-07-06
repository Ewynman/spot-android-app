package com.spot.android.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spot.android.core.logging.LogCategory
import com.spot.android.core.logging.SpotLogger
import com.spot.android.core.util.Constants
import com.spot.android.data.auth.UserSessionHolder
import com.spot.android.data.content.LocalContentRemovalBus
import com.spot.android.data.feed.EngagementRepository
import com.spot.android.data.feed.FeedEventService
import com.spot.android.data.model.FollowRelationship
import com.spot.android.data.model.Spot
import com.spot.android.data.model.User
import com.spot.android.data.model.enums.FeedEventType
import com.spot.android.data.profile.FollowRepository
import com.spot.android.data.profile.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * ViewModel for profile screens (own tab and other-user overlays).
 *
 * Reference: PRD/10-profile-social.md
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val followRepository: FollowRepository,
    private val engagementRepository: EngagementRepository,
    private val feedEventService: FeedEventService,
    private val userSessionHolder: UserSessionHolder,
    private val localContentRemovalBus: LocalContentRemovalBus,
    private val logger: SpotLogger,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _effects = Channel<ProfileEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var targetUserId: String? = null
    private var spotOffset = 0
    private var hasMoreSpots = true
    private var initialLoadStarted = false
    private var badgePollJob: Job? = null
    private var spotLoadJob: Job? = null

    fun configure(userId: String?) {
        if (targetUserId == userId && initialLoadStarted) return
        targetUserId = userId
        initialLoadStarted = false
        spotOffset = 0
        hasMoreSpots = true
        _uiState.value = ProfileUiState()
        onFirstAppear()
    }

    fun onFirstAppear() {
        if (initialLoadStarted) return
        initialLoadStarted = true
        loadProfile(resetSpots = true)
        startBadgePollingIfNeeded()
    }

    fun onTabReselected() {
        _uiState.update { it.copy(scrollToTopTrigger = it.scrollToTopTrigger + 1) }
        refresh()
    }

    fun refresh() {
        spotLoadJob?.cancel()
        spotOffset = 0
        hasMoreSpots = true
        loadProfile(resetSpots = true)
    }

    fun onTabSelected(tab: ProfileTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun onSpotSelected(spot: Spot) {
        feedEventService.recordEvent(
            spotId = spot.id,
            eventType = FeedEventType.DETAIL_OPEN,
        )
        _uiState.update {
            it.copy(
                mode = ProfileScreenMode.ExpandedSpot,
                expandedSpot = spot,
            )
        }
    }

    fun onBackFromExpandedSpot() {
        _uiState.update {
            it.copy(
                mode = ProfileScreenMode.Main,
                expandedSpot = null,
            )
        }
    }

    fun onOverflowClick() {
        _uiState.update { it.copy(showOverflowMenu = true) }
    }

    fun dismissOverflowMenu() {
        _uiState.update { it.copy(showOverflowMenu = false) }
    }

    fun onOverflowAction(action: ProfileOverflowAction) {
        dismissOverflowMenu()
        when (action) {
            ProfileOverflowAction.GoPro -> {
                viewModelScope.launch {
                    _effects.send(ProfileEffect.ShowPaywall(entryPoint = "profile_menu"))
                }
            }
            ProfileOverflowAction.YourLikes -> {
                _uiState.update { it.copy(mode = ProfileScreenMode.Likes) }
            }
            ProfileOverflowAction.YourBookmarks -> {
                val isPro = userSessionHolder.isPro.value
                _uiState.update {
                    it.copy(mode = if (isPro) ProfileScreenMode.Collections else ProfileScreenMode.Bookmarks)
                }
            }
            ProfileOverflowAction.FollowRequests -> {
                _uiState.update { it.copy(mode = ProfileScreenMode.FollowRequests) }
            }
            ProfileOverflowAction.Settings -> {
                viewModelScope.launch { _effects.send(ProfileEffect.OpenSettings) }
            }
            ProfileOverflowAction.ReportUser,
            ProfileOverflowAction.BlockUser,
            -> Unit
        }
    }

    fun onBackFromSubScreen() {
        _uiState.update {
            it.copy(
                mode = ProfileScreenMode.Main,
                expandedSpot = null,
                selectedCollectionId = null,
            )
        }
        refreshPendingFollowRequestCount()
    }

    fun onCollectionSelected(collectionId: String) {
        _uiState.update {
            it.copy(
                mode = ProfileScreenMode.CollectionDetail,
                selectedCollectionId = collectionId,
            )
        }
    }

    fun onBackFromCollectionDetail() {
        _uiState.update {
            it.copy(
                mode = ProfileScreenMode.Collections,
                selectedCollectionId = null,
            )
        }
    }

    fun onFollowButtonClick() {
        val user = _uiState.value.user ?: return
        if (_uiState.value.isFollowActionInProgress) return

        val relationship = _uiState.value.followRelationship
        _uiState.update { it.copy(isFollowActionInProgress = true) }

        viewModelScope.launch {
            val result = when (relationship) {
                FollowRelationship.NotFollowing -> followRepository.followPublicUser(user.id)
                FollowRelationship.Following,
                FollowRelationship.FollowingPrivate,
                -> followRepository.unfollowUser(user.id)
                FollowRelationship.CanRequest -> followRepository.requestFollow(user.id)
                FollowRelationship.Requested -> followRepository.cancelFollowRequest(user.id)
                FollowRelationship.Self -> Result.success(Unit)
            }

            result.fold(
                onSuccess = {
                    emitFollowFeedEvent(relationship)
                    refreshFollowRelationship(user.id)
                },
                onFailure = {
                    logger.w(LogCategory.Network, TAG, "Follow action failed", it)
                    _uiState.update { state ->
                        state.copy(errorMessage = "Couldn't update follow status")
                    }
                },
            )
            _uiState.update { it.copy(isFollowActionInProgress = false) }
        }
    }

    fun loadMoreSpotsIfNeeded(lastVisibleIndex: Int) {
        val state = _uiState.value
        if (state.mode != ProfileScreenMode.Main ||
            state.selectedTab != ProfileTab.Spots ||
            state.isLoadingMoreSpots ||
            !hasMoreSpots
        ) {
            return
        }

        val threshold = state.spots.lastIndex - 4
        if (lastVisibleIndex >= threshold) {
            spotLoadJob?.cancel()
            spotLoadJob = viewModelScope.launch {
                loadSpots(append = true)
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

        updateSpot(spot.id) { it.copy(isLiked = !currentlyLiked, likes = optimisticCount) }

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
                updateSpot(spot.id) { s -> s.copy(isLiked = currentlyLiked, likes = spot.likes) }
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

    fun requestDeleteSpot(spot: Spot) {
        _uiState.update {
            it.copy(
                showDeleteSpotDialog = true,
                spotPendingDelete = spot,
            )
        }
    }

    fun dismissDeleteSpotDialog() {
        _uiState.update {
            it.copy(
                showDeleteSpotDialog = false,
                spotPendingDelete = null,
            )
        }
    }

    fun confirmDeleteSpot() {
        val spot = _uiState.value.spotPendingDelete ?: return
        dismissDeleteSpotDialog()

        val previousSpots = _uiState.value.spots
        _uiState.update { state ->
            state.copy(
                spots = state.spots.filterNot { it.id == spot.id },
                expandedSpot = state.expandedSpot?.takeIf { it.id != spot.id },
                successToast = "Spot deleted",
            )
        }

        viewModelScope.launch {
            profileRepository.deleteOwnSpot(spot.id).onFailure {
                logger.w(LogCategory.Network, TAG, "Delete spot failed; rolling back", it)
                _uiState.update { state ->
                    state.copy(
                        spots = previousSpots,
                        errorMessage = "Couldn't delete spot",
                        successToast = null,
                    )
                }
            }
            localContentRemovalBus.removeBySpotId(spot.id)
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearSuccessToast() {
        _uiState.update { it.copy(successToast = null) }
    }

    fun onBadgePollingVisible(isVisible: Boolean) {
        if (isVisible) startBadgePollingIfNeeded() else badgePollJob?.cancel()
    }

    override fun onCleared() {
        badgePollJob?.cancel()
        super.onCleared()
    }

    private fun loadProfile(resetSpots: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    loadState = if (resetSpots) ProfileLoadState.LOADING else it.loadState,
                    errorMessage = null,
                )
            }

            val profileResult = if (targetUserId == null) {
                profileRepository.getOwnProfile()
            } else {
                profileRepository.getPublicProfile(targetUserId!!)
            }

            profileResult.fold(
                onSuccess = { user ->
                    _uiState.update { it.copy(user = user) }
                    if (user.isCurrentUser) {
                        refreshPendingFollowRequestCount()
                    } else {
                        refreshFollowRelationship(user.id)
                    }
                    if (resetSpots) {
                        spotOffset = 0
                        hasMoreSpots = true
                        loadSpots(append = false)
                    }
                },
                onFailure = {
                    logger.w(LogCategory.Network, TAG, "Profile load failed", it)
                    _uiState.update { state ->
                        state.copy(
                            loadState = ProfileLoadState.ERROR,
                            errorMessage = "Couldn't load profile",
                        )
                    }
                },
            )
        }
    }

    private suspend fun loadSpots(append: Boolean) {
        val user = _uiState.value.user ?: return
        if (!append) {
            _uiState.update { it.copy(loadState = ProfileLoadState.LOADING) }
        } else {
            _uiState.update { it.copy(isLoadingMoreSpots = true) }
        }

        val offset = if (append) spotOffset else 0
        val idsResult = profileRepository.getUserSpotIds(
            userId = user.id,
            offset = offset,
            limit = Constants.Pagination.DEFAULT_PAGE_SIZE,
        )

        idsResult.fold(
            onSuccess = { spotIds ->
                hasMoreSpots = spotIds.size >= Constants.Pagination.DEFAULT_PAGE_SIZE
                spotOffset = offset + spotIds.size

                val spotsResult = profileRepository.hydrateSpots(spotIds)
                spotsResult.fold(
                    onSuccess = { spots ->
                        _uiState.update { state ->
                            val merged = if (append) state.spots + spots else spots
                            state.copy(
                                spots = merged,
                                loadState = if (merged.isEmpty()) ProfileLoadState.EMPTY else ProfileLoadState.READY,
                                isLoadingMoreSpots = false,
                                hasMoreSpots = hasMoreSpots,
                            )
                        }
                    },
                    onFailure = {
                        _uiState.update { state ->
                            state.copy(
                                loadState = ProfileLoadState.ERROR,
                                isLoadingMoreSpots = false,
                                errorMessage = "Couldn't load spots",
                            )
                        }
                    },
                )
            },
            onFailure = {
                _uiState.update { state ->
                    state.copy(
                        loadState = ProfileLoadState.ERROR,
                        isLoadingMoreSpots = false,
                        errorMessage = "Couldn't load spots",
                    )
                }
            },
        )
    }

    private fun refreshFollowRelationship(userId: String) {
        viewModelScope.launch {
            followRepository.getFollowRelationship(userId).onSuccess { relationship ->
                _uiState.update { it.copy(followRelationship = relationship) }
            }
        }
    }

    private fun refreshPendingFollowRequestCount() {
        viewModelScope.launch {
            followRepository.getPendingIncomingCount().onSuccess { count ->
                _uiState.update { it.copy(pendingFollowRequestCount = count) }
            }
        }
    }

    private fun startBadgePollingIfNeeded() {
        val user = _uiState.value.user
        if (targetUserId != null || user?.isPrivate != true) return

        badgePollJob?.cancel()
        badgePollJob = viewModelScope.launch {
            while (isActive) {
                refreshPendingFollowRequestCount()
                delay(Constants.Social.FOLLOW_REQUESTS_BADGE_POLL_INTERVAL_SECONDS * 1_000L)
            }
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

    private fun emitFollowFeedEvent(previousRelationship: FollowRelationship) {
        val spotId = feedEventSpotContext() ?: return
        val eventType = when (previousRelationship) {
            FollowRelationship.NotFollowing,
            FollowRelationship.CanRequest,
            -> FeedEventType.FOLLOW_AUTHOR
            FollowRelationship.Following,
            FollowRelationship.FollowingPrivate,
            -> FeedEventType.UNFOLLOW_AUTHOR
            FollowRelationship.Requested,
            FollowRelationship.Self,
            -> return
        }
        feedEventService.recordEvent(
            spotId = spotId,
            eventType = eventType,
        )
    }

    private fun feedEventSpotContext(): String? {
        val state = _uiState.value
        return state.expandedSpot?.id ?: state.spots.firstOrNull()?.id
    }

    private companion object {
        const val TAG = "ProfileViewModel"
    }
}
