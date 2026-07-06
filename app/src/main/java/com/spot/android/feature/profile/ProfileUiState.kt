package com.spot.android.feature.profile

import com.spot.android.data.model.FollowRelationship
import com.spot.android.data.model.Spot
import com.spot.android.data.model.User

enum class ProfileLoadState {
    LOADING,
    READY,
    ERROR,
    EMPTY,
}

enum class ProfileTab {
    Spots,
    Map,
}

enum class ProfileScreenMode {
    Main,
    ExpandedSpot,
    FollowRequests,
    Likes,
    Bookmarks,
    Collections,
    CollectionDetail,
}

enum class ProfileOverflowAction {
    GoPro,
    YourLikes,
    YourBookmarks,
    FollowRequests,
    Settings,
    ReportUser,
    BlockUser,
}

data class ProfileUiState(
    val mode: ProfileScreenMode = ProfileScreenMode.Main,
    val loadState: ProfileLoadState = ProfileLoadState.LOADING,
    val user: User? = null,
    val spots: List<Spot> = emptyList(),
    val selectedTab: ProfileTab = ProfileTab.Spots,
    val expandedSpot: Spot? = null,
    val followRelationship: FollowRelationship = FollowRelationship.Self,
    val isFollowActionInProgress: Boolean = false,
    val pendingFollowRequestCount: Int = 0,
    val isLoadingMoreSpots: Boolean = false,
    val hasMoreSpots: Boolean = true,
    val errorMessage: String? = null,
    val successToast: String? = null,
    val showDeleteSpotDialog: Boolean = false,
    val spotPendingDelete: Spot? = null,
    val showOverflowMenu: Boolean = false,
    val scrollToTopTrigger: Int = 0,
    val selectedCollectionId: String? = null,
)

sealed interface ProfileEffect {
    data class ShowPaywall(val entryPoint: String) : ProfileEffect
    data object OpenSettings : ProfileEffect
}
