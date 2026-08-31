package com.spot.android.core.media

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.spot.android.core.logging.LogCategory
import com.spot.android.core.logging.SpotLogger
import com.spot.android.core.util.Constants
import com.spot.android.feature.map.PhotoPinGeometry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bounded LRU cache for map-marker preview bitmaps.
 *
 * Photo pins reload aggressively during pan/zoom, so we keep bitmaps small
 * (downsampled to ≤ [Constants.PhotoPin.IMAGE_TARGET_MAX_PX] on the short
 * edge) and cap the cache at [Constants.PhotoPin.IMAGE_CACHE_MAX_ENTRIES]
 * entries. Concurrent requests for the same URL share a single [Deferred] so
 * we never fan out network calls under rapid gestures. See task 12.
 */
interface MapMarkerImageCache {

    /**
     * Load a marker bitmap for [url] downsampled to [targetPx] on the short
     * edge. Returns [MapMarkerImageLoadResult.Failure] on any error;
     * consumers should show the teardrop fallback in that case.
     */
    suspend fun load(url: String, targetPx: Int): MapMarkerImageLoadResult

    /** Clear the cache in response to [android.content.ComponentCallbacks2.onTrimMemory]. */
    fun onTrimMemory()

    /** Manual clear (mostly for tests). */
    fun clear()

    /** Current entry count. Useful for tests. */
    fun size(): Int
}

sealed interface MapMarkerImageLoadResult {
    data class Success(val bitmap: Bitmap, val cacheHit: Boolean) : MapMarkerImageLoadResult
    data object Failure : MapMarkerImageLoadResult
}

/**
 * Fetch + decode seam. Kept separate so unit tests can substitute a stub
 * without an HTTP roundtrip and without needing a real [Bitmap] decode.
 */
interface MarkerBitmapSource {
    suspend fun fetch(url: String, targetPx: Int): Bitmap?
}

@Singleton
class AndroidMapMarkerImageCache @Inject constructor(
    private val bitmapSource: MarkerBitmapSource,
    private val logger: SpotLogger,
) : MapMarkerImageCache {

    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val lru = BoundedLruMap<String, Bitmap>(Constants.PhotoPin.IMAGE_CACHE_MAX_ENTRIES)
    private val inFlight = HashMap<String, Deferred<MapMarkerImageLoadResult>>()
    private val mutex = Mutex()

    override suspend fun load(url: String, targetPx: Int): MapMarkerImageLoadResult {
        if (url.isBlank()) return MapMarkerImageLoadResult.Failure

        val cached: Bitmap? = mutex.withLock { lru[url] }
        if (cached != null) {
            return MapMarkerImageLoadResult.Success(cached, cacheHit = true)
        }

        val deferred: Deferred<MapMarkerImageLoadResult> = mutex.withLock {
            inFlight[url]?.let { return@withLock it }
            val d = ioScope.async {
                val bitmap = runCatching { bitmapSource.fetch(url, targetPx) }
                    .onFailure { logger.w(LogCategory.Map, TAG, "marker fetch failed", it) }
                    .getOrNull()
                if (bitmap != null) {
                    mutex.withLock { lru.put(url, bitmap) }
                    MapMarkerImageLoadResult.Success(bitmap, cacheHit = false)
                } else {
                    MapMarkerImageLoadResult.Failure
                }
            }
            inFlight[url] = d
            d
        }

        return try {
            deferred.await()
        } finally {
            mutex.withLock { if (inFlight[url] === deferred) inFlight.remove(url) }
        }
    }

    override fun onTrimMemory() {
        // Non-suspending best-effort clear; we don't block callers on the mutex.
        synchronized(lru) { lru.clear() }
    }

    override fun clear() {
        synchronized(lru) { lru.clear() }
    }

    override fun size(): Int = synchronized(lru) { lru.size }

    private companion object {
        const val TAG = "MapMarkerImageCache"
    }
}

/**
 * Default HTTP + BitmapFactory implementation of [MarkerBitmapSource].
 */
@Singleton
class HttpMarkerBitmapSource @Inject constructor(
    private val logger: SpotLogger,
) : MarkerBitmapSource {

    override suspend fun fetch(url: String, targetPx: Int): Bitmap? =
        withContext(Dispatchers.IO) {
            val bytes = runCatching { downloadBytes(url) }
                .onFailure { logger.w(LogCategory.Map, TAG, "marker download failed", it) }
                .getOrNull()
                ?: return@withContext null
            runCatching { decodeDownsampled(bytes, targetPx) }
                .onFailure { logger.w(LogCategory.Map, TAG, "marker decode failed", it) }
                .getOrNull()
        }

    private fun downloadBytes(url: String): ByteArray? {
        val connection = URL(url).openConnection() as HttpURLConnection
        return try {
            connection.connectTimeout = HTTP_CONNECT_TIMEOUT_MS
            connection.readTimeout = HTTP_READ_TIMEOUT_MS
            connection.instanceFollowRedirects = true
            connection.requestMethod = "GET"
            val code = connection.responseCode
            if (code !in 200..299) {
                logger.w(LogCategory.Map, TAG, "marker fetch non-2xx: $code")
                return null
            }
            connection.inputStream.use { input ->
                val buffer = ByteArrayOutputStream()
                val chunk = ByteArray(BUFFER_SIZE)
                while (true) {
                    val read = input.read(chunk)
                    if (read == -1) break
                    buffer.write(chunk, 0, read)
                }
                buffer.toByteArray()
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun decodeDownsampled(bytes: ByteArray, targetPx: Int): Bitmap? {
        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        ByteArrayInputStream(bytes).use { BitmapFactory.decodeStream(it, null, boundsOptions) }
        if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) return null

        val sampleOptions = BitmapFactory.Options().apply {
            inSampleSize = PhotoPinGeometry.computeInSampleSize(
                sourceWidth = boundsOptions.outWidth,
                sourceHeight = boundsOptions.outHeight,
                targetPx = targetPx,
            )
            inPreferredConfig = Bitmap.Config.RGB_565
        }
        return ByteArrayInputStream(bytes).use {
            BitmapFactory.decodeStream(it, null, sampleOptions)
        }
    }

    private companion object {
        const val TAG = "MapMarkerImageCache"
        const val HTTP_CONNECT_TIMEOUT_MS = 5_000
        const val HTTP_READ_TIMEOUT_MS = 5_000
        const val BUFFER_SIZE = 8 * 1024
    }
}

/**
 * JVM-native access-order LRU. We roll our own instead of using
 * [android.util.LruCache] so it's directly usable from plain unit tests.
 */
internal class BoundedLruMap<K, V>(
    private val maxEntries: Int,
) : LinkedHashMap<K, V>(maxEntries, LOAD_FACTOR, /* accessOrder = */ true) {

    override fun removeEldestEntry(eldest: Map.Entry<K, V>): Boolean {
        return size > maxEntries
    }

    private companion object {
        const val LOAD_FACTOR = 0.75f
    }
}
