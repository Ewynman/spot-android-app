package com.spot.android.feature.map

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.SphericalUtil
import com.spot.android.core.design.component.PermissionType
import com.spot.android.core.design.component.Toast
import com.spot.android.core.design.component.ToastType
import com.spot.android.core.design.component.TopNavigationView
import com.spot.android.core.design.theme.SpotColors
import com.spot.android.core.util.Constants
import com.spot.android.data.permissions.PermissionState
import com.spot.android.feature.permissions.PermissionsViewModel
import com.spot.android.feature.safety.LocalSafetyActions
import com.spot.android.navigation.OverlayHostViewModel
import com.spot.android.navigation.SpotTab
import com.spot.android.navigation.TabReselectBus
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

/**
 * Map tab with viewport-based spot discovery.
 *
 * Reference: PRD/07-map.md
 */
@SuppressLint("MissingPermission")
@Composable
fun MapScreen(
    tabReselectBus: TabReselectBus,
    overlayViewModel: OverlayHostViewModel,
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = hiltViewModel(),
    permissionsViewModel: PermissionsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val safetyActions = LocalSafetyActions.current

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            uiState.initialCameraTarget,
            uiState.initialCameraZoom,
        )
    }

    var userInitiatedMove by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.onFirstAppear()
    }

    LaunchedEffect(tabReselectBus) {
        tabReselectBus.reselectEvents.collect { tab ->
            if (tab == SpotTab.Map) {
                viewModel.onTabReselected()
            }
        }
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
                    userInitiated = userInitiatedMove,
                )
                userInitiatedMove = false
            }
    }

    LaunchedEffect(cameraPositionState) {
        snapshotFlow { cameraPositionState.isMoving }
            .distinctUntilChanged()
            .filter { it }
            .collect {
                if (cameraPositionState.cameraMoveStartedReason == com.google.maps.android.compose.CameraMoveStartedReason.GESTURE) {
                    userInitiatedMove = true
                }
            }
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is MapEffect.ShowPaywall -> {
                    overlayViewModel.showPaywall(entryPoint = effect.entryPoint)
                }
                is MapEffect.AnimateCamera -> {
                    val target = effect.target
                    val liftedTarget = effect.liftMeters?.let { meters ->
                        SphericalUtil.computeOffset(target, meters, 180.0)
                    } ?: target
                    val update = if (effect.zoom != null) {
                        CameraUpdateFactory.newLatLngZoom(liftedTarget, effect.zoom)
                    } else {
                        CameraUpdateFactory.newLatLng(liftedTarget)
                    }
                    cameraPositionState.animate(update)
                }
            }
        }
    }

    val selectedSpot = uiState.selectedSpotId?.let { uiState.allSpots[it] }

    Scaffold(
        modifier = modifier.testTag("map.mapRoot"),
        topBar = { TopNavigationView() },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            GoogleMap(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("map.googleMap"),
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
                            testTag = "map.pin.${pin.spot.id}",
                        )
                    }
                }

                uiState.userLocation?.let { location ->
                    MarkerComposable(
                        state = MarkerState(position = location),
                    ) {
                        MapUserLocationMarker(
                            isPro = uiState.isPro,
                            showHalo = uiState.showLocationHalo,
                            profileImageUrl = uiState.currentUserProfileImageUrl,
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
            ) {
                if (uiState.isPro) {
                    MapFilterPillsRow(
                        activeFilters = uiState.activeFilters,
                        onFilterToggle = viewModel::toggleFilter,
                    )
                }
            }

            FloatingActionButton(
                onClick = {
                    permissionsViewModel.requestPermission(PermissionType.LOCATION) { state ->
                        when (state) {
                            PermissionState.AUTHORIZED -> viewModel.onRecenterTapped()
                            else -> Unit
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = if (selectedSpot != null) 300.dp else 96.dp)
                    .testTag("map.recenterButton"),
                containerColor = SpotColors.Primary,
                shape = CircleShape,
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Recenter on my location",
                    tint = SpotColors.ButtonText,
                )
            }

            selectedSpot?.let { spot ->
                MapSpotDrawer(
                    spot = spot,
                    drawerState = uiState.drawerState,
                    onClose = viewModel::onDrawerClose,
                    onExpandToggle = viewModel::onDrawerExpandToggle,
                    onUserClick = { /* Profile navigation in Phase 3.5 */ },
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

    if (uiState.showVibeFilterSheet) {
        var pendingVibes by remember(uiState.selectedVibeNames) {
            mutableStateOf(uiState.selectedVibeNames)
        }
        MapVibeFilterSheet(
            availableVibes = uiState.availableVibeNames,
            selectedVibes = pendingVibes,
            onVibeToggle = { vibe ->
                pendingVibes = if (pendingVibes.contains(vibe)) {
                    pendingVibes - vibe
                } else {
                    pendingVibes + vibe
                }
                viewModel.applyVibeFilter(pendingVibes)
            },
            onDismiss = viewModel::onVibeFilterSheetDismissed,
        )
    }
}
