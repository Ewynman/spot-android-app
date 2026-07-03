package com.spot.android.feature.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.spot.android.core.design.component.TopNavigationView
import com.spot.android.core.design.theme.SpotColors
import com.spot.android.navigation.SpotTab
import com.spot.android.navigation.TabReselectBus

/**
 * Map tab placeholder.
 *
 * Full map implementation lands in Phase 3.3 (PRD/07).
 */
@Composable
fun MapScreen(
    tabReselectBus: TabReselectBus,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(tabReselectBus) {
        tabReselectBus.reselectEvents.collect { tab ->
            if (tab == SpotTab.Map) {
                // Phase 3.3: dismiss spot drawer
            }
        }
    }

    Scaffold(
        modifier = modifier.testTag("map.mapRoot"),
        topBar = { TopNavigationView() },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Map",
                color = SpotColors.Primary,
            )
        }
    }
}
