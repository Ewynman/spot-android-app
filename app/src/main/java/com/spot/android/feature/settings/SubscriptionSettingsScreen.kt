package com.spot.android.feature.settings

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spot.android.core.design.Dimensions
import java.text.SimpleDateFormat
import java.util.*

/**
 * Subscription settings screen: Pro status, manage subscription, restore, collections.
 *
 * Reference: PRD/11-settings.md, PRD/12-pro-subscription.md
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionSettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCollections: () -> Unit,
    onShowPaywall: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SubscriptionSettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalContext.current as? Activity

    LaunchedEffect(Unit) {
        viewModel.onFirstAppear()
    }

    LaunchedEffect(viewModel.effects) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is SubscriptionSettingsEffect.ShowError -> {
                    // Error feedback via SnackbarHost or similar in production
                }
                is SubscriptionSettingsEffect.ShowSuccess -> {
                    // Success feedback via SnackbarHost or similar in production
                }
                SubscriptionSettingsEffect.ShowPaywall -> {
                    onShowPaywall()
                }
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings.subscription"),
        topBar = {
            TopAppBar(
                title = { Text("Subscription & Pro") },
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
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimensions.Spacing.medium),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    ProStatusCard(
                        isPro = uiState.isPro,
                        proUntil = uiState.proUntil,
                        modifier = Modifier.padding(horizontal = Dimensions.Spacing.medium),
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(Dimensions.Spacing.large))
                HorizontalDivider()
            }

            if (!uiState.isPro) {
                item {
                    SubscriptionActionRow(
                        icon = Icons.Default.Star,
                        title = "Go Pro",
                        subtitle = "Unlock premium features",
                        onClick = { viewModel.onGoProClicked() },
                    )
                }
            } else {
                item {
                    SubscriptionActionRow(
                        icon = Icons.Default.Settings,
                        title = "Manage subscription",
                        subtitle = "Google Play subscriptions",
                        onClick = { viewModel.onManageSubscriptionClicked() },
                    )
                }
            }

            item {
                SubscriptionActionRow(
                    icon = Icons.Default.Refresh,
                    title = "Restore purchases",
                    subtitle = "Sync your Pro status",
                    onClick = { viewModel.onRestorePurchasesClicked() },
                )
            }

            if (uiState.isPro) {
                item {
                    SubscriptionActionRow(
                        icon = Icons.Default.Collections,
                        title = "Collections",
                        subtitle = "Organize your bookmarks",
                        onClick = onNavigateToCollections,
                    )
                }
            }
        }
    }

    if (uiState.isRestoringPurchases) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Restoring purchases") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))
                    Text("Please wait...")
                }
            },
            confirmButton = { },
        )
    }
}

@Composable
private fun ProStatusCard(
    isPro: Boolean,
    proUntil: Long?,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isPro) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.Spacing.medium),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = if (isPro) Icons.Default.CheckCircle else Icons.Default.Info,
                    contentDescription = null,
                    tint = if (isPro) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
                Spacer(modifier = Modifier.width(Dimensions.Spacing.small))
                Text(
                    text = if (isPro) "Pro Active" else "Free",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isPro) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }

            if (isPro && proUntil != null) {
                Spacer(modifier = Modifier.height(Dimensions.Spacing.small))
                Text(
                    text = "Expires: ${formatProExpiryDate(proUntil)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isPro) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }

            if (!isPro) {
                Spacer(modifier = Modifier.height(Dimensions.Spacing.small))
                Text(
                    text = "Upgrade to unlock premium features",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SubscriptionActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
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
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun formatProExpiryDate(timestamp: Long): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return formatter.format(date)
}
