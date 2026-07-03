package com.spot.android.feature.post

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.spot.android.core.design.component.TopNavigationView
import com.spot.android.core.design.theme.SpotColors

/**
 * Post composer tab placeholder.
 *
 * Full 3-step composer lands in Phase 3.2 (PRD/08).
 */
@Composable
fun PostScreen(
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.testTag("post.postRoot"),
        topBar = { TopNavigationView() },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Create a Spot",
                color = SpotColors.Primary,
            )
        }
    }
}
