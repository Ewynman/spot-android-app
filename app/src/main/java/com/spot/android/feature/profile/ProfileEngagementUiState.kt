package com.spot.android.feature.profile

import com.spot.android.data.model.Spot

enum class ProfileEngagementKind {
    Likes,
    Bookmarks,
}

enum class ProfileEngagementLoadState {
    LOADING,
    READY,
    EMPTY,
    ERROR,
}

data class ProfileEngagementUiState(
    val kind: ProfileEngagementKind,
    val loadState: ProfileEngagementLoadState = ProfileEngagementLoadState.LOADING,
    val spots: List<Spot> = emptyList(),
    val expandedSpot: Spot? = null,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val errorMessage: String? = null,
)
