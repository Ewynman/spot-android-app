package com.spot.android.feature.post

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.spot.android.core.design.Dimensions
import com.spot.android.core.design.component.VibeChipFlow
import com.spot.android.core.design.theme.SpotColors
import com.spot.android.core.util.Constants
import com.spot.android.data.location.PlaceSuggestion
import com.spot.android.data.post.PostDraftStep
import com.spot.android.data.post.ProcessedImage

@Composable
fun PostStepIndicator(
    currentStep: PostDraftStep,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("post.stepIndicator"),
        horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small),
    ) {
        PostDraftStep.entries.forEach { step ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (step.index <= currentStep.index) SpotColors.Primary else SpotColors.Accent,
                    ),
            )
        }
    }
}

@Composable
fun PostPhotosStep(
    images: List<ProcessedImage>,
    isProcessing: Boolean,
    isPro: Boolean,
    onTakePhoto: () -> Unit,
    onChooseGallery: () -> Unit,
    onRemoveImage: (String) -> Unit,
    onOpenDrafts: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("post.step.photos"),
    ) {
        Text(
            text = "Add photos",
            style = MaterialTheme.typography.titleMedium,
            color = SpotColors.Primary,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))

        if (isProcessing) {
            CircularProgressIndicator(
                color = SpotColors.Primary,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .testTag("post.processingImages"),
            )
        }

        if (images.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small),
                modifier = Modifier.testTag("post.selectedImages"),
            ) {
                items(images, key = { it.fileName }) { image ->
                    SelectedImageThumbnail(
                        image = image,
                        onRemove = { onRemoveImage(image.fileName) },
                    )
                }
            }
            Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))
        }

        PostPrimaryButton(
            text = "Take Photo",
            enabled = !isProcessing,
            onClick = onTakePhoto,
            modifier = Modifier.testTag("post.takePhoto"),
        )
        Spacer(modifier = Modifier.height(Dimensions.Spacing.small))
        OutlinedButton(
            onClick = onChooseGallery,
            enabled = !isProcessing,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("post.chooseGallery"),
        ) {
            Text("Choose from Gallery", color = SpotColors.Primary)
        }

        if (!isPro) {
            Spacer(modifier = Modifier.height(Dimensions.Spacing.small))
            Text(
                text = "Free accounts: 1 image per post",
                style = MaterialTheme.typography.bodySmall,
                color = SpotColors.Primary,
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        TextButtonLink(
            text = "Drafts",
            onClick = onOpenDrafts,
            modifier = Modifier.testTag("post.draftsButton"),
        )
    }
}

@Composable
private fun SelectedImageThumbnail(
    image: ProcessedImage,
    onRemove: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(96.dp)
            .clip(RoundedCornerShape(Dimensions.Radius.medium)),
    ) {
        AsyncImage(
            model = image.bytes,
            contentDescription = "Selected image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(1f),
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(24.dp)
                .testTag("post.removeImage.${image.fileName}"),
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove image",
                tint = SpotColors.ButtonText,
            )
        }
    }
}

@Composable
fun PostLocationStep(
    query: String,
    suggestions: List<PlaceSuggestion>,
    selectedPlace: PlaceSuggestion?,
    isSearching: Boolean,
    onQueryChange: (String) -> Unit,
    onSelectPlace: (PlaceSuggestion) -> Unit,
    onCustomNameChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("post.step.location"),
    ) {
        Text(
            text = "Choose a location",
            style = MaterialTheme.typography.titleMedium,
            color = SpotColors.Primary,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text("Search places") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("post.locationSearch"),
            singleLine = true,
        )

        if (isSearching) {
            Spacer(modifier = Modifier.height(Dimensions.Spacing.small))
            CircularProgressIndicator(
                color = SpotColors.Primary,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }

        if (suggestions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(Dimensions.Spacing.small))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small),
                modifier = Modifier.testTag("post.locationSuggestions"),
            ) {
                items(suggestions, key = { "${it.latitude}_${it.longitude}_${it.name}" }) { place ->
                    OutlinedButton(
                        onClick = { onSelectPlace(place) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(place.name, color = SpotColors.Primary)
                            place.address?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SpotColors.Primary,
                                )
                            }
                        }
                    }
                }
            }
        }

        if (selectedPlace != null) {
            Spacer(modifier = Modifier.height(Dimensions.Spacing.large))
            Text(
                text = "Display name",
                style = MaterialTheme.typography.labelMedium,
                color = SpotColors.Primary,
            )
            Spacer(modifier = Modifier.height(Dimensions.Spacing.small))
            OutlinedTextField(
                value = selectedPlace.name,
                onValueChange = onCustomNameChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("post.locationDisplayName"),
                singleLine = true,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostVibesStep(
    selectedVibes: Set<String>,
    customVibeInput: String,
    isPro: Boolean,
    onToggleVibe: (String) -> Unit,
    onCustomVibeInputChange: (String) -> Unit,
    onAddCustomVibe: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val allVibes = Constants.VibeTags.DEFAULT_TAGS

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("post.step.vibes"),
    ) {
        Text(
            text = "Choose vibes",
            style = MaterialTheme.typography.titleMedium,
            color = SpotColors.Primary,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))

        VibeChipFlow(
            vibes = allVibes,
            selectedVibes = selectedVibes,
            onVibeToggle = onToggleVibe,
        )

        Spacer(modifier = Modifier.height(Dimensions.Spacing.large))
        Text(
            text = if (isPro) "Add a custom vibe" else "Custom vibes (Pro)",
            style = MaterialTheme.typography.labelMedium,
            color = SpotColors.Primary,
        )
        Spacer(modifier = Modifier.height(Dimensions.Spacing.small))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small),
            verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small),
        ) {
            OutlinedTextField(
                value = customVibeInput,
                onValueChange = onCustomVibeInputChange,
                label = { Text("Custom vibe") },
                modifier = Modifier
                    .weight(1f, fill = false)
                    .testTag("post.customVibeInput"),
                singleLine = true,
            )
            OutlinedButton(
                onClick = onAddCustomVibe,
                modifier = Modifier.testTag("post.addCustomVibe"),
            ) {
                Text("Add", color = SpotColors.Primary)
            }
        }
    }
}

@Composable
fun PostPrimaryButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = SpotColors.Primary,
            contentColor = SpotColors.ButtonText,
            disabledContainerColor = SpotColors.Accent,
            disabledContentColor = SpotColors.Primary,
        ),
    ) {
        Text(text)
    }
}

@Composable
fun TextButtonLink(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Text(text, color = SpotColors.Primary)
    }
}

@Composable
fun PostComposerActions(
    currentStep: PostDraftStep,
    canProceed: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onPublish: () -> Unit,
    onSaveDraft: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (currentStep == PostDraftStep.VIBES) {
            PostPrimaryButton(
                text = "Post",
                enabled = canProceed,
                onClick = onPublish,
                modifier = Modifier.testTag("post.publishButton"),
            )
        } else {
            PostPrimaryButton(
                text = "Next",
                enabled = canProceed,
                onClick = onNext,
                modifier = Modifier.testTag("post.nextButton"),
            )
        }
        Spacer(modifier = Modifier.height(Dimensions.Spacing.small))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            if (currentStep != PostDraftStep.PHOTOS) {
                TextButtonLink(
                    text = "Back",
                    onClick = onBack,
                    modifier = Modifier.testTag("post.backButton"),
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
            TextButtonLink(
                text = "Save draft",
                onClick = onSaveDraft,
                modifier = Modifier.testTag("post.saveDraftButton"),
            )
        }
    }
}
