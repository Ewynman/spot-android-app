package com.spot.android.feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.spot.android.core.design.component.EmptyFeedView
import com.spot.android.core.design.component.SpotCard
import com.spot.android.core.design.component.TopNavigationView
import com.spot.android.data.model.Spot
import com.spot.android.feature.safety.LocalSafetyActions
import com.spot.android.feature.safety.SafetyPreviewData
import com.spot.android.navigation.SpotTab
import com.spot.android.navigation.TabReselectBus

/**
 * Home feed tab placeholder with a demo spot card wired to safety overflow actions.
 *
 * Full feed implementation lands in Phase 3.1 (PRD/06).
 */
@Composable
fun HomeScreen(
    tabReselectBus: TabReselectBus,
    modifier: Modifier = Modifier,
) {
    val safetyActions = LocalSafetyActions.current
    val demoSpot = SafetyPreviewData.demoSpot

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
            if (safetyActions == null) {
                EmptyFeedView(
                    title = "No spots yet",
                    subtitle = "Follow people or explore the map to discover places.",
                )
            } else {
                SpotCard(
                    spot = demoSpot,
                    onMoreClick = { safetyActions.openSpotOverflowMenu(demoSpot) },
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .testTag("home.demoSpotCard"),
                )
            }
        }
    }
}
