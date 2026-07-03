package com.spot.android.core.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.spot.android.core.util.Constants
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FirebaseAnalyticsTrackerTest {

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var bundleFactory: AnalyticsBundleFactory
    private lateinit var tracker: FirebaseAnalyticsTracker

    @Before
    fun setup() {
        firebaseAnalytics = mockk(relaxed = true)
        bundleFactory = mockk(relaxed = true)
        tracker = FirebaseAnalyticsTracker(firebaseAnalytics, bundleFactory)
    }

    @Test
    fun `trackDeepLink sends origin and route params`() {
        val eventName = slot<String>()
        val params = slot<Map<String, String>>()
        val bundle = mockk<Bundle>(relaxed = true)

        every { bundleFactory.create(capture(params)) } returns bundle
        every { firebaseAnalytics.logEvent(capture(eventName), bundle) } returns Unit

        tracker.trackDeepLink(
            origin = DeepLinkOrigin.AppLink,
            route = "/s/spot-id",
        )

        assertEquals(Constants.Analytics.DEEP_LINK, eventName.captured)
        assertEquals("app_link", params.captured[Constants.Analytics.Params.ORIGIN])
        assertEquals("/s/spot-id", params.captured[Constants.Analytics.Params.ROUTE])
    }

    @Test
    fun `trackPermissionsRequested sends permission type`() {
        val params = slot<Map<String, String>>()

        every { bundleFactory.create(capture(params)) } returns mockk<Bundle>(relaxed = true)

        tracker.trackPermissionsRequested("location")

        assertEquals("location", params.captured[Constants.Analytics.Params.PERMISSION_TYPE])
        verify { firebaseAnalytics.logEvent(Constants.Analytics.PERMS_REQUESTED, any()) }
    }

    @Test
    fun `trackAuthReinstall uses canonical event name`() {
        tracker.trackAuthReinstall()

        verify { firebaseAnalytics.logEvent(Constants.Analytics.AUTH_REINSTALL, any()) }
    }

    @Test
    fun `trackImageLoadFailed sends source param`() {
        val params = slot<Map<String, String>>()

        every { bundleFactory.create(capture(params)) } returns mockk<Bundle>(relaxed = true)

        tracker.trackImageLoadFailed("spot_card")

        assertEquals("spot_card", params.captured[Constants.Analytics.Params.SOURCE])
    }
}
