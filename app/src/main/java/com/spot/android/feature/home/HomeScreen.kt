package com.spot.android.feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.spot.android.core.design.component.EmptyFeedView
import com.spot.android.core.design.component.TopNavigationView
import com.spot.android.navigation.SpotTab
import com.spot.android.navigation.TabReselectBus

/**
 * Home feed tab placeholder.
 *
 * Full feed implementation lands in Phase 3.1 (PRD/06).
 */
@Composable
fun HomeScreen(
    tabReselectBus: TabReselectBus,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(tabReselectBus) {
        tabReselectBus.reselectEvents.collect { tab ->
            if (tab == SpotTab.Home) {
                // Phase 3.1: scroll to top / refresh feed
            }
        }
    }

    Scaffold(
        modifier = modifier.testTag("home.feedRoot"),
        topBar = { TopNavigationView() },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            EmptyFeedView(
                title = "No spots yet",
                subtitle = "Follow people or explore the map to discover places.",
            )
        }
    }
}
