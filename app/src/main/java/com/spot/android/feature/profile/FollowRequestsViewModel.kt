package com.spot.android.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spot.android.core.logging.LogCategory
import com.spot.android.core.logging.SpotLogger
import com.spot.android.core.util.Constants
import com.spot.android.data.profile.FollowRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the follow-requests screen.
 *
 * Reference: PRD/10-profile-social.md
 */
@HiltViewModel
class FollowRequestsViewModel @Inject constructor(
    private val followRepository: FollowRepository,
    private val logger: SpotLogger,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FollowRequestsUiState())
    val uiState: StateFlow<FollowRequestsUiState> = _uiState.asStateFlow()

    private var offset = 0
    private var hasMore = true
    private var initialLoadStarted = false

    fun onFirstAppear() {
        if (initialLoadStarted) return
        initialLoadStarted = true
        loadRequests(reset = true)
    }

    fun refresh() {
        offset = 0
        hasMore = true
        loadRequests(reset = true, isRefresh = true)
    }

    fun loadMoreIfNeeded(lastVisibleIndex: Int) {
        val state = _uiState.value
        if (state.isLoadingMore || !hasMore) return
        if (lastVisibleIndex >= state.requests.lastIndex - 4) {
            loadRequests(reset = false)
        }
    }

    fun acceptRequest(requestId: String, requesterId: String) {
        if (_uiState.value.actionInProgressIds.contains(requestId)) return
        markActionInProgress(requestId, true)

        viewModelScope.launch {
            followRepository.acceptFollowRequest(requestId, requesterId).fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            requests = state.requests.filterNot { it.id == requestId },
                            loadState = if (state.requests.size <= 1) {
                                FollowRequestsLoadState.EMPTY
                            } else {
                                state.loadState
                            },
                        )
                    }
                },
                onFailure = {
                    logger.w(LogCategory.Network, TAG, "Accept follow request failed", it)
                    _uiState.update { state -> state.copy(errorMessage = "Couldn't accept request") }
                },
            )
            markActionInProgress(requestId, false)
        }
    }

    fun denyRequest(requestId: String) {
        if (_uiState.value.actionInProgressIds.contains(requestId)) return
        markActionInProgress(requestId, true)

        viewModelScope.launch {
            followRepository.denyFollowRequest(requestId).fold(
                onSuccess = {
                    _uiState.update { state ->
                        val remaining = state.requests.filterNot { it.id == requestId }
                        state.copy(
                            requests = remaining,
                            loadState = if (remaining.isEmpty()) {
                                FollowRequestsLoadState.EMPTY
                            } else {
                                state.loadState
                            },
                        )
                    }
                },
                onFailure = {
                    logger.w(LogCategory.Network, TAG, "Deny follow request failed", it)
                    _uiState.update { state -> state.copy(errorMessage = "Couldn't deny request") }
                },
            )
            markActionInProgress(requestId, false)
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun loadRequests(reset: Boolean, isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (reset) {
                _uiState.update {
                    it.copy(
                        loadState = FollowRequestsLoadState.LOADING,
                        isRefreshing = isRefresh,
                        errorMessage = null,
                    )
                }
            } else {
                _uiState.update { it.copy(isLoadingMore = true) }
            }

            val currentOffset = if (reset) 0 else offset
            followRepository.getPendingIncomingRequests(
                offset = currentOffset,
                limit = Constants.Social.FOLLOW_REQUESTS_PAGE_SIZE,
            ).fold(
                onSuccess = { requests ->
                    hasMore = requests.size >= Constants.Social.FOLLOW_REQUESTS_PAGE_SIZE
                    offset = currentOffset + requests.size
                    _uiState.update { state ->
                        val merged = if (reset) requests else state.requests + requests
                        state.copy(
                            requests = merged,
                            loadState = if (merged.isEmpty()) {
                                FollowRequestsLoadState.EMPTY
                            } else {
                                FollowRequestsLoadState.READY
                            },
                            isRefreshing = false,
                            isLoadingMore = false,
                            hasMore = hasMore,
                        )
                    }
                },
                onFailure = {
                    logger.w(LogCategory.Network, TAG, "Load follow requests failed", it)
                    _uiState.update { state ->
                        state.copy(
                            loadState = FollowRequestsLoadState.ERROR,
                            isRefreshing = false,
                            isLoadingMore = false,
                            errorMessage = "Couldn't load follow requests",
                        )
                    }
                },
            )
        }
    }

    private fun markActionInProgress(requestId: String, inProgress: Boolean) {
        _uiState.update { state ->
            val ids = state.actionInProgressIds.toMutableSet()
            if (inProgress) ids.add(requestId) else ids.remove(requestId)
            state.copy(actionInProgressIds = ids)
        }
    }

    private companion object {
        const val TAG = "FollowRequestsViewModel"
    }
}
