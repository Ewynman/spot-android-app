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
 * ViewModel for the collections list screen (Pro feature).
 * 
 * Lists all collections and allows creating/deleting collections.
 * 
 * Reference: PRD/10-profile-social.md, PRD/12-pro-subscription.md
 */
@HiltViewModel
class CollectionsListViewModel @Inject constructor(
    private val collectionsRepository: CollectionsRepository,
    private val logger: SpotLogger,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CollectionsListUiState())
    val uiState: StateFlow<CollectionsListUiState> = _uiState.asStateFlow()

    private val _effects = Channel<CollectionsEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var initialLoadStarted = false

    fun onFirstAppear() {
        if (initialLoadStarted) return
        initialLoadStarted = true
        loadCollections()
    }

    fun refresh() {
        loadCollections()
    }

    fun createCollection(name: String) {
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
                    _uiState.update { state ->
                        state.copy(
                            collections = state.collections + collection,
                            isCreatingCollection = false,
                        )
                    }
                    _effects.send(CollectionsEffect.ShowToast("Collection created"))
                },
                onFailure = { error ->
                    logger.e(LogCategory.Network, TAG, "Failed to create collection", error)
                    _uiState.update { it.copy(isCreatingCollection = false) }
                    _effects.send(CollectionsEffect.ShowToast("Failed to create collection"))
                },
            )
        }
    }

    fun deleteCollection(collectionId: String) {
        viewModelScope.launch {
            collectionsRepository.deleteCollection(collectionId).fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            collections = state.collections.filterNot { it.id == collectionId },
                        )
                    }
                    _effects.send(CollectionsEffect.ShowToast("Collection deleted"))
                },
                onFailure = { error ->
                    logger.e(LogCategory.Network, TAG, "Failed to delete collection", error)
                    _effects.send(CollectionsEffect.ShowToast("Failed to delete collection"))
                },
            )
        }
    }

    fun onCollectionSelected(collectionId: String) {
        viewModelScope.launch {
            _effects.send(CollectionsEffect.NavigateToCollection(collectionId))
        }
    }

    private fun loadCollections() {
        _uiState.update { it.copy(loadState = CollectionsLoadState.LOADING, errorMessage = null) }
        viewModelScope.launch {
            collectionsRepository.getCollections().fold(
                onSuccess = { collections ->
                    _uiState.update { state ->
                        state.copy(
                            loadState = if (collections.isEmpty()) {
                                CollectionsLoadState.EMPTY
                            } else {
                                CollectionsLoadState.READY
                            },
                            collections = collections,
                        )
                    }
                },
                onFailure = { error ->
                    logger.e(LogCategory.Network, TAG, "Failed to load collections", error)
                    _uiState.update { state ->
                        state.copy(
                            loadState = CollectionsLoadState.ERROR,
                            errorMessage = "Couldn't load collections",
                        )
                    }
                },
            )
        }
    }

    private companion object {
        const val TAG = "CollectionsListViewModel"
    }
}
