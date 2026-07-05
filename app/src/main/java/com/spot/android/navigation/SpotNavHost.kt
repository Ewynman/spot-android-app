package com.spot.android.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.spot.android.feature.home.HomeScreen
import com.spot.android.feature.map.MapScreen
import com.spot.android.feature.permissions.PermissionRequestHost
import com.spot.android.feature.post.PostScreen
import com.spot.android.feature.profile.ProfileScreen
import com.spot.android.feature.safety.SafetyFlowHost
import com.spot.android.feature.search.SearchScreen

/**
 * Main app shell: 5-tab bottom bar + NavGraph + overlay host.
 *
 * Default selected tab is Home (index 0). Reselecting the active tab fires
 * a [TabReselectBus] event for the active screen to handle.
 *
 * Reference: PRD/00-overview.md, PRD/18-build-order.md (Task 1.6)
 */
@Composable
fun SpotShell(
    tabReselectBus: TabReselectBus,
    shellNavigationBus: ShellNavigationBus,
    overlayViewModel: OverlayHostViewModel,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val selectedTab = SpotTab.fromRoute(currentRoute) ?: SpotTab.DEFAULT

    LaunchedEffect(shellNavigationBus) {
        shellNavigationBus.tabRequests.collect { tab ->
            navController.navigate(tab.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    SafetyFlowHost(
        modifier = modifier
            .fillMaxSize()
            .testTag("navigation.shell"),
    ) {
        PermissionRequestHost(
            modifier = Modifier.fillMaxSize(),
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    SpotBottomBar(
                        selectedTab = selectedTab,
                        onTabSelected = { tab ->
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onTabReselected = tabReselectBus::onTabReselected,
                    )
                },
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = SpotRoutes.HOME,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                ) {
                    composable(SpotRoutes.HOME) {
                        HomeScreen(
                            tabReselectBus = tabReselectBus,
                            overlayViewModel = overlayViewModel,
                        )
                    }
                    composable(SpotRoutes.MAP) {
                        MapScreen(
                            tabReselectBus = tabReselectBus,
                            overlayViewModel = overlayViewModel,
                        )
                    }
                    composable(SpotRoutes.POST) {
                        PostScreen()
                    }
                    composable(SpotRoutes.SEARCH) {
                        SearchScreen(tabReselectBus = tabReselectBus)
                    }
                    composable(SpotRoutes.PROFILE) {
                        ProfileScreen()
                    }
                }
            }

            OverlayHostLayer(viewModel = overlayViewModel)
        }
    }
}
