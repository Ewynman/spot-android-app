package com.spot.android.data.map

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeMapMarkerFeatureFlags(
    photoPinEnabled: Boolean = true,
) : MapMarkerFeatureFlags {

    private val _photoPinMarkersEnabled = MutableStateFlow(photoPinEnabled)
    override val photoPinMarkersEnabled: StateFlow<Boolean> =
        _photoPinMarkersEnabled.asStateFlow()

    override fun setPhotoPinMarkersEnabled(enabled: Boolean) {
        _photoPinMarkersEnabled.value = enabled
    }
}
