package com.spot.android.feature.post

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spot.android.core.logging.LogCategory
import com.spot.android.core.logging.SpotLogger
import com.spot.android.core.supabase.SessionBridge
import com.spot.android.core.util.Constants
import com.spot.android.data.auth.AuthRepository
import com.spot.android.data.auth.UserSessionHolder
import com.spot.android.data.location.PlaceSearchProvider
import com.spot.android.data.location.PlaceSuggestion
import com.spot.android.data.post.AUTOSAVE_DRAFT_ID
import com.spot.android.data.post.ImageProcessor
import com.spot.android.data.post.PostDraft
import com.spot.android.data.post.PostDraftRepository
import com.spot.android.data.post.PostDraftStatus
import com.spot.android.data.post.PostDraftStep
import com.spot.android.data.post.ProcessedImage
import com.spot.android.data.post.PostingRulesStore
import com.spot.android.data.post.PublishJob
import com.spot.android.data.post.SpotPublishCoordinator
import com.spot.android.navigation.ShellNavigationBus
import com.spot.android.navigation.SpotTab
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the 3-step post composer and publish pipeline.
 *
 * Reference: PRD/08-post-flow.md
 */
@HiltViewModel
class PostViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionBridge: SessionBridge,
    private val userSessionHolder: UserSessionHolder,
    private val imageProcessor: ImageProcessor,
    private val postDraftRepository: PostDraftRepository,
    private val postingRulesStore: PostingRulesStore,
    private val spotPublishCoordinator: SpotPublishCoordinator,
    private val placeSearchProvider: PlaceSearchProvider,
    private val shellNavigationBus: ShellNavigationBus,
    private val logger: SpotLogger,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PostUiState())
    val uiState: StateFlow<PostUiState> = _uiState.asStateFlow()

    private val _effects = Channel<PostEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var locationSearchJob: Job? = null
    private var autosaveJob: Job? = null

    init {
        observeProStatus()
    }

    fun onFirstAppear() {
        viewModelScope.launch {
            _uiState.update { it.copy(entryState = PostEntryState.CHECKING_EMAIL) }
            val verified = authRepository.isCurrentEmailVerified()
            if (!verified) {
                _uiState.update {
                    it.copy(
                        entryState = PostEntryState.EMAIL_NOT_VERIFIED,
                        pendingVerificationEmail = sessionBridge.currentUserId?.let { null },
                    )
                }
                return@launch
            }

            val acceptedRules = postingRulesStore.hasAccepted()
            val autosave = postDraftRepository.loadDraft(AUTOSAVE_DRAFT_ID).getOrNull()
            val images = if (autosave != null) {
                postDraftRepository.loadDraftImages(AUTOSAVE_DRAFT_ID).getOrElse { emptyList() }
            } else {
                emptyList()
            }

            _uiState.update {
                it.copy(
                    entryState = PostEntryState.COMPOSER,
                    hasAcceptedPostingRules = acceptedRules,
                    currentStep = autosave?.draftStep ?: PostDraftStep.PHOTOS,
                    selectedVibes = autosave?.vibeTags?.toSet() ?: emptySet(),
                    selectedPlace = autosave?.toPlaceSuggestion(),
                    images = images,
                    isPro = userSessionHolder.isPro.value,
                )
            }
        }
    }

    fun onVerifyEmailClick() {
        viewModelScope.launch {
            _effects.send(PostEffect.OpenConfirmEmail)
        }
    }

    fun addImagesFromUris(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingImages = true, validationMessage = null) }

            val isPro = _uiState.value.isPro
            val maxImages = if (isPro) {
                Constants.PostLimits.PRO_MAX_IMAGES
            } else {
                Constants.PostLimits.FREE_MAX_IMAGES
            }

            var selectedUris = uris
            var message: String? = null
            if (!isPro && uris.size > 1) {
                selectedUris = uris.take(1)
                message = "Multiple images are available with Pro."
            } else if (isPro && uris.size > maxImages) {
                selectedUris = uris.take(maxImages)
                message = "You can add up to 5 images per post."
            }

            val processed = mutableListOf<ProcessedImage>()
            for (uri in selectedUris) {
                imageProcessor.processFromUri(uri)
                    .onSuccess { processed.add(it) }
                    .onFailure { error ->
                        logger.w(LogCategory.Post, TAG, "Image processing failed", error)
                    }
            }

            applyProcessedImages(processed, maxImages, message)
        }
    }

    fun addImagesFromBytes(bytes: ByteArray) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingImages = true, validationMessage = null) }
            val isPro = _uiState.value.isPro
            val maxImages = if (isPro) {
                Constants.PostLimits.PRO_MAX_IMAGES
            } else {
                Constants.PostLimits.FREE_MAX_IMAGES
            }

            val processed = imageProcessor.processFromBytes(bytes, "${UUID.randomUUID()}.jpg")
                .getOrNull()
                ?.let { listOf(it) }
                .orEmpty()

            applyProcessedImages(processed, maxImages, message = null)
        }
    }

    private fun applyProcessedImages(
        processed: List<ProcessedImage>,
        maxImages: Int,
        message: String?,
    ) {
        _uiState.update { state ->
            val merged = (state.images + processed).take(maxImages)
            state.copy(
                images = merged,
                isProcessingImages = false,
                validationMessage = message,
            )
        }
        scheduleAutosave()
    }

    fun removeImage(fileName: String) {
        _uiState.update { state ->
            state.copy(images = state.images.filterNot { it.fileName == fileName })
        }
        scheduleAutosave()
    }

    fun nextStep() {
        val state = _uiState.value
        if (!canProceed(state)) return

        val next = when (state.currentStep) {
            PostDraftStep.PHOTOS -> PostDraftStep.LOCATION
            PostDraftStep.LOCATION -> PostDraftStep.VIBES
            PostDraftStep.VIBES -> return
        }
        _uiState.update { it.copy(currentStep = next, validationMessage = null) }
        scheduleAutosave()

        if (next == PostDraftStep.LOCATION) {
            loadCurrentLocationSuggestion()
        }
    }

    fun previousStep() {
        val prev = when (_uiState.value.currentStep) {
            PostDraftStep.PHOTOS -> return
            PostDraftStep.LOCATION -> PostDraftStep.PHOTOS
            PostDraftStep.VIBES -> PostDraftStep.LOCATION
        }
        _uiState.update { it.copy(currentStep = prev, validationMessage = null) }
        scheduleAutosave()
    }

    fun updateLocationQuery(query: String) {
        _uiState.update { it.copy(locationQuery = query) }
        locationSearchJob?.cancel()
        if (query.length < 2) {
            _uiState.update { it.copy(locationSuggestions = emptyList(), isSearchingLocation = false) }
            return
        }
        locationSearchJob = viewModelScope.launch {
            _uiState.update { it.copy(isSearchingLocation = true) }
            delay(300)
            val results = placeSearchProvider.search(query).getOrElse { emptyList() }
            _uiState.update {
                it.copy(
                    locationSuggestions = results,
                    isSearchingLocation = false,
                )
            }
        }
    }

    fun selectPlace(place: PlaceSuggestion) {
        _uiState.update {
            it.copy(
                selectedPlace = place,
                locationQuery = place.name,
                locationSuggestions = emptyList(),
                validationMessage = null,
            )
        }
        scheduleAutosave()
    }

    fun setCustomPlaceName(name: String) {
        val current = _uiState.value.selectedPlace
        if (current == null) {
            _uiState.update { it.copy(validationMessage = "Select a location first.") }
            return
        }
        _uiState.update {
            it.copy(
                selectedPlace = current.copy(name = name.trim(), isCustomName = true),
                locationQuery = name.trim(),
            )
        }
        scheduleAutosave()
    }

    fun toggleVibe(vibe: String) {
        val isPro = _uiState.value.isPro
        val current = _uiState.value.selectedVibes
        if (vibe in current) {
            _uiState.update { it.copy(selectedVibes = current - vibe, validationMessage = null) }
        } else {
            val maxVibes = if (isPro) {
                Constants.PostLimits.PRO_MAX_VIBES
            } else {
                Constants.PostLimits.FREE_MAX_VIBES
            }
            if (!isPro && current.size >= 1) {
                _uiState.update {
                    it.copy(validationMessage = "Multiple vibes are available with Pro.")
                }
                return
            }
            if (isPro && current.size >= maxVibes) {
                _uiState.update {
                    it.copy(validationMessage = "You can select up to 5 vibes.")
                }
                return
            }
            _uiState.update { it.copy(selectedVibes = current + vibe, validationMessage = null) }
        }
        scheduleAutosave()
    }

    fun updateCustomVibeInput(input: String) {
        _uiState.update { it.copy(customVibeInput = input) }
    }

    fun addCustomVibe() {
        if (!_uiState.value.isPro) {
            viewModelScope.launch {
                _effects.send(PostEffect.ShowPaywall(entryPoint = "custom_vibe"))
            }
            return
        }

        val input = _uiState.value.customVibeInput.trim()
        when (val result = VibeTagValidator.validate(input)) {
            is VibeTagValidator.ValidationResult.Invalid -> {
                _uiState.update { it.copy(validationMessage = result.message) }
            }
            VibeTagValidator.ValidationResult.Valid -> toggleVibe(input)
        }
        _uiState.update { it.copy(customVibeInput = "") }
    }

    fun publish() {
        val state = _uiState.value
        if (!canProceed(state) || state.currentStep != PostDraftStep.VIBES) return

        if (!state.hasAcceptedPostingRules) {
            _uiState.update { it.copy(showPostingRulesSheet = true) }
            return
        }
        startPublish()
    }

    fun acceptPostingRules() {
        viewModelScope.launch {
            postingRulesStore.setAccepted(true)
            _uiState.update {
                it.copy(
                    hasAcceptedPostingRules = true,
                    showPostingRulesSheet = false,
                )
            }
            startPublish()
        }
    }

    fun dismissPostingRules() {
        _uiState.update { it.copy(showPostingRulesSheet = false) }
    }

    fun showDraftsSheet() {
        viewModelScope.launch {
            val drafts = postDraftRepository.listSummaries().getOrElse { emptyList() }
                .filter { it.id != AUTOSAVE_DRAFT_ID }
            _uiState.update { it.copy(showDraftsSheet = true, drafts = drafts) }
        }
    }

    fun dismissDraftsSheet() {
        _uiState.update { it.copy(showDraftsSheet = false) }
    }

    fun loadDraft(draftId: String) {
        viewModelScope.launch {
            val draft = postDraftRepository.loadDraft(draftId).getOrNull() ?: return@launch
            val images = postDraftRepository.loadDraftImages(draftId).getOrElse { emptyList() }
            _uiState.update {
                it.copy(
                    currentStep = draft.draftStep,
                    selectedVibes = draft.vibeTags.toSet(),
                    selectedPlace = draft.toPlaceSuggestion(),
                    images = images,
                    showDraftsSheet = false,
                    validationMessage = null,
                )
            }
        }
    }

    fun deleteDraft(draftId: String) {
        viewModelScope.launch {
            postDraftRepository.deleteDraft(draftId)
            val drafts = postDraftRepository.listSummaries().getOrElse { emptyList() }
                .filter { it.id != AUTOSAVE_DRAFT_ID }
            _uiState.update { it.copy(drafts = drafts) }
        }
    }

    fun showSaveDraftDialog() {
        _uiState.update { it.copy(showSaveDraftDialog = true, saveDraftName = "") }
    }

    fun dismissSaveDraftDialog() {
        _uiState.update { it.copy(showSaveDraftDialog = false) }
    }

    fun updateSaveDraftName(name: String) {
        _uiState.update { it.copy(saveDraftName = name) }
    }

    fun saveNamedDraft() {
        val name = _uiState.value.saveDraftName.trim()
        if (name.isEmpty()) return

        viewModelScope.launch {
            val draft = buildDraft(id = UUID.randomUUID().toString(), status = PostDraftStatus.SAVED)
            postDraftRepository.saveDraft(draft, _uiState.value.images)
            postDraftRepository.deleteDraft(AUTOSAVE_DRAFT_ID)
            _uiState.update {
                PostUiState(
                    entryState = PostEntryState.COMPOSER,
                    isPro = it.isPro,
                    hasAcceptedPostingRules = it.hasAcceptedPostingRules,
                )
            }
            _effects.send(PostEffect.ShowToast("Draft saved"))
            shellNavigationBus.navigateToTab(SpotTab.Home)
            _effects.send(PostEffect.NavigateToHome)
        }
    }

    fun clearValidationMessage() {
        _uiState.update { it.copy(validationMessage = null) }
    }

    private fun startPublish() {
        val state = _uiState.value
        val place = state.selectedPlace ?: return
        if (state.images.isEmpty() || state.selectedVibes.isEmpty()) return

        if (sessionBridge.currentUserId == null) {
            viewModelScope.launch {
                _effects.send(PostEffect.ShowToast("Sign in to post", isError = true))
            }
            return
        }

        _uiState.update { it.copy(isPublishing = true) }

        spotPublishCoordinator.enqueue(
            PublishJob(
                images = state.images,
                vibeTags = state.selectedVibes.toList(),
                latitude = place.latitude,
                longitude = place.longitude,
                locationName = place.name,
            ),
        )

        shellNavigationBus.navigateToTab(SpotTab.Home)
        viewModelScope.launch {
            _effects.send(PostEffect.NavigateToHome)
        }

        _uiState.update {
            PostUiState(
                entryState = PostEntryState.COMPOSER,
                isPro = it.isPro,
                hasAcceptedPostingRules = it.hasAcceptedPostingRules,
            )
        }
    }

    private fun loadCurrentLocationSuggestion() {
        viewModelScope.launch {
            val place = placeSearchProvider.currentLocationPlace().getOrNull()
            if (place != null && _uiState.value.selectedPlace == null) {
                _uiState.update {
                    it.copy(
                        selectedPlace = place,
                        locationQuery = place.name,
                    )
                }
                scheduleAutosave()
            }
        }
    }

    private fun scheduleAutosave() {
        autosaveJob?.cancel()
        autosaveJob = viewModelScope.launch {
            delay(500)
            val draft = buildDraft(id = AUTOSAVE_DRAFT_ID, status = PostDraftStatus.AUTOSAVED)
            postDraftRepository.autosave(draft, _uiState.value.images)
        }
    }

    private fun buildDraft(id: String, status: PostDraftStatus): PostDraft {
        val state = _uiState.value
        val place = state.selectedPlace
        return PostDraft(
            id = id,
            step = state.currentStep.index,
            status = status.name,
            vibeTags = state.selectedVibes.toList(),
            latitude = place?.latitude,
            longitude = place?.longitude,
            placeName = place?.name,
            address = place?.address,
            isCustomName = place?.isCustomName ?: false,
            imageFileNames = state.images.map { it.fileName },
        )
    }

    private fun canProceed(state: PostUiState): Boolean = when (state.currentStep) {
        PostDraftStep.PHOTOS -> state.images.isNotEmpty()
        PostDraftStep.LOCATION -> state.selectedPlace != null
        PostDraftStep.VIBES -> state.selectedVibes.isNotEmpty()
    }

    fun canProceedCurrentStep(): Boolean = canProceed(_uiState.value)

    private fun observeProStatus() {
        viewModelScope.launch {
            userSessionHolder.isPro.collect { isPro ->
                _uiState.update { it.copy(isPro = isPro) }
            }
        }
    }

    private fun PostDraft.toPlaceSuggestion(): PlaceSuggestion? {
        val lat = latitude ?: return null
        val lng = longitude ?: return null
        val name = placeName ?: return null
        return PlaceSuggestion(
            name = name,
            address = address,
            latitude = lat,
            longitude = lng,
            isCustomName = isCustomName,
        )
    }

    private companion object {
        const val TAG = "PostViewModel"
    }
}
