package com.spot.android.feature.map

import com.google.android.gms.maps.model.LatLng
import com.spot.android.data.map.MapPin
import com.spot.android.data.map.SpotMapFilter
import com.spot.android.data.model.Spot

enum class MapDrawerState {
    HIDDEN,
    PEEK,
    EXPANDED,
}

enum class MapLoadState {
    IDLE,
    LOADING,
    LOADED,
    ERROR,
}

data class MapUiState(
    val loadState: MapLoadState = MapLoadState.IDLE,
    val pins: List<MapPin> = emptyList(),
    val allSpots: Map<String, Spot> = emptyMap(),
    val selectedSpotId: String? = null,
    val drawerState: MapDrawerState = MapDrawerState.HIDDEN,
    val userLocation: LatLng? = null,
    val userHasMovedMap: Boolean = false,
    val hasCenteredOnFirstFix: Boolean = false,
    val initialCameraTarget: LatLng = LatLng(
        com.spot.android.core.util.Constants.MapDesign.FALLBACK_LATITUDE,
        com.spot.android.core.util.Constants.MapDesign.FALLBACK_LONGITUDE,
    ),
    val initialCameraZoom: Float = com.spot.android.core.util.Constants.MapDesign.FALLBACK_ZOOM,
    val pendingCameraTarget: LatLng? = null,
    val pendingCameraZoom: Float? = null,
    val pendingCameraLiftMeters: Double? = null,
    val isPro: Boolean = false,
    val activeFilters: Set<SpotMapFilter> = emptySet(),
    val selectedVibeNames: Set<String> = emptySet(),
    val showVibeFilterSheet: Boolean = false,
    val availableVibeNames: List<String> = emptyList(),
    val showLocationHalo: Boolean = false,
    val errorMessage: String? = null,
    val profileFilterUserId: String? = null,
    val currentUserProfileImageUrl: String? = null,
)

sealed interface MapEffect {
    data class ShowPaywall(val entryPoint: String) : MapEffect
    data class AnimateCamera(
        val target: LatLng,
        val zoom: Float? = null,
        val liftMeters: Double? = null,
    ) : MapEffect
}
