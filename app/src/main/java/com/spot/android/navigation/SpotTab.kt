package com.spot.android.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.spot.android.R

/**
 * Bottom tab destinations for the main app shell.
 *
 * Tab order and default selection match iOS parity (Home is index 0 and default).
 * Each tab exposes a stable iOS-compatible test tag for shared UI tests.
 *
 * Reference: PRD/00-overview.md, PRD/02-design-system.md
 */
enum class SpotTab(
    val index: Int,
    val route: String,
    val testTag: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    Home(
        index = 0,
        route = SpotRoutes.HOME,
        testTag = "navigation.homeTab",
        labelRes = R.string.nav_home,
        icon = Icons.Default.Home,
    ),
    Map(
        index = 1,
        route = SpotRoutes.MAP,
        testTag = "navigation.mapTab",
        labelRes = R.string.nav_map,
        icon = Icons.Default.Map,
    ),
    Post(
        index = 2,
        route = SpotRoutes.POST,
        testTag = "navigation.postTab",
        labelRes = R.string.nav_post,
        icon = Icons.Default.Add,
    ),
    Search(
        index = 3,
        route = SpotRoutes.SEARCH,
        testTag = "navigation.searchTab",
        labelRes = R.string.nav_search,
        icon = Icons.Default.Search,
    ),
    Profile(
        index = 4,
        route = SpotRoutes.PROFILE,
        testTag = "navigation.profileTab",
        labelRes = R.string.nav_profile,
        icon = Icons.Default.Person,
    );

    companion object {
        val DEFAULT = Home

        fun fromRoute(route: String?): SpotTab? =
            entries.firstOrNull { it.route == route }
    }
}
