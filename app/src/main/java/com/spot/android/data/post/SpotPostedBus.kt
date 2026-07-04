package com.spot.android.data.post

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Emits when a spot is successfully published so the feed can refresh and show a toast.
 *
 * Reference: PRD/06-home-feed.md, PRD/08-post-flow.md
 */
@Singleton
class SpotPostedBus @Inject constructor() {

    private val _events = MutableSharedFlow<SpotPostedEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<SpotPostedEvent> = _events.asSharedFlow()

    fun emit(spotId: String) {
        _events.tryEmit(SpotPostedEvent(spotId))
    }
}

data class SpotPostedEvent(
    val spotId: String,
)
