package com.spot.android.feature.map

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.spot.android.core.design.component.Toast
import com.spot.android.core.design.component.ToastType
import com.spot.android.feature.safety.LocalSafetyActions
import com.spot.android.navigation.ProfileNavigationBus
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

/**
 * Profile map variant showing a single user's spots with drawer interaction.
 *
 * Reference: PRD/07-map.md, PRD/10-profile-social.md
 */
@SuppressLint("MissingPermission")
@Composable
fun ProfileMapView(
    userId: String,
    profileNavigationBus: ProfileNavigationBus? = null,
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = hiltViewModel(key = "profile_map_$userId"),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val safetyActions = LocalSafetyActions.current

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            uiState.initialCameraTarget,
            uiState.initialCameraZoom,
        )
    }

    LaunchedEffect(userId) {
        viewModel.setProfileUserFilter(userId)
        viewModel.onFirstAppear()
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.onTabLeft() }
    }

    LaunchedEffect(cameraPositionState) {
        snapshotFlow { cameraPositionState.isMoving }
            .distinctUntilChanged()
            .filter { !it }
            .collect {
                val position = cameraPositionState.position
                viewModel.onCameraIdle(
                    centerLat = position.target.latitude,
                    centerLng = position.target.longitude,
                    zoom = position.zoom,
                    userInitiated = false,
                )
            }
    }

    val selectedSpot = uiState.selectedSpotId?.let { uiState.allSpots[it] }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile.mapView"),
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false),
            uiSettings = MapUiSettings(
                myLocationButtonEnabled = false,
                zoomControlsEnabled = false,
                compassEnabled = false,
            ),
            onMapClick = { viewModel.onMapTapped() },
        ) {
            uiState.pins.forEach { pin ->
                val isSelected = pin.spot.id == uiState.selectedSpotId
                MarkerComposable(
                    state = MarkerState(position = pin.displayPosition),
                    onClick = {
                        viewModel.onPinSelected(pin.spot.id)
                        true
                    },
                ) {
                    MapPinMarker(
                        isSelected = isSelected,
                        testTag = "profile.mapPin.${pin.spot.id}",
                    )
                }
            }
        }

        selectedSpot?.let { spot ->
            MapSpotDrawer(
                spot = spot,
                drawerState = uiState.drawerState,
                onClose = viewModel::onDrawerClose,
                onExpandToggle = viewModel::onDrawerExpandToggle,
                onUserClick = { profileNavigationBus?.openProfile(spot.userId) },
                onLikeClick = { viewModel.toggleLike(spot) },
                onBookmarkClick = { viewModel.toggleBookmark(spot) },
                onMoreClick = { safetyActions?.openSpotOverflowMenu(spot) },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }

        uiState.errorMessage?.let { message ->
            Toast(
                message = message,
                type = ToastType.ERROR,
                onDismiss = viewModel::clearErrorMessage,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp),
            )
        }
    }
}
