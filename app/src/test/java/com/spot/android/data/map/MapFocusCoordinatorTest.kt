package com.spot.android.data.map

import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test

class MapFocusCoordinatorTest {

    @Test
    fun `openInMap sets pendingFocus and pendingHomeReturnScroll`() {
        val coordinator = MapFocusCoordinator()

        coordinator.openInMap(
            spotId = "spot-1",
            target = LatLng(37.7749, -122.4194),
            now = 42L,
        )

        val focus = coordinator.pendingFocus.value
        assertNotNull(focus)
        assertEquals("spot-1", focus!!.spotId)
        assertEquals(37.7749, focus.target.latitude, 0.0001)
        assertEquals(-122.4194, focus.target.longitude, 0.0001)
        assertEquals(42L, focus.timestamp)
        assertEquals("spot-1", coordinator.pendingHomeReturnScroll.value)
    }

    @Test
    fun `consumeFocus clears pendingFocus but leaves return scroll`() {
        val coordinator = MapFocusCoordinator()
        coordinator.openInMap("spot-1", LatLng(1.0, 1.0))

        coordinator.consumeFocus()

        assertNull(coordinator.pendingFocus.value)
        assertEquals("spot-1", coordinator.pendingHomeReturnScroll.value)
    }

    @Test
    fun `consumeHomeReturnScroll returns and clears pending id`() {
        val coordinator = MapFocusCoordinator()
        coordinator.openInMap("spot-1", LatLng(1.0, 1.0))

        val consumed = coordinator.consumeHomeReturnScroll()

        assertEquals("spot-1", consumed)
        assertNull(coordinator.pendingHomeReturnScroll.value)
    }

    @Test
    fun `consumeHomeReturnScroll returns null when nothing pending`() {
        val coordinator = MapFocusCoordinator()
        assertNull(coordinator.consumeHomeReturnScroll())
    }

    @Test
    fun `multiple openInMap calls overwrite previous focus`() {
        val coordinator = MapFocusCoordinator()

        coordinator.openInMap("spot-1", LatLng(1.0, 2.0), now = 1L)
        coordinator.openInMap("spot-2", LatLng(3.0, 4.0), now = 2L)

        val focus = coordinator.pendingFocus.value!!
        assertEquals("spot-2", focus.spotId)
        assertEquals(2L, focus.timestamp)
        assertEquals("spot-2", coordinator.pendingHomeReturnScroll.value)
    }
}
