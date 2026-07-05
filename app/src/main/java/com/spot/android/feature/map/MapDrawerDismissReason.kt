package com.spot.android.feature.map

/**
 * Reasons the map spot drawer was dismissed.
 *
 * Reference: PRD/07-map.md
 */
enum class MapDrawerDismissReason {
    CLOSE_BUTTON,
    MAP_MOVED,
    EMPTY_MAP_TAP,
    FILTER_CHANGED,
    SELECTED_SPOT_NO_LONGER_VISIBLE,
    SPOT_SWITCH,
    TAB_LEFT,
    TAB_RESELECTED,
}
