package com.spot.android.data.search

import java.util.UUID

/**
 * Local search history entry persisted per segment.
 *
 * Reference: PRD/09-search.md
 */
enum class SearchHistoryType(val rawValue: String) {
    User("user"),
    Location("location"),
    Vibe("vibe"),
}

data class SearchHistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val type: SearchHistoryType,
    val query: String,
    val displayText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val vibeTagIds: List<String> = emptyList(),
)
