package com.spot.android.data.deeplink

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeepLinkRouterTest {
    @Test
    fun parsesHttpsSpotLink() {
        val route = DeepLinkRouter.parse("https://spotapp.online/s/abc_123")
        assertEquals(DeepLinkRoute.SpotDetail("abc_123"), route)
    }

    @Test
    fun parsesWwwSpotLink() {
        val route = DeepLinkRouter.parse("https://www.spotapp.online/s/spot-1")
        assertEquals(DeepLinkRoute.SpotDetail("spot-1"), route)
    }

    @Test
    fun parsesCustomSchemeSpotHost() {
        val route = DeepLinkRouter.parse("spotapp://spot/abc_123")
        assertEquals(DeepLinkRoute.SpotDetail("abc_123"), route)
    }

    @Test
    fun parsesCustomSchemeQuery() {
        val route = DeepLinkRouter.parse("spotapp://open?spotId=abc_123")
        assertEquals(DeepLinkRoute.SpotDetail("abc_123"), route)
    }

    @Test
    fun parsesSubscriptionReturn() {
        val route = DeepLinkRouter.parse("spotapp://subscription/return")
        assertEquals(DeepLinkRoute.SubscriptionReturn, route)
    }

    @Test
    fun rejectsInvalidSpotId() {
        val route = DeepLinkRouter.parse("https://spotapp.online/s/bad id!")
        assertEquals(DeepLinkRoute.Unknown, route)
    }

    @Test
    fun validatesSpotIdRules() {
        assertTrue(DeepLinkRouter.isValidSpotId("a"))
        assertTrue(!DeepLinkRouter.isValidSpotId(""))
        assertTrue(!DeepLinkRouter.isValidSpotId("x".repeat(51)))
    }
}
