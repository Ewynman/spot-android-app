package com.spot.android.feature.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.spot.android.feature.permissions.PermissionsViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Settings navigation host that manages all settings screens.
 *
 * Reference: PRD/11-settings.md
 */
@Composable
fun SettingsNavigationHost(
    onNavigateBack: () -> Unit,
    onNavigateToWelcome: () -> Unit,
    onShowPaywall: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var currentRoute by remember { mutableStateOf(SettingsRoutes.SETTINGS_ROOT) }
    val context = LocalContext.current

    when (currentRoute) {
        SettingsRoutes.SETTINGS_ROOT -> {
            val permissionsViewModel: PermissionsViewModel = hiltViewModel()
            val permissionsState by permissionsViewModel.uiState.collectAsStateWithLifecycle()

            val hasPermissionWarning = permissionsState.permissionStates.values.any { state ->
                state == com.spot.android.data.permissions.PermissionState.DENIED ||
                state == com.spot.android.data.permissions.PermissionState.PERMANENTLY_DENIED
            }

            SettingsScreen(
                onNavigateToAccount = { currentRoute = SettingsRoutes.ACCOUNT },
                onNavigateToSecurity = { currentRoute = SettingsRoutes.SECURITY },
                onNavigateToSubscription = { currentRoute = SettingsRoutes.SUBSCRIPTION },
                onNavigateToPermissions = { currentRoute = SettingsRoutes.PERMISSIONS },
                onNavigateToLegal = { currentRoute = SettingsRoutes.LEGAL },
                onNavigateToDebugLogging = { currentRoute = SettingsRoutes.DEBUG_LOGGING },
                onNavigateToDebugAlgorithm = { currentRoute = SettingsRoutes.DEBUG_ALGORITHM },
                onNavigateBack = onNavigateBack,
                hasPermissionWarning = hasPermissionWarning,
                modifier = modifier,
            )
        }

        SettingsRoutes.ACCOUNT -> AccountSettingsScreen(
            onNavigateBack = { currentRoute = SettingsRoutes.SETTINGS_ROOT },
            onNavigateToWelcome = onNavigateToWelcome,
            modifier = modifier,
        )

        SettingsRoutes.SECURITY -> SecuritySettingsScreen(
            onNavigateBack = { currentRoute = SettingsRoutes.SETTINGS_ROOT },
            modifier = modifier,
        )

        SettingsRoutes.SUBSCRIPTION -> SubscriptionSettingsScreen(
            onNavigateBack = { currentRoute = SettingsRoutes.SETTINGS_ROOT },
            onNavigateToCollections = { /* TODO: Navigate to collections */ },
            onShowPaywall = onShowPaywall,
            modifier = modifier,
        )

        SettingsRoutes.PERMISSIONS -> {
            val permissionsViewModel: PermissionsViewModel = hiltViewModel()
            val permissionsState by permissionsViewModel.uiState.collectAsStateWithLifecycle()

            PermissionsSettingsScreen(
                onNavigateBack = { currentRoute = SettingsRoutes.SETTINGS_ROOT },
                onOpenSystemSettings = { openSystemSettings(context) },
                locationPermissionStatus = mapPermissionStateToStatus(
                    permissionsState.permissionStates[com.spot.android.core.design.component.PermissionType.LOCATION]
                ),
                notificationsPermissionStatus = mapPermissionStateToStatus(
                    permissionsState.permissionStates[com.spot.android.core.design.component.PermissionType.NOTIFICATIONS]
                ),
                cameraPermissionStatus = mapPermissionStateToStatus(
                    permissionsState.permissionStates[com.spot.android.core.design.component.PermissionType.CAMERA]
                ),
                photosPermissionStatus = mapPermissionStateToStatus(
                    permissionsState.permissionStates[com.spot.android.core.design.component.PermissionType.PHOTOS]
                ),
                modifier = modifier,
            )
        }

        SettingsRoutes.LEGAL -> LegalScreen(
            onNavigateBack = { currentRoute = SettingsRoutes.SETTINGS_ROOT },
            onOpenTerms = { openUrl(context, "https://spotapp.online/terms") },
            onOpenPrivacy = { openUrl(context, "https://spotapp.online/privacy") },
            onContactSupport = { openEmail(context, "support@spotapp.online") },
            modifier = modifier,
        )

        SettingsRoutes.DEBUG_LOGGING -> DebugLoggingScreen(
            onNavigateBack = { currentRoute = SettingsRoutes.SETTINGS_ROOT },
            modifier = modifier,
        )

        SettingsRoutes.DEBUG_ALGORITHM -> AlgorithmSnapshotScreen(
            onNavigateBack = { currentRoute = SettingsRoutes.SETTINGS_ROOT },
            modifier = modifier,
        )
    }
}

private fun openSystemSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)
}

private fun openUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)
}

private fun openEmail(context: Context, email: String) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:$email")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback to chooser if no email client
        val chooserIntent = Intent.createChooser(intent, "Contact Support")
        chooserIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(chooserIntent)
    }
}

private fun mapPermissionStateToStatus(state: com.spot.android.data.permissions.PermissionState?): PermissionStatus {
    return when (state) {
        com.spot.android.data.permissions.PermissionState.AUTHORIZED,
        com.spot.android.data.permissions.PermissionState.NOT_REQUIRED -> PermissionStatus.GRANTED
        com.spot.android.data.permissions.PermissionState.DENIED,
        com.spot.android.data.permissions.PermissionState.PERMANENTLY_DENIED -> PermissionStatus.DENIED
        com.spot.android.data.permissions.PermissionState.NOT_DETERMINED,
        null -> PermissionStatus.NOT_REQUESTED
    }
}
