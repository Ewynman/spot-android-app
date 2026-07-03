package com.spot.android.navigation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Event bus for bottom-tab reselect events.
 *
 * When the user taps the already-selected tab, the shell emits a reselect event
 * so the active screen can scroll to top, dismiss drawers, or reset state.
 *
 * Reference: PRD/00-overview.md, PRD/06-home-feed.md
 */
@Singleton
class TabReselectBus @Inject constructor() {

    private val _reselectEvents = MutableSharedFlow<SpotTab>(
        extraBufferCapacity = 1,
    )

    val reselectEvents: SharedFlow<SpotTab> = _reselectEvents.asSharedFlow()

    fun onTabReselected(tab: SpotTab) {
        _reselectEvents.tryEmit(tab)
    }
}
