package com.spot.android.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class SpotTabTest {

    @Test
    fun `default tab is Home`() {
        assertEquals(SpotTab.Home, SpotTab.DEFAULT)
    }

    @Test
    fun `Home is index 0`() {
        assertEquals(0, SpotTab.Home.index)
    }

    @Test
    fun `tab indices are sequential 0 through 4`() {
        SpotTab.entries.forEachIndexed { index, tab ->
            assertEquals(index, tab.index)
        }
    }

    @Test
    fun `fromRoute returns correct tab`() {
        assertEquals(SpotTab.Home, SpotTab.fromRoute(SpotRoutes.HOME))
        assertEquals(SpotTab.Map, SpotTab.fromRoute(SpotRoutes.MAP))
        assertEquals(SpotTab.Post, SpotTab.fromRoute(SpotRoutes.POST))
        assertEquals(SpotTab.Search, SpotTab.fromRoute(SpotRoutes.SEARCH))
        assertEquals(SpotTab.Profile, SpotTab.fromRoute(SpotRoutes.PROFILE))
    }

    @Test
    fun `fromRoute returns null for unknown route`() {
        assertEquals(null, SpotTab.fromRoute("unknown"))
        assertEquals(null, SpotTab.fromRoute(null))
    }

    @Test
    fun `test tags match iOS vocabulary`() {
        assertEquals("navigation.homeTab", SpotTab.Home.testTag)
        assertEquals("navigation.mapTab", SpotTab.Map.testTag)
        assertEquals("navigation.postTab", SpotTab.Post.testTag)
        assertEquals("navigation.searchTab", SpotTab.Search.testTag)
        assertEquals("navigation.profileTab", SpotTab.Profile.testTag)
    }
}
