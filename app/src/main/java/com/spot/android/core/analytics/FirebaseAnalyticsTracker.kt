package com.spot.android.core.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.spot.android.core.util.Constants
import javax.inject.Inject
import javax.inject.Singleton

interface AnalyticsBundleFactory {
    fun create(params: Map<String, String>): Bundle
}

class AndroidAnalyticsBundleFactory @Inject constructor() : AnalyticsBundleFactory {
    override fun create(params: Map<String, String>): Bundle {
        return Bundle(params.size).also { bundle ->
            params.forEach { (key, value) ->
                bundle.putString(key, value)
            }
        }
    }
}

@Singleton
class FirebaseAnalyticsTracker @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics,
    private val bundleFactory: AnalyticsBundleFactory,
) : AnalyticsTracker {

    override fun logEvent(name: String, params: Map<String, Any?>) {
        firebaseAnalytics.logEvent(name, bundleFactory.create(analyticsParams(params)))
    }

    override fun trackAuthReinstall() {
        logEvent(Constants.Analytics.AUTH_REINSTALL)
    }

    override fun trackPermissionsRequested(permissionType: String) {
        logEvent(
            name = Constants.Analytics.PERMS_REQUESTED,
            params = mapOf(Constants.Analytics.Params.PERMISSION_TYPE to permissionType),
        )
    }

    override fun trackFeedDropPrivate(reason: String) {
        logEvent(
            name = Constants.Analytics.FEED_DROP_PRIVATE,
            params = mapOf(Constants.Analytics.Params.REASON to reason),
        )
    }

    override fun trackImageLoadFailed(source: String) {
        logEvent(
            name = Constants.Analytics.IMAGE_LOAD_FAILED,
            params = mapOf(Constants.Analytics.Params.SOURCE to source),
        )
    }

    override fun trackAuthEmailInUse() {
        logEvent(Constants.Analytics.AUTH_EMAIL_IN_USE)
    }

    override fun trackAuthDeleteByEmail() {
        logEvent(Constants.Analytics.AUTH_DELETE_BY_EMAIL)
    }

    override fun trackDeepLink(origin: DeepLinkOrigin, route: String) {
        logEvent(
            name = Constants.Analytics.DEEP_LINK,
            params = mapOf(
                Constants.Analytics.Params.ORIGIN to origin.analyticsValue,
                Constants.Analytics.Params.ROUTE to route,
            ),
        )
    }
}
