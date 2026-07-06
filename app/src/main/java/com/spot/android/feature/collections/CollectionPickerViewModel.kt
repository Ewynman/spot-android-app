package com.spot.android.feature.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spot.android.core.logging.LogCategory
import com.spot.android.core.logging.SpotLogger
import com.spot.android.data.collections.CollectionsRepository
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
 * ViewModel for the collection picker sheet.
 * 
 * Shown when a Pro user bookmarks a spot to choose which collection(s) to add it to.
 * 
 * Reference: PRD/10-profile-social.md, PRD/12-pro-subscription.md
 */
@HiltViewModel
class CollectionPickerViewModel @Inject constructor(
    private val collectionsRepository: CollectionsRepository,
    private val logger: SpotLogger,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CollectionPickerUiState())
    val uiState: StateFlow<CollectionPickerUiState> = _uiState.asStateFlow()

    private val _effects = Channel<CollectionsEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var currentSpotId: String? = null

    fun configure(spotId: String) {
        if (currentSpotId == spotId) return
        currentSpotId = spotId
        loadCollections()
        loadSelectedCollections(spotId)
    }

    fun toggleCollection(collectionId: String, spotId: String) {
        val isCurrentlySelected = _uiState.value.selectedCollectionIds.contains(collectionId)

        _uiState.update { state ->
            val newSelected = if (isCurrentlySelected) {
                state.selectedCollectionIds - collectionId
            } else {
                state.selectedCollectionIds + collectionId
            }
            state.copy(selectedCollectionIds = newSelected)
        }

        viewModelScope.launch {
            val result = if (isCurrentlySelected) {
                collectionsRepository.removeSpotFromCollection(collectionId, spotId)
            } else {
                collectionsRepository.addSpotToCollection(collectionId, spotId)
            }
            result.onFailure { error ->
                logger.e(LogCategory.Network, TAG, "Failed to toggle collection", error)
                _uiState.update { state ->
                    val newSelected = if (isCurrentlySelected) {
                        state.selectedCollectionIds + collectionId
                    } else {
                        state.selectedCollectionIds - collectionId
                    }
                    state.copy(selectedCollectionIds = newSelected)
                }
                _effects.send(CollectionsEffect.ShowToast("Failed to update collection"))
            }
        }
    }

    fun createCollection(name: String, spotId: String) {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            viewModelScope.launch {
                _effects.send(CollectionsEffect.ShowToast("Collection name cannot be empty"))
            }
            return
        }

        _uiState.update { it.copy(isCreatingCollection = true) }
        viewModelScope.launch {
            collectionsRepository.createCollection(trimmedName).fold(
                onSuccess = { collection ->
                    collectionsRepository.addSpotToCollection(collection.id, spotId).fold(
                        onSuccess = {
                            _uiState.update { state ->
                                state.copy(
                                    collections = state.collections + collection,
                                    selectedCollectionIds = state.selectedCollectionIds + collection.id,
                                    isCreatingCollection = false,
                                )
                            }
                            _effects.send(CollectionsEffect.ShowToast("Collection created"))
                        },
                        onFailure = { error ->
                            logger.e(LogCategory.Network, TAG, "Failed to add to new collection", error)
                            _uiState.update { state ->
                                state.copy(
                                    collections = state.collections + collection,
                                    isCreatingCollection = false,
                                )
                            }
                            _effects.send(CollectionsEffect.ShowToast("Collection created"))
                        },
                    )
                },
                onFailure = { error ->
                    logger.e(LogCategory.Network, TAG, "Failed to create collection", error)
                    _uiState.update { it.copy(isCreatingCollection = false) }
                    _effects.send(CollectionsEffect.ShowToast("Failed to create collection"))
                },
            )
        }
    }

    private fun loadCollections() {
        _uiState.update { it.copy(loadState = CollectionPickerLoadState.LOADING, errorMessage = null) }
        viewModelScope.launch {
            collectionsRepository.getCollections().fold(
                onSuccess = { collections ->
                    _uiState.update { state ->
                        state.copy(
                            loadState = if (collections.isEmpty()) {
                                CollectionPickerLoadState.EMPTY
                            } else {
                                CollectionPickerLoadState.READY
                            },
                            collections = collections,
                        )
                    }
                },
                onFailure = { error ->
                    logger.e(LogCategory.Network, TAG, "Failed to load collections", error)
                    _uiState.update { state ->
                        state.copy(
                            loadState = CollectionPickerLoadState.ERROR,
                            errorMessage = "Couldn't load collections",
                        )
                    }
                },
            )
        }
    }

    private fun loadSelectedCollections(spotId: String) {
        viewModelScope.launch {
            collectionsRepository.getCollectionsContainingSpot(spotId).fold(
                onSuccess = { collectionIds ->
                    _uiState.update { it.copy(selectedCollectionIds = collectionIds.toSet()) }
                },
                onFailure = { error ->
                    logger.e(LogCategory.Network, TAG, "Failed to load selected collections", error)
                },
            )
        }
    }

    private companion object {
        const val TAG = "CollectionPickerViewModel"
    }
}
