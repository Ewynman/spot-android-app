package com.spot.android.data.map

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Runtime feature flags for map marker rendering.
 *
 * Mirrors iOS `MapMarkerFeatureFlags` (task 12). QA can flip flags off at
 * runtime via debug settings; production defaults are declared here.
 */
interface MapMarkerFeatureFlags {
    val photoPinMarkersEnabled: StateFlow<Boolean>

    fun setPhotoPinMarkersEnabled(enabled: Boolean)
}

@Singleton
class DefaultMapMarkerFeatureFlags @Inject constructor() : MapMarkerFeatureFlags {

    private val _photoPinMarkersEnabled = MutableStateFlow(DEFAULT_PHOTO_PIN_MARKERS_ENABLED)
    override val photoPinMarkersEnabled: StateFlow<Boolean> =
        _photoPinMarkersEnabled.asStateFlow()

    override fun setPhotoPinMarkersEnabled(enabled: Boolean) {
        _photoPinMarkersEnabled.value = enabled
    }

    companion object {
        // Photo pins ship on in Debug + Release; QA can flip off if needed.
        const val DEFAULT_PHOTO_PIN_MARKERS_ENABLED = true
    }
}
