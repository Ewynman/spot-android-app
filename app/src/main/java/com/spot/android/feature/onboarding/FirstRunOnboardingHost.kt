package com.spot.android.feature.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spot.android.core.design.component.PermissionType
import com.spot.android.feature.permissions.PermissionsViewModel
import com.spot.android.navigation.SpotTab

/**
 * Host that starts the first-run coach when entering the main shell and
 * routes permission effects through [PermissionsViewModel].
 */
@Composable
fun FirstRunOnboardingHost(
    selectedTab: SpotTab,
    permissionsViewModel: PermissionsViewModel,
    modifier: Modifier = Modifier,
    viewModel: FirstRunOnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.startIfNeeded()
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab == SpotTab.Map) {
            viewModel.onMapTabSelected()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                FirstRunEffect.RequestLocationPermission ->
                    permissionsViewModel.requestPermission(PermissionType.LOCATION)
                FirstRunEffect.RequestNotificationPermission ->
                    permissionsViewModel.requestPermission(PermissionType.NOTIFICATIONS)
            }
        }
    }

    SpotFirstRunOnboardingOverlay(
        state = state,
        onPrimary = viewModel::onPrimary,
        onBack = viewModel::back,
        onSkip = viewModel::skip,
        modifier = modifier,
    )
}
