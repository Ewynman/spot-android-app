package com.spot.android.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OverlayHostViewModelTest {

    private lateinit var viewModel: OverlayHostViewModel

    @Before
    fun setup() {
        viewModel = OverlayHostViewModel()
    }

    @Test
    fun `initial overlay state is None`() {
        assertEquals(AppOverlay.None, viewModel.overlayState.value)
    }

    @Test
    fun `showSpotDetail sets overlay state`() {
        viewModel.showSpotDetail("spot-123")
        assertEquals(AppOverlay.SpotDetail("spot-123"), viewModel.overlayState.value)
    }

    @Test
    fun `showSpotLoading sets overlay state`() {
        viewModel.showSpotLoading("spot-456")
        assertEquals(AppOverlay.SpotLoading("spot-456"), viewModel.overlayState.value)
    }

    @Test
    fun `showSpotUnavailable sets overlay state`() {
        viewModel.showSpotUnavailable("spot-789")
        assertEquals(AppOverlay.SpotUnavailable("spot-789"), viewModel.overlayState.value)
    }

    @Test
    fun `showProSuccess sets overlay state`() {
        viewModel.showProSuccess()
        assertEquals(AppOverlay.ProSuccess, viewModel.overlayState.value)
    }

    @Test
    fun `showPaywall sets overlay state with entry point`() {
        viewModel.showPaywall("bookmark_cap")
        assertEquals(AppOverlay.Paywall("bookmark_cap"), viewModel.overlayState.value)
    }

    @Test
    fun `dismissOverlay clears overlay state`() {
        viewModel.showPaywall("profile_menu")
        viewModel.dismissOverlay()
        assertEquals(AppOverlay.None, viewModel.overlayState.value)
    }

    @Test
    fun `replaceOverlay updates current overlay`() {
        viewModel.showSpotLoading("abc")
        viewModel.replaceOverlay(AppOverlay.SpotDetail("abc"))
        assertEquals(AppOverlay.SpotDetail("abc"), viewModel.overlayState.value)
    }

    @Test
    fun `showOverlay accepts any overlay type`() {
        viewModel.showOverlay(AppOverlay.ProSuccess)
        assertTrue(viewModel.overlayState.value is AppOverlay.ProSuccess)
    }
}
