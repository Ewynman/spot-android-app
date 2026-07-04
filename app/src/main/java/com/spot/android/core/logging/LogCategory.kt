package com.spot.android.core.logging

/**
 * Structured log categories mirroring iOS `SpotLogger` areas.
 *
 * Reference: PRD/17-non-functional-testing.md, PRD/11-settings.md
 */
enum class LogCategory {
    Feed,
    Auth,
    Network,
    Post,
    Map,
    DeepLink,
    Privacy,
    SpotCard,
    Billing,
}
