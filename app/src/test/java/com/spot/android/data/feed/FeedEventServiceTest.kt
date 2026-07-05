package com.spot.android.data.feed

import com.spot.android.core.logging.FakeLogWriter
import com.spot.android.core.logging.SpotLogger
import com.spot.android.core.supabase.SupabaseClientProvider
import com.spot.android.core.util.Constants
import com.spot.android.data.model.enums.FeedEventType
import io.github.jan.supabase.gotrue.Auth
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FeedEventServiceTest {

    private val testScheduler = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(testScheduler)
    private lateinit var mockProvider: SupabaseClientProvider
    private lateinit var recordedEvents: MutableList<RecordFeedEventRpcParams>
    private lateinit var service: FeedEventServiceDelegate

    @Before
    fun setup() {
        recordedEvents = mutableListOf()
        val mockAuth = mockk<Auth>(relaxed = true)
        mockProvider = mockk(relaxed = true)
        every { mockProvider.auth } returns mockAuth

        val logger = SpotLogger(
            preferencesRepository = com.spot.android.core.logging.FakeLogPreferencesRepository(),
            logWriter = FakeLogWriter(),
        )
        service = FeedEventServiceDelegate(
            supabaseProvider = mockProvider,
            logger = logger,
            dispatcher = testDispatcher,
            onEventRecorded = { recordedEvents.add(it) },
        )
    }

    @Test
    fun `recordEvent captures action events`() = runTest(testScheduler) {
        service.recordEvent("spot-1", FeedEventType.LIKE)
        advanceUntilIdle()

        assertEquals(1, recordedEvents.size)
        assertEquals("spot-1", recordedEvents.first().p_spot_id)
        assertEquals("like", recordedEvents.first().p_event_type)
    }

    @Test
    fun `recordEvent coalesces duplicate visibility keys`() = runTest(testScheduler) {
        service.recordEvent(
            spotId = "spot-1",
            eventType = FeedEventType.IMPRESSION,
            coalesceKey = "impression:spot-1",
        )
        service.recordEvent(
            spotId = "spot-1",
            eventType = FeedEventType.IMPRESSION,
            coalesceKey = "impression:spot-1",
        )
        advanceUntilIdle()

        assertEquals(1, recordedEvents.size)
        assertEquals("impression", recordedEvents.first().p_event_type)
    }

    @Test
    fun `recordEvent does not coalesce repeated action events`() = runTest(testScheduler) {
        service.recordEvent("spot-1", FeedEventType.LIKE)
        service.recordEvent("spot-1", FeedEventType.LIKE)
        advanceUntilIdle()

        assertEquals(2, recordedEvents.size)
    }

    @Test
    fun `resetSession allows visibility event to emit again`() = runTest(testScheduler) {
        service.recordEvent(
            spotId = "spot-1",
            eventType = FeedEventType.IMPRESSION,
            coalesceKey = "impression:spot-1",
        )
        advanceUntilIdle()

        service.resetSession()
        advanceUntilIdle()

        service.recordEvent(
            spotId = "spot-1",
            eventType = FeedEventType.IMPRESSION,
            coalesceKey = "impression:spot-1",
        )
        advanceUntilIdle()

        assertEquals(2, recordedEvents.size)
    }

    @Test
    fun `feed event timing constants match PRD guidance`() {
        assertEquals(2_000L, Constants.FeedEvents.VISIBLE_2S_MS)
        assertEquals(8_000L, Constants.FeedEvents.LONG_DWELL_MS)
        assertEquals(1_500L, Constants.FeedEvents.QUICK_SKIP_MAX_MS)
    }
}
