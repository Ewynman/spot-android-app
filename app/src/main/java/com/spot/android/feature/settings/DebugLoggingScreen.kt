package com.spot.android.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spot.android.core.design.Dimensions
import com.spot.android.core.logging.LogCategory

/**
 * Debug logging settings screen: toggle structured log categories.
 *
 * Reference: PRD/11-settings.md
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugLoggingScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DebugLoggingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings.debug.logging"),
        topBar = {
            TopAppBar(
                title = { Text("Console logging") },
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
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(vertical = Dimensions.Spacing.medium),
            ) {
                item {
                    LogCategoryToggle(
                        title = "Debug Logging Enabled",
                        subtitle = "Master switch for all debug logging",
                        checked = uiState.debugLoggingEnabled,
                        onCheckedChange = { viewModel.onDebugLoggingToggled(it) },
                    )
                }
                item {
                    HorizontalDivider()
                }
                item {
                    LogCategoryToggle(
                        title = "Log All Categories",
                        subtitle = "Enable all debug categories",
                        checked = uiState.logAllCategories,
                        onCheckedChange = { viewModel.onLogAllCategoriesToggled(it) },
                    )
                }
                item {
                Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))
                HorizontalDivider()
                Text(
                    text = "Individual Categories",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(Dimensions.Spacing.medium),
                    )
                }
            item {
                LogCategoryToggle(
                    title = "Spot Card",
                    checked = uiState.logSpotCard,
                    onCheckedChange = { viewModel.onCategoryToggled(LogCategory.SpotCard, it) },
                )
            }
            item {
                LogCategoryToggle(
                    title = "Privacy",
                    checked = uiState.logPrivacy,
                    onCheckedChange = { viewModel.onCategoryToggled(LogCategory.Privacy, it) },
                )
            }
            item {
                LogCategoryToggle(
                    title = "Feed Component",
                    checked = uiState.logFeedComponent,
                    onCheckedChange = { viewModel.onCategoryToggled(LogCategory.Feed, it) },
                )
            }
            item {
                LogCategoryToggle(
                    title = "Post Flow",
                    checked = uiState.logPostFlow,
                    onCheckedChange = { viewModel.onCategoryToggled(LogCategory.Post, it) },
                )
            }
            item {
                LogCategoryToggle(
                    title = "Auth",
                    checked = uiState.logAuth,
                    onCheckedChange = { viewModel.onCategoryToggled(LogCategory.Auth, it) },
                )
            }
            item {
                LogCategoryToggle(
                    title = "Network Component",
                    checked = uiState.logNetworkComponent,
                    onCheckedChange = { viewModel.onCategoryToggled(LogCategory.Network, it) },
                )
            }
            item {
                LogCategoryToggle(
                    title = "Deep Link",
                    checked = uiState.logDeepLink,
                    onCheckedChange = { viewModel.onCategoryToggled(LogCategory.DeepLink, it) },
                )
            }
            }
        }
    }
}

@Composable
private fun LogCategoryToggle(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = Dimensions.Spacing.medium,
                vertical = Dimensions.Spacing.small,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}
