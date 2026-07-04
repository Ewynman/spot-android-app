package com.spot.android.feature.permissions

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spot.android.core.design.component.PermissionPrePrompt

/**
 * Host composable that shows permission pre-prompts and launches OS permission dialogs.
 *
 * Wrap app content with this to enable contextual permission requests from any screen.
 * Denial never blocks — users can always skip.
 *
 * Reference: PRD/05-auth-onboarding.md
 */
@Composable
fun PermissionRequestHost(
    modifier: Modifier = Modifier,
    viewModel: PermissionsViewModel = hiltViewModel(),
    content: @Composable () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { results ->
        val granted = results.values.all { it }
        viewModel.onSystemPermissionResult(granted)
    }

    LaunchedEffect(uiState.pendingLaunchPermissions) {
        val permissions = uiState.pendingLaunchPermissions ?: return@LaunchedEffect
        permissionLauncher.launch(permissions)
        viewModel.clearPendingLaunch()
    }

    content()

    val activePrePrompt = uiState.activePrePrompt
    if (activePrePrompt != null) {
        PermissionPrePrompt(
            type = activePrePrompt,
            title = PermissionContent.title(activePrePrompt),
            message = PermissionContent.message(activePrePrompt),
            onContinue = viewModel::onPrePromptContinue,
            onSkip = viewModel::onPrePromptSkip,
            modifier = modifier.fillMaxSize(),
        )
    }
}

/**
 * Opens the app's system settings page for manual permission grants.
 */
fun openAppSettings(context: android.content.Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}
