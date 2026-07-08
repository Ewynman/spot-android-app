package com.spot.android.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.spot.android.core.design.Dimensions

/**
 * Permissions settings screen: location, notifications, camera, photos.
 *
 * Reference: PRD/11-settings.md
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsSettingsScreen(
    onNavigateBack: () -> Unit,
    onOpenSystemSettings: () -> Unit,
    locationPermissionStatus: PermissionStatus,
    notificationsPermissionStatus: PermissionStatus,
    cameraPermissionStatus: PermissionStatus,
    photosPermissionStatus: PermissionStatus,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings.permissions"),
        topBar = {
            TopAppBar(
                title = { Text("Permissions") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(vertical = Dimensions.Spacing.medium),
        ) {
            item {
                PermissionRow(
                    icon = Icons.Default.LocationOn,
                    title = "Location",
                    status = locationPermissionStatus,
                    onOpenSettings = onOpenSystemSettings,
                )
            }
            item {
                HorizontalDivider()
            }
            item {
                PermissionRow(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    status = notificationsPermissionStatus,
                    onOpenSettings = onOpenSystemSettings,
                )
            }
            item {
                HorizontalDivider()
            }
            item {
                PermissionRow(
                    icon = Icons.Default.CameraAlt,
                    title = "Camera",
                    status = cameraPermissionStatus,
                    onOpenSettings = onOpenSystemSettings,
                )
            }
            item {
                HorizontalDivider()
            }
            item {
                PermissionRow(
                    icon = Icons.Default.Photo,
                    title = "Photos",
                    status = photosPermissionStatus,
                    onOpenSettings = onOpenSystemSettings,
                )
            }
        }
    }
}

@Composable
private fun PermissionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    status: PermissionStatus,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = Dimensions.Spacing.medium,
                vertical = Dimensions.Spacing.medium,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
        )
            Spacer(modifier = Modifier.width(Dimensions.Spacing.medium))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = status.displayText,
                style = MaterialTheme.typography.bodySmall,
                color = when (status) {
                    PermissionStatus.GRANTED -> MaterialTheme.colorScheme.primary
                    PermissionStatus.DENIED -> MaterialTheme.colorScheme.error
                    PermissionStatus.NOT_REQUESTED -> MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
        if (status == PermissionStatus.DENIED) {
            TextButton(onClick = onOpenSettings) {
                Text("Open Settings")
            }
        }
    }
}

enum class PermissionStatus(val displayText: String) {
    GRANTED("Granted"),
    DENIED("Denied"),
    NOT_REQUESTED("Not requested"),
}
