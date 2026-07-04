package com.spot.android.navigation

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Requests tab switches from feature screens (e.g. post flow → home on publish).
 */
@Singleton
class ShellNavigationBus @Inject constructor() {

    private val _tabRequests = MutableSharedFlow<SpotTab>(extraBufferCapacity = 2)
    val tabRequests: SharedFlow<SpotTab> = _tabRequests.asSharedFlow()

    fun navigateToTab(tab: SpotTab) {
        _tabRequests.tryEmit(tab)
    }
}
