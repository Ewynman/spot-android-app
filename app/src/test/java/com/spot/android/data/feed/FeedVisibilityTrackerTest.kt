package com.spot.android.data.feed

import com.spot.android.core.util.Constants
import com.spot.android.data.model.enums.FeedEventType
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FeedVisibilityTrackerTest {

    private val testScheduler = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(testScheduler)
    private lateinit var feedEventService: FeedEventService
    private lateinit var tracker: FeedVisibilityTrackerDelegate

    @Before
    fun setup() {
        feedEventService = mockk(relaxed = true)
        tracker = FeedVisibilityTrackerDelegate(feedEventService, testDispatcher)
    }

    @Test
    fun `becoming visible emits impression`() = runTest(testScheduler) {
        tracker.syncVisibleSpotIds(setOf("spot-1"))

        verify {
            feedEventService.recordEvent(
                spotId = "spot-1",
                eventType = FeedEventType.IMPRESSION,
                coalesceKey = "impression:spot-1",
            )
        }
    }

    @Test
    fun `visible for 2 seconds emits visible_2s`() = runTest(testScheduler) {
        tracker.syncVisibleSpotIds(setOf("spot-1"))
        advanceTimeBy(Constants.FeedEvents.VISIBLE_2S_MS)
        advanceUntilIdle()

        verify {
            feedEventService.recordEvent(
                spotId = "spot-1",
                eventType = FeedEventType.VISIBLE_2S,
                coalesceKey = "visible_2s:spot-1",
            )
        }
    }

    @Test
    fun `quick scroll away emits quick_skip`() = runTest(testScheduler) {
        tracker.syncVisibleSpotIds(setOf("spot-1"))
        advanceTimeBy(500)
        tracker.syncVisibleSpotIds(emptySet())

        verify {
            feedEventService.recordEvent(
                spotId = "spot-1",
                eventType = FeedEventType.QUICK_SKIP,
                dwellMs = any(),
                metadata = any(),
                coalesceKey = "quick_skip:spot-1",
            )
        }
    }

    @Test
    fun `long dwell emits after threshold`() = runTest(testScheduler) {
        tracker.syncVisibleSpotIds(setOf("spot-1"))
        advanceTimeBy(Constants.FeedEvents.LONG_DWELL_MS)
        advanceUntilIdle()

        verify {
            feedEventService.recordEvent(
                spotId = "spot-1",
                eventType = FeedEventType.LONG_DWELL,
                dwellMs = any(),
                metadata = any(),
                coalesceKey = "long_dwell:spot-1",
            )
        }
    }

    @Test
    fun `resetSession clears tracker and feed session`() = runTest(testScheduler) {
        tracker.syncVisibleSpotIds(setOf("spot-1"))
        tracker.resetSession()

        verify { feedEventService.resetSession() }
    }
}
