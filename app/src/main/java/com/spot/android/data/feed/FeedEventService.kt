package com.spot.android.data.feed

import com.spot.android.core.logging.LogCategory
import com.spot.android.core.logging.SpotLogger
import com.spot.android.core.supabase.SupabaseClientProvider
import com.spot.android.core.util.Constants
import com.spot.android.data.model.enums.FeedEventType
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonObject

/**
 * Lightweight coalescing emitter for feed behavioral events.
 *
 * Visibility events (`impression`, `visible_2s`, `long_dwell`, `quick_skip`) pass an explicit
 * [coalesceKey] so duplicates within a feed session are suppressed. Action events omit the key
 * and always emit so toggles like like/unlike remain accurate.
 *
 * Reference: PRD/16-feed-ranking-algorithm.md, PRD/06-home-feed.md
 */
@Singleton
class FeedEventService @Inject constructor(
    supabaseProvider: SupabaseClientProvider,
    logger: SpotLogger,
) {
    private val delegate = FeedEventServiceDelegate(
        supabaseProvider = supabaseProvider,
        logger = logger,
        dispatcher = Dispatchers.IO,
    )

    fun recordEvent(
        spotId: String,
        eventType: FeedEventType,
        dwellMs: Int? = null,
        metadata: JsonObject = JsonObject(emptyMap()),
        coalesceKey: String? = null,
    ) = delegate.recordEvent(
        spotId = spotId,
        eventType = eventType,
        dwellMs = dwellMs,
        metadata = metadata,
        coalesceKey = coalesceKey,
    )

    fun resetSession() = delegate.resetSession()
}

internal class FeedEventServiceDelegate(
    private val supabaseProvider: SupabaseClientProvider,
    private val logger: SpotLogger,
    dispatcher: CoroutineDispatcher,
    private val onEventRecorded: (RecordFeedEventRpcParams) -> Unit = {},
) {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val mutex = Mutex()
    private val coalescedKeys = LinkedHashSet<String>()

    private val postgrest get() = supabaseProvider.client.postgrest

    fun recordEvent(
        spotId: String,
        eventType: FeedEventType,
        dwellMs: Int? = null,
        metadata: JsonObject = JsonObject(emptyMap()),
        coalesceKey: String? = null,
    ) {
        scope.launch {
            if (coalesceKey != null && !shouldEmitCoalesced(coalesceKey)) {
                return@launch
            }

            try {
                val params = RecordFeedEventRpcParams(
                    p_spot_id = spotId,
                    p_event_type = eventType.value,
                    p_dwell_ms = dwellMs,
                    p_metadata = metadata,
                )
                onEventRecorded(params)
                postgrest.rpc(
                    function = "record_feed_event_v1",
                    parameters = params,
                )
            } catch (e: Exception) {
                logger.w(LogCategory.Feed, TAG, "Failed to record feed event", e)
            }
        }
    }

    fun resetSession() {
        scope.launch {
            mutex.withLock {
                coalescedKeys.clear()
            }
        }
    }

    private suspend fun shouldEmitCoalesced(key: String): Boolean {
        return mutex.withLock {
            if (coalescedKeys.contains(key)) {
                false
            } else {
                coalescedKeys.add(key)
                if (coalescedKeys.size > Constants.FeedEvents.COALESCE_MAX_ENTRIES) {
                    val iterator = coalescedKeys.iterator()
                    if (iterator.hasNext()) {
                        iterator.next()
                        iterator.remove()
                    }
                }
                true
            }
        }
    }

    private companion object {
        const val TAG = "FeedEventService"
    }
}
