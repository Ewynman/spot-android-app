package com.spot.android.feature.profile

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
 * Profile tab placeholder.
 *
 * Full profile implementation lands in Phase 3.5 (PRD/10).
 */
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.testTag("profile.profileRoot"),
        topBar = { TopNavigationView() },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Profile",
                color = SpotColors.Primary,
            )
        }
    }
}
