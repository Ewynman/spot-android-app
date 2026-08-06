package com.spot.android.core.util

import org.junit.Assert.assertEquals
import org.junit.Test

class LocationFormatTest {
    @Test
    fun cityState_keepsCityAndState() {
        assertEquals("San Francisco, CA", cityStateFromLocation("San Francisco, CA"))
    }

    @Test
    fun cityState_stripsCountryAndStreetDigits() {
        assertEquals(
            "San Francisco, CA",
            cityStateFromLocation("123 Market St, San Francisco, CA, United States"),
        )
    }

    @Test
    fun cityState_singleSegment() {
        assertEquals("Tokyo", cityStateFromLocation("Tokyo"))
    }
}
