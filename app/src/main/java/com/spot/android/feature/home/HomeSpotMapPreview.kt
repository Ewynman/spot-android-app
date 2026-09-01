package com.spot.android.feature.home

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.spot.android.core.design.Dimensions
import com.spot.android.core.design.theme.SpotColors
import com.spot.android.core.util.Constants
import com.spot.android.feature.map.TeardropPinMarker

/**
 * Back-face of the Home [com.spot.android.core.design.component.SpotCard]:
 * a small non-interactive Google Map centred on the spot's coordinates with
 * the branded teardrop marker and an "Open in Map" pill overlay.
 *
 * Reuses [Constants.HomeMapPreview] for zoom + card sizing so the front and
 * back faces occupy the same aspect ratio. Task 13 / iOS PR #90.
 */
@SuppressLint("MissingPermission")
@Composable
fun HomeSpotMapPreview(
    spotId: String,
    latitude: Double,
    longitude: Double,
    aspectRatio: Float,
    onOpenInMap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val target = androidx.compose.runtime.remember(spotId) { LatLng(latitude, longitude) }
    val cameraPositionState = rememberCameraPositionState(key = "home.mapPreview.$spotId") {
        position = CameraPosition.fromLatLngZoom(target, Constants.HomeMapPreview.ZOOM_LEVEL)
    }
    val markerState = androidx.compose.runtime.remember(spotId) { MarkerState(position = target) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(Dimensions.Radius.medium))
            .aspectRatio(aspectRatio.takeIf { it > 0f } ?: 1f)
            .testTag("spotCard.mapPreview.$spotId"),
    ) {
        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .testTag("spotCard.mapPreview.map"),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false),
            uiSettings = MapUiSettings(
                compassEnabled = false,
                indoorLevelPickerEnabled = false,
                mapToolbarEnabled = false,
                myLocationButtonEnabled = false,
                rotationGesturesEnabled = false,
                scrollGesturesEnabled = false,
                tiltGesturesEnabled = false,
                zoomControlsEnabled = false,
                zoomGesturesEnabled = false,
            ),
            onMapClick = { onOpenInMap() },
        ) {
            MarkerComposable(
                spotId,
                state = markerState,
                onClick = {
                    onOpenInMap()
                    true
                },
            ) {
                TeardropPinMarker(
                    isSelected = false,
                    testTag = "spotCard.mapPreview.marker",
                )
            }
        }

        OpenInMapPill(
            onClick = onOpenInMap,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp),
        )
    }
}

@Composable
private fun OpenInMapPill(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(SpotColors.Primary)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("spotCard.openInMap"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
            contentDescription = null,
            tint = SpotColors.ButtonText,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = "Open in Map",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = SpotColors.ButtonText,
        )
    }
}
