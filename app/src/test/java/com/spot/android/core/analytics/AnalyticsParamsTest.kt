package com.spot.android.core.analytics

import com.spot.android.core.util.Constants
import org.junit.Assert.assertEquals
import org.junit.Test

class AnalyticsParamsTest {

    @Test
    fun `analyticsParams converts values to strings and drops nulls`() {
        val params = analyticsParams(
            mapOf(
                Constants.Analytics.Params.ORIGIN to DeepLinkOrigin.AppLink.analyticsValue,
                Constants.Analytics.Params.ROUTE to "/s/spot-id",
                "ignored" to null,
            ),
        )

        assertEquals(
            mapOf(
                Constants.Analytics.Params.ORIGIN to "app_link",
                Constants.Analytics.Params.ROUTE to "/s/spot-id",
            ),
            params,
        )
    }
}
