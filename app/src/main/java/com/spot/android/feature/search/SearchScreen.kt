package com.spot.android.feature.search

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
 * Search tab placeholder.
 *
 * Full search implementation lands in Phase 3.4 (PRD/09).
 */
@Composable
fun SearchScreen(
    tabReselectBus: TabReselectBus,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(tabReselectBus) {
        tabReselectBus.reselectEvents.collect { tab ->
            if (tab == SpotTab.Search) {
                // Phase 3.4: reset search query / scroll to top
            }
        }
    }

    Scaffold(
        modifier = modifier.testTag("search.searchRoot"),
        topBar = { TopNavigationView() },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Search",
                color = SpotColors.Primary,
            )
        }
    }
}
