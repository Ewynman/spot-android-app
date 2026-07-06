package com.spot.android.feature.collections

import com.spot.android.data.model.BookmarkCollection
import com.spot.android.data.model.Spot

/**
 * UI state for the collections list screen.
 * 
 * Reference: PRD/10-profile-social.md, PRD/12-pro-subscription.md
 */
data class CollectionsListUiState(
    val loadState: CollectionsLoadState = CollectionsLoadState.LOADING,
    val collections: List<BookmarkCollection> = emptyList(),
    val isCreatingCollection: Boolean = false,
    val errorMessage: String? = null,
)

enum class CollectionsLoadState {
    LOADING,
    READY,
    EMPTY,
    ERROR,
}

/**
 * UI state for viewing a specific collection's spots.
 */
data class CollectionDetailUiState(
    val collection: BookmarkCollection? = null,
    val loadState: CollectionDetailLoadState = CollectionDetailLoadState.LOADING,
    val spots: List<Spot> = emptyList(),
    val expandedSpot: Spot? = null,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val errorMessage: String? = null,
    val isEditingName: Boolean = false,
)

enum class CollectionDetailLoadState {
    LOADING,
    READY,
    EMPTY,
    ERROR,
}

/**
 * UI state for the collection picker sheet.
 * 
 * Shown when bookmarking a spot to choose which collection to add it to.
 */
data class CollectionPickerUiState(
    val loadState: CollectionPickerLoadState = CollectionPickerLoadState.LOADING,
    val collections: List<BookmarkCollection> = emptyList(),
    val selectedCollectionIds: Set<String> = emptySet(),
    val isCreatingCollection: Boolean = false,
    val errorMessage: String? = null,
)

enum class CollectionPickerLoadState {
    LOADING,
    READY,
    EMPTY,
    ERROR,
}

/**
 * One-shot effects for the collections feature.
 */
sealed interface CollectionsEffect {
    data class ShowToast(val message: String) : CollectionsEffect
    data class NavigateToCollection(val collectionId: String) : CollectionsEffect
    data class ShowPaywall(val entryPoint: String) : CollectionsEffect
}
