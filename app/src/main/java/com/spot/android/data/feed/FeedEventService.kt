package com.spot.android.data.feed

import com.spot.android.core.logging.LogCategory
import com.spot.android.core.logging.SpotLogger
import com.spot.android.core.supabase.SupabaseClientProvider
import com.spot.android.data.model.enums.FeedEventType
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Lightweight coalescing emitter for feed behavioral events.
 *
 * Reference: PRD/16-feed-ranking-algorithm.md, PRD/06-home-feed.md
 */
@Singleton
class FeedEventService @Inject constructor(
    private val supabaseProvider: SupabaseClientProvider,
    private val logger: SpotLogger,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()
    private val recentKeys = LinkedHashSet<String>()

    private val postgrest get() = supabaseProvider.client.postgrest

    fun recordEvent(
        spotId: String,
        eventType: FeedEventType,
        dwellMs: Int? = null,
        coalesceKey: String? = null,
    ) {
        scope.launch {
            val key = coalesceKey ?: "${spotId}:${eventType.value}"
            if (!shouldEmit(key)) return@launch

            try {
                postgrest.rpc(
                    function = "record_feed_event_v1",
                    parameters = RecordFeedEventRpcParams(
                        p_spot_id = spotId,
                        p_event_type = eventType.value,
                        p_dwell_ms = dwellMs,
                    ),
                )
            } catch (e: Exception) {
                logger.w(LogCategory.Feed, TAG, "Failed to record feed event", e)
            }
        }
    }

    private suspend fun shouldEmit(key: String): Boolean {
        return mutex.withLock {
            if (recentKeys.contains(key)) {
                false
            } else {
                recentKeys.add(key)
                if (recentKeys.size > MAX_COALESCE_ENTRIES) {
                    val iterator = recentKeys.iterator()
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
        const val MAX_COALESCE_ENTRIES = 256
    }
}
