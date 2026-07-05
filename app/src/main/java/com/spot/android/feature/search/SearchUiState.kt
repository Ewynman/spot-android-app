package com.spot.android.feature.search

import com.spot.android.data.model.Spot
import com.spot.android.data.model.User
import com.spot.android.data.model.VibeTag
import com.spot.android.data.search.SearchGridRequest
import com.spot.android.data.search.SearchHistoryItem
import com.spot.android.data.search.SearchSegment

enum class SearchScreenMode {
    Main,
    Grid,
    ExpandedSpot,
    UserProfile,
}

enum class SearchLoadState {
    Idle,
    Loading,
    Loaded,
    Empty,
    Error,
}

data class SearchGridUiState(
    val title: String,
    val request: SearchGridRequest,
    val isVibeGrid: Boolean,
    val spots: List<Spot> = emptyList(),
    val loadState: SearchLoadState = SearchLoadState.Idle,
    val nextOffset: Int = 0,
    val hasMore: Boolean = false,
    val isLoadingMore: Boolean = false,
    val selectedVibeFilterIds: Set<String> = emptySet(),
    val showVibeFilterSheet: Boolean = false,
)

data class SearchUiState(
    val mode: SearchScreenMode = SearchScreenMode.Main,
    val segment: SearchSegment = SearchSegment.Users,
    val query: String = "",
    val loadState: SearchLoadState = SearchLoadState.Idle,
    val users: List<User> = emptyList(),
    val locations: List<String> = emptyList(),
    val vibes: List<VibeTag> = emptyList(),
    val history: List<SearchHistoryItem> = emptyList(),
    val grid: SearchGridUiState? = null,
    val expandedSpot: Spot? = null,
    val selectedUser: User? = null,
    val errorToast: String? = null,
    val isPro: Boolean = false,
    val availableVibeTags: List<VibeTag> = emptyList(),
)

sealed interface SearchEffect {
    data class ShowPaywall(val entryPoint: String) : SearchEffect
}
