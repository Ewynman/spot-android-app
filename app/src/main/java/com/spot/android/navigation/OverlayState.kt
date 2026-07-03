package com.spot.android.navigation

/**
 * Top-level overlay state for content shown above the tab shell.
 *
 * Overlays float over the current tab and are not tabs themselves.
 * Full implementations will be wired in later phases (deep links, billing).
 *
 * Reference: PRD/00-overview.md, PRD/15-deep-links.md, PRD/12-pro-subscription.md
 */
sealed interface AppOverlay {
    data object None : AppOverlay

    /** Full-screen spot detail from a deep link or in-app navigation. */
    data class SpotDetail(val spotId: String) : AppOverlay

    /** Loading state while fetching a deep-linked spot. */
    data class SpotLoading(val spotId: String) : AppOverlay

    /** Spot unavailable (not found, blocked, private, network error). */
    data class SpotUnavailable(val spotId: String) : AppOverlay

    /** Post-purchase / subscription-return success screen. */
    data object ProSuccess : AppOverlay

    /** Pro upsell sheet. [entryPoint] identifies the trigger for analytics. */
    data class Paywall(val entryPoint: String? = null) : AppOverlay
}
