package com.spot.android.feature.post

import com.spot.android.data.location.PlaceSuggestion
import com.spot.android.data.post.PostDraftStep
import com.spot.android.data.post.ProcessedImage

/**
 * UI state for the 3-step post composer.
 *
 * Reference: PRD/08-post-flow.md
 */
enum class PostEntryState {
    CHECKING_EMAIL,
    EMAIL_NOT_VERIFIED,
    COMPOSER,
}

data class PostUiState(
    val entryState: PostEntryState = PostEntryState.CHECKING_EMAIL,
    val currentStep: PostDraftStep = PostDraftStep.PHOTOS,
    val images: List<ProcessedImage> = emptyList(),
    val selectedPlace: PlaceSuggestion? = null,
    val selectedVibes: Set<String> = emptySet(),
    val customVibeInput: String = "",
    val isPro: Boolean = false,
    val isProcessingImages: Boolean = false,
    val isPublishing: Boolean = false,
    val validationMessage: String? = null,
    val showDraftsSheet: Boolean = false,
    val showPostingRulesSheet: Boolean = false,
    val showSaveDraftDialog: Boolean = false,
    val saveDraftName: String = "",
    val drafts: List<com.spot.android.data.post.PostDraftSummary> = emptyList(),
    val locationQuery: String = "",
    val locationSuggestions: List<PlaceSuggestion> = emptyList(),
    val isSearchingLocation: Boolean = false,
    val hasAcceptedPostingRules: Boolean = false,
    val pendingVerificationEmail: String? = null,
)

sealed interface PostEffect {
    data object NavigateToHome : PostEffect
    data class ShowPaywall(val entryPoint: String) : PostEffect
    data class ShowToast(val message: String, val isError: Boolean = false) : PostEffect
    data object OpenConfirmEmail : PostEffect
}
