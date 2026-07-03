package com.spot.android.core.analytics

/**
 * Deep-link origin tags for analytics.
 *
 * Reference: PRD/15-deep-links.md
 */
enum class DeepLinkOrigin(val analyticsValue: String) {
    AppLink("app_link"),
    CustomScheme("custom_scheme"),
}
