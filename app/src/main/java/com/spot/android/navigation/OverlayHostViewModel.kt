package com.spot.android.navigation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * ViewModel for the top-level overlay host above the tab shell.
 *
 * Feature code and deep-link handlers will call [showOverlay] / [dismissOverlay]
 * to present paywall, spot detail, and subscription success overlays.
 */
@HiltViewModel
class OverlayHostViewModel @Inject constructor() : ViewModel() {

    private val _overlayState = MutableStateFlow<AppOverlay>(AppOverlay.None)
    val overlayState: StateFlow<AppOverlay> = _overlayState.asStateFlow()

    fun showOverlay(overlay: AppOverlay) {
        _overlayState.value = overlay
    }

    fun dismissOverlay() {
        _overlayState.value = AppOverlay.None
    }

    fun showSpotDetail(spotId: String) {
        showOverlay(AppOverlay.SpotDetail(spotId))
    }

    fun showSpotLoading(spotId: String) {
        showOverlay(AppOverlay.SpotLoading(spotId))
    }

    fun showSpotUnavailable(spotId: String) {
        showOverlay(AppOverlay.SpotUnavailable(spotId))
    }

    fun showProSuccess() {
        showOverlay(AppOverlay.ProSuccess)
    }

    fun showProOnboarding() {
        showOverlay(AppOverlay.ProOnboarding)
    }

    fun showPaywall(entryPoint: String? = null) {
        showOverlay(AppOverlay.Paywall(entryPoint))
    }

    /**
     * Replaces the current overlay (e.g. loading → detail or unavailable).
     */
    fun replaceOverlay(overlay: AppOverlay) {
        _overlayState.update { overlay }
    }
}
