package com.spot.android.data.content

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Emits instant local removal signals for feed/map surfaces.
 *
 * Used when blocking or reporting content so items disappear immediately
 * without waiting for the next server fetch.
 *
 * Reference: PRD/06-home-feed.md, PRD/13-moderation-safety.md
 */
@Singleton
class LocalContentRemovalBus @Inject constructor() {

    private val _removals = MutableSharedFlow<ContentRemovalEvent>(extraBufferCapacity = 16)
    val removals: SharedFlow<ContentRemovalEvent> = _removals.asSharedFlow()

    fun removeByAuthor(authorUserId: String) {
        _removals.tryEmit(ContentRemovalEvent.ByAuthor(authorUserId))
    }

    fun removeBySpotId(spotId: String) {
        _removals.tryEmit(ContentRemovalEvent.BySpotId(spotId))
    }
}

sealed interface ContentRemovalEvent {
    data class ByAuthor(val authorUserId: String) : ContentRemovalEvent
    data class BySpotId(val spotId: String) : ContentRemovalEvent
}
