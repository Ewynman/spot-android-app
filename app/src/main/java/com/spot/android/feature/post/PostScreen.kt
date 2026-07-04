package com.spot.android.feature.post

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spot.android.core.design.Dimensions
import com.spot.android.core.design.component.PermissionType
import com.spot.android.core.design.component.Toast
import com.spot.android.core.design.component.ToastType
import com.spot.android.core.design.component.TopNavigationView
import com.spot.android.core.design.theme.SpotColors
import com.spot.android.data.post.PostDraftStep
import com.spot.android.feature.permissions.PermissionsViewModel
import com.spot.android.navigation.OverlayHostViewModel

/**
 * Post composer tab with 3-step flow and publish pipeline.
 *
 * Reference: PRD/08-post-flow.md
 */
@Composable
fun PostScreen(
    modifier: Modifier = Modifier,
    viewModel: PostViewModel = hiltViewModel(),
    permissionsViewModel: PermissionsViewModel = hiltViewModel(),
    overlayViewModel: OverlayHostViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onFirstAppear()
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is PostEffect.ShowPaywall -> overlayViewModel.showPaywall(entryPoint = effect.entryPoint)
                is PostEffect.ShowToast -> Unit
                PostEffect.NavigateToHome,
                PostEffect.OpenConfirmEmail,
                -> Unit
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5),
    ) { uris ->
        viewModel.addImagesFromUris(uris)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
    ) { bitmap ->
        if (bitmap != null) {
            val bytes = java.io.ByteArrayOutputStream().apply {
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, this)
            }.toByteArray()
            viewModel.addImagesFromBytes(bytes)
        }
    }

    Scaffold(
        modifier = modifier.testTag("post.postRoot"),
        topBar = { TopNavigationView() },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (uiState.entryState) {
                PostEntryState.CHECKING_EMAIL -> PostLoadingGate()
                PostEntryState.EMAIL_NOT_VERIFIED -> PostEmailVerificationGate(
                    onVerifyClick = viewModel::onVerifyEmailClick,
                )
                PostEntryState.COMPOSER -> PostComposerContent(
                    uiState = uiState,
                    canProceed = viewModel.canProceedCurrentStep(),
                    onTakePhoto = {
                        permissionsViewModel.requestPermission(PermissionType.CAMERA) { state ->
                            if (state.isGranted()) cameraLauncher.launch(null)
                        }
                    },
                    onChooseGallery = {
                        permissionsViewModel.requestPermission(PermissionType.PHOTOS) { state ->
                            if (state.isGranted()) {
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                                )
                            }
                        }
                    },
                    onRemoveImage = viewModel::removeImage,
                    onOpenDrafts = viewModel::showDraftsSheet,
                    onQueryChange = viewModel::updateLocationQuery,
                    onSelectPlace = viewModel::selectPlace,
                    onCustomNameChange = viewModel::setCustomPlaceName,
                    onToggleVibe = viewModel::toggleVibe,
                    onCustomVibeInputChange = viewModel::updateCustomVibeInput,
                    onAddCustomVibe = viewModel::addCustomVibe,
                    onBack = viewModel::previousStep,
                    onNext = viewModel::nextStep,
                    onPublish = viewModel::publish,
                    onSaveDraft = viewModel::showSaveDraftDialog,
                )
            }

            uiState.validationMessage?.let { message ->
                Toast(
                    message = message,
                    type = ToastType.INFO,
                    visible = true,
                    onDismiss = viewModel::clearValidationMessage,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                        .testTag("post.validationToast"),
                )
            }
        }
    }

    if (uiState.showDraftsSheet) {
        PostDraftsSheet(
            drafts = uiState.drafts,
            onLoadDraft = viewModel::loadDraft,
            onDeleteDraft = viewModel::deleteDraft,
            onDismiss = viewModel::dismissDraftsSheet,
        )
    }

    if (uiState.showPostingRulesSheet) {
        PostingRulesSheet(
            onAccept = viewModel::acceptPostingRules,
            onDismiss = viewModel::dismissPostingRules,
        )
    }

    if (uiState.showSaveDraftDialog) {
        SaveDraftDialog(
            name = uiState.saveDraftName,
            onNameChange = viewModel::updateSaveDraftName,
            onSave = viewModel::saveNamedDraft,
            onDismiss = viewModel::dismissSaveDraftDialog,
        )
    }
}

@Composable
private fun PostComposerContent(
    uiState: PostUiState,
    canProceed: Boolean,
    onTakePhoto: () -> Unit,
    onChooseGallery: () -> Unit,
    onRemoveImage: (String) -> Unit,
    onOpenDrafts: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSelectPlace: (com.spot.android.data.location.PlaceSuggestion) -> Unit,
    onCustomNameChange: (String) -> Unit,
    onToggleVibe: (String) -> Unit,
    onCustomVibeInputChange: (String) -> Unit,
    onAddCustomVibe: () -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onPublish: () -> Unit,
    onSaveDraft: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Dimensions.Padding.horizontal)
            .padding(vertical = Dimensions.Padding.verticalMedium)
            .testTag("post.composer"),
    ) {
        PostStepIndicator(currentStep = uiState.currentStep)
        Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))
        Text(
            text = "Step ${uiState.currentStep.index} of 3",
            color = SpotColors.Primary,
            modifier = Modifier.testTag("post.stepLabel"),
        )
        Spacer(modifier = Modifier.height(Dimensions.Spacing.large))

        when (uiState.currentStep) {
            PostDraftStep.PHOTOS -> PostPhotosStep(
                images = uiState.images,
                isProcessing = uiState.isProcessingImages,
                isPro = uiState.isPro,
                onTakePhoto = onTakePhoto,
                onChooseGallery = onChooseGallery,
                onRemoveImage = onRemoveImage,
                onOpenDrafts = onOpenDrafts,
                modifier = Modifier.weight(1f),
            )
            PostDraftStep.LOCATION -> PostLocationStep(
                query = uiState.locationQuery,
                suggestions = uiState.locationSuggestions,
                selectedPlace = uiState.selectedPlace,
                isSearching = uiState.isSearchingLocation,
                onQueryChange = onQueryChange,
                onSelectPlace = onSelectPlace,
                onCustomNameChange = onCustomNameChange,
                modifier = Modifier.weight(1f),
            )
            PostDraftStep.VIBES -> PostVibesStep(
                selectedVibes = uiState.selectedVibes,
                customVibeInput = uiState.customVibeInput,
                isPro = uiState.isPro,
                onToggleVibe = onToggleVibe,
                onCustomVibeInputChange = onCustomVibeInputChange,
                onAddCustomVibe = onAddCustomVibe,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))
        PostComposerActions(
            currentStep = uiState.currentStep,
            canProceed = canProceed,
            onBack = onBack,
            onNext = onNext,
            onPublish = onPublish,
            onSaveDraft = onSaveDraft,
        )
    }
}

private fun com.spot.android.data.permissions.PermissionState.isGranted(): Boolean =
    this == com.spot.android.data.permissions.PermissionState.AUTHORIZED ||
        this == com.spot.android.data.permissions.PermissionState.NOT_REQUIRED
