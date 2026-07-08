package com.spot.android.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spot.android.BuildConfig
import com.spot.android.core.design.Dimensions

/**
 * Main settings screen showing sections: Account, Security, Subscription,
 * Permissions, Support, Legal, and Debug (debug builds only).
 *
 * Reference: PRD/11-settings.md
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToAccount: () -> Unit,
    onNavigateToSecurity: () -> Unit,
    onNavigateToSubscription: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToLegal: () -> Unit,
    onNavigateToDebugLogging: () -> Unit,
    onNavigateToDebugAlgorithm: () -> Unit,
    onNavigateBack: () -> Unit,
    hasPermissionWarning: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings.root"),
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
            contentPadding = PaddingValues(vertical = Dimensions.paddingMedium),
        ) {
            item {
                SettingsSectionHeader(title = "Account")
            }
            item {
                SettingsRow(
                    icon = Icons.Default.Person,
                    title = "Account settings",
                    onClick = onNavigateToAccount,
                )
            }

            item {
                SettingsSectionHeader(title = "Security")
            }
            item {
                SettingsRow(
                    icon = Icons.Default.Security,
                    title = "Security options",
                    onClick = onNavigateToSecurity,
                )
            }

            item {
                SettingsSectionHeader(title = "Subscription")
            }
            item {
                SettingsRow(
                    icon = Icons.Default.Star,
                    title = "Subscription & Pro",
                    onClick = onNavigateToSubscription,
                )
            }

            item {
                SettingsSectionHeader(title = "Permissions")
            }
            item {
                SettingsRow(
                    icon = Icons.Default.Settings,
                    title = "Permissions",
                    onClick = onNavigateToPermissions,
                    showWarningBadge = hasPermissionWarning,
                )
            }

            item {
                SettingsSectionHeader(title = "Support")
            }
            item {
                SettingsRow(
                    icon = Icons.Default.Email,
                    title = "Contact Support",
                    subtitle = "support@spotapp.online",
                    onClick = { /* mailto handled externally */ },
                )
            }

            item {
                SettingsSectionHeader(title = "Legal")
            }
            item {
                SettingsRow(
                    icon = Icons.Default.Description,
                    title = "Legal documents",
                    onClick = onNavigateToLegal,
                )
            }

            if (BuildConfig.DEBUG) {
                item {
                    SettingsSectionHeader(title = "Debug")
                }
                item {
                    SettingsRow(
                        icon = Icons.Default.BugReport,
                        title = "Console logging",
                        onClick = onNavigateToDebugLogging,
                    )
                }
                item {
                    SettingsRow(
                        icon = Icons.Default.Analytics,
                        title = "Algorithm snapshot",
                        onClick = onNavigateToDebugAlgorithm,
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = Dimensions.paddingMedium,
                vertical = Dimensions.paddingSmall,
            ),
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    showWarningBadge: Boolean = false,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = Dimensions.paddingMedium,
                    vertical = Dimensions.paddingMedium,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.width(Dimensions.paddingMedium))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    if (showWarningBadge) {
                        Spacer(modifier = Modifier.width(Dimensions.paddingSmall))
                        Badge(
                            containerColor = MaterialTheme.colorScheme.error,
                        ) {
                            Text("!")
                        }
                    }
                }
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    HorizontalDivider()
}
