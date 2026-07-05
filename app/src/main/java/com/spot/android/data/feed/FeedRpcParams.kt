package com.spot.android.data.feed

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
internal data class GetHomeFeedRpcParams(
    val p_limit: Int,
    val p_viewer_lat: Double? = null,
    val p_viewer_lng: Double? = null,
    val p_batch_id: String,
    val p_force_seen_fallback: Boolean = false,
)

@Serializable
internal data class RecordFeedEventRpcParams(
    val p_spot_id: String,
    val p_event_type: String,
    val p_dwell_ms: Int? = null,
    val p_metadata: JsonObject = JsonObject(emptyMap()),
)
