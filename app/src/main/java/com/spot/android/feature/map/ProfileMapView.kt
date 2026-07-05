package com.spot.android.feature.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Profile map variant showing a single user's spots.
 *
 * Reuses [MapViewModel] with a user filter. Full profile integration lands in Phase 3.5.
 *
 * Reference: PRD/07-map.md, PRD/10-profile-social.md
 */
@Composable
fun ProfileMapView(
    userId: String,
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = hiltViewModel(),
) {
    LaunchedEffect(userId) {
        viewModel.setProfileUserFilter(userId)
        viewModel.onFirstAppear()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile.mapView"),
    ) {
        // Profile shell will embed map content directly in Phase 3.5.
        // For now this composable sets up the filtered map state for reuse.
    }
}
