package com.spot.android.data.search

/**
 * Search tab segments mirroring iOS `Segment`.
 *
 * Reference: PRD/09-search.md
 */
enum class SearchSegment(val rawValue: String, val title: String) {
    Users("users", "Users"),
    Locations("locations", "Locations"),
    Vibes("vibes", "Vibes"),
}
