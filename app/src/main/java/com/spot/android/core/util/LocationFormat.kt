package com.spot.android.core.util

/**
 * Formats a raw location string into a compact "City, State/Region" label,
 * matching iOS SpotCard.cityState(from:).
 */
fun cityStateFromLocation(raw: String): String {
    val disallowed = setOf(
        "united states",
        "usa",
        "us",
        "united states of america",
    )
    val parts = raw
        .split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .filter { segment ->
            val lower = segment.lowercase()
            if (lower in disallowed) return@filter false
            segment.none { it.isDigit() }
        }

    return when {
        parts.size >= 2 -> parts.takeLast(2).joinToString(", ")
        parts.isNotEmpty() -> parts.first()
        else -> raw
    }
}
