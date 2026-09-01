package com.spot.android.core.media

import android.graphics.Bitmap
import com.spot.android.core.logging.FakeLogPreferencesRepository
import com.spot.android.core.logging.FakeLogWriter
import com.spot.android.core.logging.SpotLogger
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

@OptIn(ExperimentalCoroutinesApi::class)
class MapMarkerImageCacheTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var logger: SpotLogger

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        logger = SpotLogger(
            preferencesRepository = FakeLogPreferencesRepository(),
            logWriter = FakeLogWriter(),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `blank url returns Failure without hitting source`() = runTest {
        val source = CountingBitmapSource { _, _ -> mockk<Bitmap>(relaxed = true) }
        val cache = AndroidMapMarkerImageCache(source, logger)

        val result = cache.load("", targetPx = 88)
        assertEquals(MapMarkerImageLoadResult.Failure, result)
        assertEquals(0, source.fetchCount.get())
    }

    @Test
    fun `first load hits source and second load reports cache hit without re-fetch`() = runTest {
        val bitmap = mockk<Bitmap>(relaxed = true)
        val source = CountingBitmapSource { _, _ -> bitmap }
        val cache = AndroidMapMarkerImageCache(source, logger)

        val first = cache.load(URL, targetPx = 88)
        assertTrue(first is MapMarkerImageLoadResult.Success)
        assertEquals(false, (first as MapMarkerImageLoadResult.Success).cacheHit)

        val second = cache.load(URL, targetPx = 88)
        assertTrue(second is MapMarkerImageLoadResult.Success)
        assertEquals(true, (second as MapMarkerImageLoadResult.Success).cacheHit)

        assertEquals(1, source.fetchCount.get())
        assertEquals(1, cache.size())
    }

    @Test
    fun `concurrent loads of the same url share a single fetch`() = runTest {
        val gate = CompletableDeferred<Unit>()
        val bitmap = mockk<Bitmap>(relaxed = true)
        val source = CountingBitmapSource { _, _ ->
            gate.await()
            bitmap
        }
        val cache = AndroidMapMarkerImageCache(source, logger)

        val jobs = List(5) { async { cache.load(URL, targetPx = 88) } }
        advanceUntilIdle()
        gate.complete(Unit)
        val results = jobs.awaitAll()

        assertEquals(5, results.size)
        assertTrue(results.all { it is MapMarkerImageLoadResult.Success })
        assertEquals(1, source.fetchCount.get())
    }

    @Test
    fun `failed fetch returns Failure and does not populate cache`() = runTest {
        val source = CountingBitmapSource { _, _ -> null }
        val cache = AndroidMapMarkerImageCache(source, logger)

        val result = cache.load(URL, targetPx = 88)

        assertEquals(MapMarkerImageLoadResult.Failure, result)
        assertEquals(0, cache.size())
    }

    @Test
    fun `onTrimMemory clears cached bitmaps`() = runTest {
        val bitmap = mockk<Bitmap>(relaxed = true)
        val source = CountingBitmapSource { _, _ -> bitmap }
        val cache = AndroidMapMarkerImageCache(source, logger)

        cache.load(URL, targetPx = 88)
        assertEquals(1, cache.size())

        cache.onTrimMemory()
        assertEquals(0, cache.size())
    }

    @Test
    fun `cancelling one caller does not cancel other concurrent callers`() = runTest {
        val gate = CompletableDeferred<Unit>()
        val bitmap = mockk<Bitmap>(relaxed = true)
        val source = CountingBitmapSource { _, _ ->
            gate.await()
            bitmap
        }
        val cache = AndroidMapMarkerImageCache(source, logger)

        val holder = mutableListOf<MapMarkerImageLoadResult>()
        val loser = launch { holder.add(cache.load(URL, targetPx = 88)) }
        val winner = async { cache.load(URL, targetPx = 88) }

        advanceUntilIdle()
        loser.cancelAndJoin()
        gate.complete(Unit)
        val winnerResult = winner.await()

        assertTrue(winnerResult is MapMarkerImageLoadResult.Success)
        assertEquals(1, source.fetchCount.get())
    }

    @Test
    fun `bounded LRU evicts oldest entries when past capacity`() {
        val lru = BoundedLruMap<String, String>(maxEntries = 3)
        lru.put("a", "1")
        lru.put("b", "2")
        lru.put("c", "3")
        // access "a" so "b" becomes eldest
        lru["a"]
        lru.put("d", "4")

        assertEquals(3, lru.size)
        assertEquals(true, lru.containsKey("a"))
        assertEquals(false, lru.containsKey("b"))
        assertEquals(true, lru.containsKey("c"))
        assertEquals(true, lru.containsKey("d"))
    }

    private class CountingBitmapSource(
        private val loader: suspend (String, Int) -> Bitmap?,
    ) : MarkerBitmapSource {

        val fetchCount = AtomicInteger(0)

        override suspend fun fetch(url: String, targetPx: Int): Bitmap? {
            fetchCount.incrementAndGet()
            return loader(url, targetPx)
        }
    }

    private companion object {
        const val URL = "https://example.com/spot.jpg"
    }
}
