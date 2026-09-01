package com.spot.android.data.map

import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cross-tab focus dispatcher for the "Open in Map" flow from Home cards.
 *
 * When the user flips a home card and taps **Open in Map**, we:
 *
 *  1. Publish a [FocusRequest] so [com.spot.android.feature.map.MapViewModel]
 *     can animate the camera and select the spot.
 *  2. Mark the spot id as "return scroll pending" so Home can scroll back to
 *     the same card when the user pops back to the Home tab.
 *
 * Both slots are one-shot: consumers clear them after handling. Mirrors iOS
 * `MapFocusCoordinator` (task 13 / iOS PR #90).
 */
@Singleton
class MapFocusCoordinator @Inject constructor() {

    data class FocusRequest(
        val spotId: String,
        val target: LatLng,
        val timestamp: Long,
    )

    private val _pendingFocus = MutableStateFlow<FocusRequest?>(null)
    val pendingFocus: StateFlow<FocusRequest?> = _pendingFocus.asStateFlow()

    private val _pendingHomeReturnScroll = MutableStateFlow<String?>(null)
    val pendingHomeReturnScroll: StateFlow<String?> = _pendingHomeReturnScroll.asStateFlow()

    /**
     * Request that the Map tab focus on the given spot and mark the same spot
     * for a one-shot Home return scroll.
     */
    fun openInMap(spotId: String, target: LatLng, now: Long = System.currentTimeMillis()) {
        _pendingFocus.value = FocusRequest(spotId = spotId, target = target, timestamp = now)
        _pendingHomeReturnScroll.value = spotId
    }

    /**
     * Called by the Map tab once it has animated the camera and selected the
     * spot.
     */
    fun consumeFocus() {
        _pendingFocus.value = null
    }

    /**
     * Called by Home once it has scrolled back to the pending spot. Returns
     * the id that was consumed (or null if there was nothing pending).
     */
    fun consumeHomeReturnScroll(): String? {
        val id = _pendingHomeReturnScroll.value
        _pendingHomeReturnScroll.value = null
        return id
    }
}
