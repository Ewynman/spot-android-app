package com.spot.android.feature.home

import com.spot.android.data.feed.HomeFeedEmptyReason

/**
 * Feed load state machine mirroring iOS `FeedLoadState`.
 *
 * Reference: PRD/06-home-feed.md
 */
enum class FeedLoadState {
    IDLE,
    LOADING_INITIAL,
    LOADING_MORE,
    LOADED,
    EMPTY,
    ERROR,
}

data class HomeFeedUiState(
    val loadState: FeedLoadState = FeedLoadState.IDLE,
    val spots: List<com.spot.android.data.model.Spot> = emptyList(),
    val emptyReason: HomeFeedEmptyReason? = null,
    val errorToast: String? = null,
    val scrollToTopTrigger: Int = 0,
    val isRefreshing: Boolean = false,
)

sealed interface HomeFeedEffect {
    data class ShowPaywall(val entryPoint: String) : HomeFeedEffect
}
