package com.spot.android.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.spot.android.core.design.Dimensions
import com.spot.android.core.design.component.EmptyFeedView
import com.spot.android.data.model.UserBrief

/**
 * Security settings screen: private account toggle and blocked users list.
 *
 * Reference: PRD/11-settings.md
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SecuritySettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onFirstAppear()
    }

    LaunchedEffect(viewModel.effects) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is SecuritySettingsEffect.ShowError -> {
                    // Error feedback via SnackbarHost or similar in production
                }
                is SecuritySettingsEffect.ShowSuccess -> {
                    // Success feedback via SnackbarHost or similar in production
                }
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings.security"),
        topBar = {
            TopAppBar(
                title = { Text("Security options") },
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimensions.Spacing.medium, vertical = Dimensions.Spacing.small),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Private Account",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                text = "Require approval for follow requests",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = uiState.isPrivateAccount,
                            onCheckedChange = { viewModel.onPrivateAccountToggled(it) },
                            enabled = !uiState.isUpdatingPrivacy,
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(Dimensions.Spacing.large))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(Dimensions.Spacing.small))
                Text(
                    text = "Blocked Users",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = Dimensions.Spacing.medium),
                )
                Spacer(modifier = Modifier.height(Dimensions.Spacing.small))
            }

            if (uiState.isLoadingBlockedUsers) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimensions.Spacing.medium),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (uiState.blockedUsers.isEmpty()) {
                item {
                    EmptyFeedView(
                        icon = Icons.Default.Block,
                        title = "No blocked users",
                        subtitle = "You haven't blocked anyone.",
                        modifier = Modifier.padding(Dimensions.Spacing.large),
                    )
                }
            } else {
                items(uiState.blockedUsers) { user ->
                    BlockedUserRow(
                        user = user,
                        onUnblock = { viewModel.onUnblockUser(user.userId) },
                    )
                }
            }
        }
    }
}

@Composable
private fun BlockedUserRow(
    user: UserBrief,
    onUnblock: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.Spacing.medium, vertical = Dimensions.Spacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = user.profileImageUrl,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
        )
        Spacer(modifier = Modifier.width(Dimensions.Spacing.small))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.username,
                style = MaterialTheme.typography.bodyLarge,
            )
            user.displayName?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        TextButton(onClick = onUnblock) {
            Text("Unblock")
        }
    }
}
