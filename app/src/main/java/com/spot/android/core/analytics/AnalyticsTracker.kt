package com.spot.android.core.analytics

/**
 * Analytics abstraction for Firebase Analytics.
 *
 * Keeps feature code decoupled from Firebase and testable with fakes.
 * Never include PII in event parameters.
 *
 * Reference: PRD/17-non-functional-testing.md
 */
interface AnalyticsTracker {
    fun logEvent(name: String, params: Map<String, Any?> = emptyMap())

    fun trackAuthReinstall()
    fun trackPermissionsRequested(permissionType: String)
    fun trackFeedDropPrivate(reason: String)
    fun trackImageLoadFailed(source: String)
    fun trackAuthEmailInUse()
    fun trackAuthDeleteByEmail()
    fun trackDeepLink(origin: DeepLinkOrigin, route: String)
}
