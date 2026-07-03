package com.spot.android.core.analytics

/**
 * Converts analytics parameter maps into Firebase-safe string values.
 */
internal fun analyticsParams(params: Map<String, Any?>): Map<String, String> {
    return buildMap {
        params.forEach { (key, value) ->
            if (value != null) {
                put(key, value.toString())
            }
        }
    }
}
