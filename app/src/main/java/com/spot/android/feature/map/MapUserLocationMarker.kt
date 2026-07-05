package com.spot.android.feature.map

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.spot.android.core.design.theme.SpotColors
import com.spot.android.core.util.Constants

/**
 * User-location avatar marker with Pro gold or default green ring.
 *
 * Reference: PRD/07-map.md
 */
@Composable
fun MapUserLocationMarker(
    isPro: Boolean,
    showHalo: Boolean,
    profileImageUrl: String?,
    modifier: Modifier = Modifier,
) {
    val size = Constants.MapDesign.AVATAR_MARKER_SIZE_DP.dp
    val ringColor = if (isPro) SpotColors.ProGold else SpotColors.MapAvatarRing
    val ringWidth = Constants.MapDesign.AVATAR_RING_WIDTH_DP.dp

    Box(
        modifier = modifier
            .size(size + if (showHalo) 12.dp else 0.dp)
            .testTag("map.userLocationMarker"),
        contentAlignment = Alignment.Center,
    ) {
        if (showHalo) {
            Box(
                modifier = Modifier
                    .size(size + 10.dp)
                    .background(SpotColors.MapAvatarHalo, CircleShape),
            )
        }

        Box(
            modifier = Modifier
                .size(size)
                .border(ringWidth, ringColor, CircleShape)
                .clip(CircleShape)
                .background(SpotColors.Accent),
            contentAlignment = Alignment.Center,
        ) {
            if (profileImageUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(profileImageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Your location",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize(),
                )
            }
        }
    }
}
