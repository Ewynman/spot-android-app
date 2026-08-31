package com.spot.android.feature.map

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.spot.android.core.design.theme.SpotColors
import com.spot.android.core.media.MapMarkerImageCache
import com.spot.android.core.util.Constants

/**
 * Dispatch a spot to the right marker composable (task 12).
 *
 * When [showPhotoPin] is true **and** [imageUrl] is non-null we render the
 * new circular photo preview; otherwise we render the legacy branded
 * teardrop. This keeps a single call-site for [MapScreen] while letting the
 * feature flag short-circuit the whole thing.
 */
@Composable
fun MapPinMarker(
    isSelected: Boolean,
    showPhotoPin: Boolean,
    imageUrl: String?,
    imageCache: MapMarkerImageCache,
    onImageLoaded: (cacheHit: Boolean) -> Unit,
    onImageFailed: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "map.pin",
    username: String? = null,
    locationName: String? = null,
) {
    val usePhotoPin = showPhotoPin && !imageUrl.isNullOrBlank()
    if (usePhotoPin) {
        PhotoPinMarker(
            imageUrl = imageUrl,
            isSelected = isSelected,
            imageCache = imageCache,
            onImageLoaded = onImageLoaded,
            onImageFailed = onImageFailed,
            modifier = modifier,
            testTag = testTag,
            username = username,
            locationName = locationName,
        )
    } else {
        TeardropPinMarker(
            isSelected = isSelected,
            modifier = modifier,
            testTag = testTag,
        )
    }
}

/**
 * Branded green teardrop marker — the pre-photo-pin behaviour, retained as
 * the fallback and for the profile-map surface (task 14 will migrate that).
 */
@Composable
fun TeardropPinMarker(
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    testTag: String = "map.pin",
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) Constants.MapDesign.PIN_SELECTED_SCALE else 1f,
        label = "pinScale",
    )
    val pinSize = Constants.MapDesign.PIN_SIZE_DP.dp

    Box(
        modifier = modifier
            .scale(scale)
            .size(pinSize)
            .testTag(testTag),
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(pinSize + 10.dp)
                    .background(SpotColors.MapSelectedGlow, CircleShape),
            )
        }

        Box(
            modifier = Modifier
                .size(pinSize)
                .shadow(if (isSelected) 6.dp else 2.dp, CircleShape)
                .background(SpotColors.MapMarkerGreen, CircleShape)
                .border(1.5.dp, SpotColors.MapMarkerStroke, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(pinSize * 0.35f)
                    .background(SpotColors.MapMarkerDot, CircleShape),
            )
        }
    }
}
