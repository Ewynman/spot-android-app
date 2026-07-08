package com.spot.android.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spot.android.core.logging.LogCategory
import com.spot.android.core.logging.SpotLogger
import com.spot.android.core.supabase.SupabaseClientProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject

/**
 * ViewModel for Algorithm Snapshot debug screen.
 *
 * Reference: PRD/11-settings.md
 */
@HiltViewModel
class AlgorithmSnapshotViewModel @Inject constructor(
    private val supabaseClientProvider: SupabaseClientProvider,
    private val logger: SpotLogger,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlgorithmSnapshotUiState())
    val uiState: StateFlow<AlgorithmSnapshotUiState> = _uiState.asStateFlow()

    fun onFirstAppear() {
        loadFeedProfile()
    }

    fun onRefresh() {
        loadFeedProfile()
    }

    private fun loadFeedProfile() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val result = supabaseClientProvider.client
                    .from("user_feed_profiles")
                    .select(columns = Columns.list("profile")) {
                        limit(1)
                    }
                    .decodeSingle<FeedProfileRow>()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        profile = result.profile,
                    )
                }
            } catch (e: Exception) {
                logger.e(LogCategory.Network, "Failed to load feed profile", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load feed profile: ${e.message}",
                    )
                }
            }
        }
    }
}

@kotlinx.serialization.Serializable
private data class FeedProfileRow(
    val profile: JsonObject,
)

data class AlgorithmSnapshotUiState(
    val isLoading: Boolean = true,
    val profile: JsonObject? = null,
    val error: String? = null,
)
