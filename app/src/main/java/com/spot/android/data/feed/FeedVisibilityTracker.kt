package com.spot.android.data.feed

import com.spot.android.core.util.Constants
import com.spot.android.data.model.enums.FeedEventType
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Tracks feed card visibility and emits dwell-based behavioral events.
 *
 * Reference: PRD/06-home-feed.md, PRD/16-feed-ranking-algorithm.md
 */
@Singleton
class FeedVisibilityTracker @Inject constructor(
    feedEventService: FeedEventService,
) {
    private val delegate = FeedVisibilityTrackerDelegate(
        feedEventService = feedEventService,
        dispatcher = Dispatchers.Default,
    )

    fun syncVisibleSpotIds(spotIds: Set<String>) = delegate.syncVisibleSpotIds(spotIds)

    fun resetSession() = delegate.resetSession()
}

internal class FeedVisibilityTrackerDelegate(
    private val feedEventService: FeedEventService,
    dispatcher: CoroutineDispatcher,
) {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private data class SpotVisibility(
        val visibleSinceMs: Long,
        var visible2sEmitted: Boolean = false,
        var longDwellEmitted: Boolean = false,
    )

    private val visibleSpots = mutableMapOf<String, SpotVisibility>()
    private val timerJobs = mutableMapOf<String, Job>()

    fun syncVisibleSpotIds(spotIds: Set<String>) {
        val currentlyVisible = visibleSpots.keys.toSet()
        (currentlyVisible - spotIds).forEach(::onSpotHidden)
        (spotIds - currentlyVisible).forEach(::onSpotBecameVisible)
    }

    fun resetSession() {
        visibleSpots.keys.toList().forEach(::onSpotHidden)
        feedEventService.resetSession()
    }

    private fun onSpotBecameVisible(spotId: String) {
        if (visibleSpots.containsKey(spotId)) return

        val now = System.currentTimeMillis()
        visibleSpots[spotId] = SpotVisibility(visibleSinceMs = now)

        feedEventService.recordEvent(
            spotId = spotId,
            eventType = FeedEventType.IMPRESSION,
            coalesceKey = "impression:$spotId",
        )

        timerJobs[spotId] = scope.launch {
            delay(Constants.FeedEvents.VISIBLE_2S_MS)
            val state = visibleSpots[spotId] ?: return@launch
            if (state.visible2sEmitted) return@launch

            state.visible2sEmitted = true
            feedEventService.recordEvent(
                spotId = spotId,
                eventType = FeedEventType.VISIBLE_2S,
                coalesceKey = "visible_2s:$spotId",
            )

            delay(Constants.FeedEvents.LONG_DWELL_MS - Constants.FeedEvents.VISIBLE_2S_MS)
            val dwellState = visibleSpots[spotId] ?: return@launch
            if (dwellState.longDwellEmitted) return@launch

            dwellState.longDwellEmitted = true
            val dwellMs = (System.currentTimeMillis() - dwellState.visibleSinceMs).toInt()
            feedEventService.recordEvent(
                spotId = spotId,
                eventType = FeedEventType.LONG_DWELL,
                dwellMs = dwellMs,
                coalesceKey = "long_dwell:$spotId",
            )
        }
    }

    private fun onSpotHidden(spotId: String) {
        val state = visibleSpots.remove(spotId) ?: return
        timerJobs.remove(spotId)?.cancel()

        val dwellMs = (System.currentTimeMillis() - state.visibleSinceMs).toInt()
        if (!state.visible2sEmitted && dwellMs < Constants.FeedEvents.QUICK_SKIP_MAX_MS) {
            feedEventService.recordEvent(
                spotId = spotId,
                eventType = FeedEventType.QUICK_SKIP,
                dwellMs = dwellMs,
                coalesceKey = "quick_skip:$spotId",
            )
        }
    }
}
