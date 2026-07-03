package com.spot.android.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.spot.android.core.design.theme.SpotTheme
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented tests for the main tab shell navigation.
 *
 * Verifies tab switching and iOS-compatible test tags without requiring a live backend.
 */
class SpotShellNavigationTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun shell_displays_bottom_bar_and_home_feed() {
        composeRule.setContent {
            SpotTheme {
                SpotShell(
                    tabReselectBus = TabReselectBus(),
                    overlayViewModel = OverlayHostViewModel(),
                )
            }
        }

        composeRule.onNodeWithTag("navigation.shell").assertIsDisplayed()
        composeRule.onNodeWithTag("navigation.bottomBar").assertIsDisplayed()
        composeRule.onNodeWithTag("home.feedRoot").assertIsDisplayed()
    }

    @Test
    fun all_tab_buttons_are_displayed() {
        composeRule.setContent {
            SpotTheme {
                SpotShell(
                    tabReselectBus = TabReselectBus(),
                    overlayViewModel = OverlayHostViewModel(),
                )
            }
        }

        composeRule.onNodeWithTag("navigation.homeTab").assertIsDisplayed()
        composeRule.onNodeWithTag("navigation.mapTab").assertIsDisplayed()
        composeRule.onNodeWithTag("navigation.postTab").assertIsDisplayed()
        composeRule.onNodeWithTag("navigation.searchTab").assertIsDisplayed()
        composeRule.onNodeWithTag("navigation.profileTab").assertIsDisplayed()
    }

    @Test
    fun tapping_map_tab_shows_map_screen() {
        composeRule.setContent {
            SpotTheme {
                SpotShell(
                    tabReselectBus = TabReselectBus(),
                    overlayViewModel = OverlayHostViewModel(),
                )
            }
        }

        composeRule.onNodeWithTag("navigation.mapTab").performClick()
        composeRule.onNodeWithTag("map.mapRoot").assertIsDisplayed()
    }

    @Test
    fun tapping_search_tab_shows_search_screen() {
        composeRule.setContent {
            SpotTheme {
                SpotShell(
                    tabReselectBus = TabReselectBus(),
                    overlayViewModel = OverlayHostViewModel(),
                )
            }
        }

        composeRule.onNodeWithTag("navigation.searchTab").performClick()
        composeRule.onNodeWithTag("search.searchRoot").assertIsDisplayed()
    }

    @Test
    fun tapping_profile_tab_shows_profile_screen() {
        composeRule.setContent {
            SpotTheme {
                SpotShell(
                    tabReselectBus = TabReselectBus(),
                    overlayViewModel = OverlayHostViewModel(),
                )
            }
        }

        composeRule.onNodeWithTag("navigation.profileTab").performClick()
        composeRule.onNodeWithTag("profile.profileRoot").assertIsDisplayed()
    }

    @Test
    fun tapping_post_tab_shows_post_screen() {
        composeRule.setContent {
            SpotTheme {
                SpotShell(
                    tabReselectBus = TabReselectBus(),
                    overlayViewModel = OverlayHostViewModel(),
                )
            }
        }

        composeRule.onNodeWithTag("navigation.postTab").performClick()
        composeRule.onNodeWithTag("post.postRoot").assertIsDisplayed()
    }

    @Test
    fun tapping_home_tab_from_another_tab_returns_to_feed() {
        composeRule.setContent {
            SpotTheme {
                SpotShell(
                    tabReselectBus = TabReselectBus(),
                    overlayViewModel = OverlayHostViewModel(),
                )
            }
        }

        composeRule.onNodeWithTag("navigation.mapTab").performClick()
        composeRule.onNodeWithTag("navigation.homeTab").performClick()
        composeRule.onNodeWithTag("home.feedRoot").assertIsDisplayed()
    }

    @Test
    fun overlay_host_shows_when_overlay_is_active() {
        val overlayViewModel = OverlayHostViewModel()
        overlayViewModel.showPaywall("test")

        composeRule.setContent {
            SpotTheme {
                SpotShell(
                    tabReselectBus = TabReselectBus(),
                    overlayViewModel = overlayViewModel,
                )
            }
        }

        composeRule.onNodeWithTag("navigation.overlayHost").assertIsDisplayed()
        composeRule.onNodeWithTag("overlay.paywall").assertIsDisplayed()
    }
}
