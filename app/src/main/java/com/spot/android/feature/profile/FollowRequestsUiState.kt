package com.spot.android.feature.profile

import com.spot.android.data.model.FollowRequest

enum class FollowRequestsLoadState {
    LOADING,
    READY,
    EMPTY,
    ERROR,
}

data class FollowRequestsUiState(
    val loadState: FollowRequestsLoadState = FollowRequestsLoadState.LOADING,
    val requests: List<FollowRequest> = emptyList(),
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val errorMessage: String? = null,
    val actionInProgressIds: Set<String> = emptySet(),
)
