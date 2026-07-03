package com.spot.android.navigation

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TabReselectBusTest {

    private lateinit var bus: TabReselectBus

    @Before
    fun setup() {
        bus = TabReselectBus()
    }

    @Test
    fun `onTabReselected emits the tab`() = runTest {
        bus.reselectEvents.test {
            bus.onTabReselected(SpotTab.Home)
            assertEquals(SpotTab.Home, awaitItem())

            bus.onTabReselected(SpotTab.Map)
            assertEquals(SpotTab.Map, awaitItem())
        }
    }

    @Test
    fun `reselect events are emitted for all tabs`() = runTest {
        bus.reselectEvents.test {
            SpotTab.entries.forEach { tab ->
                bus.onTabReselected(tab)
                assertEquals(tab, awaitItem())
            }
        }
    }

    @Test
    fun `multiple rapid reselects emit events`() = runTest {
        bus.reselectEvents.test {
            bus.onTabReselected(SpotTab.Home)
            bus.onTabReselected(SpotTab.Map)
            assertEquals(SpotTab.Home, awaitItem())
            assertEquals(SpotTab.Map, awaitItem())
        }
    }
}
