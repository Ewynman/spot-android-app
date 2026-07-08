package com.spot.android.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spot.android.core.design.Dimensions

/**
 * Account settings screen: edit profile, change password, logout, delete account.
 *
 * Reference: PRD/11-settings.md
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToWelcome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AccountSettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onFirstAppear()
    }

    LaunchedEffect(viewModel.effects) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is AccountSettingsEffect.ShowError -> {
                    // Error will be shown via SnackbarHost or similar in production
                    // For now, just log it
                }
                is AccountSettingsEffect.ShowSuccess -> {
                    // Success feedback via SnackbarHost or similar in production
                }
                AccountSettingsEffect.NavigateToWelcome -> {
                    onNavigateToWelcome()
                }
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings.account"),
        topBar = {
            TopAppBar(
                title = { Text("Account settings") },
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
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimensions.Spacing.medium),
                    ) {
                        Text(
                            text = "Username",
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Text(
                            text = uiState.username,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))
                        Text(
                            text = "Email",
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Text(
                            text = uiState.email,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(Dimensions.Spacing.large))
                HorizontalDivider()
            }

            item {
                SettingsActionRow(
                    icon = Icons.Default.Lock,
                    title = "Change password",
                    onClick = { viewModel.onChangePasswordRequested() },
                )
            }

            item {
                SettingsActionRow(
                    icon = Icons.Default.ExitToApp,
                    title = "Logout",
                    onClick = { viewModel.onLogoutRequested() },
                )
            }

            item {
                Spacer(modifier = Modifier.height(Dimensions.paddingLarge))
                HorizontalDivider()
            }

            item {
                SettingsActionRow(
                    icon = Icons.Default.Delete,
                    title = "Delete account",
                    titleColor = MaterialTheme.colorScheme.error,
                    onClick = { viewModel.onDeleteAccountRequested() },
                )
            }
        }
    }

    if (uiState.showChangePasswordDialog) {
        ChangePasswordDialog(
            currentPassword = uiState.currentPassword,
            newPassword = uiState.newPassword,
            confirmPassword = uiState.confirmPassword,
            isLoading = uiState.isChangingPassword,
            onCurrentPasswordChanged = viewModel::onCurrentPasswordChanged,
            onNewPasswordChanged = viewModel::onNewPasswordChanged,
            onConfirmPasswordChanged = viewModel::onConfirmPasswordChanged,
            onConfirm = { viewModel.onChangePasswordConfirmed() },
            onDismiss = { viewModel.onChangePasswordCancelled() },
        )
    }

    if (uiState.showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onLogoutCancelled() },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.onLogoutConfirmed() },
                    enabled = !uiState.isLoggingOut,
                ) {
                    if (uiState.isLoggingOut) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Logout")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onLogoutCancelled() }) {
                    Text("Cancel")
                }
            },
        )
    }

    if (uiState.showDeleteAccountDialog) {
        DeleteAccountDialog(
            password = uiState.deleteAccountPassword,
            confirmed = uiState.deleteAccountConfirmed,
            isLoading = uiState.isDeletingAccount,
            onPasswordChanged = viewModel::onDeleteAccountPasswordChanged,
            onConfirmationChanged = viewModel::onDeleteAccountConfirmationChanged,
            onConfirm = { viewModel.onDeleteAccountConfirmed() },
            onDismiss = { viewModel.onDeleteAccountCancelled() },
        )
    }
}

@Composable
private fun SettingsActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    titleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
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
                tint = titleColor,
            )
            Spacer(modifier = Modifier.width(Dimensions.Spacing.medium))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = titleColor,
            )
        }
    }
}

@Composable
private fun ChangePasswordDialog(
    currentPassword: String,
    newPassword: String,
    confirmPassword: String,
    isLoading: Boolean,
    onCurrentPasswordChanged: (String) -> Unit,
    onNewPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change password") },
        text = {
            Column {
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = onCurrentPasswordChanged,
                    label = { Text("Current password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(Dimensions.Spacing.small))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = onNewPasswordChanged,
                    label = { Text("New password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(Dimensions.Spacing.small))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = onConfirmPasswordChanged,
                    label = { Text("Confirm password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isLoading && 
                    currentPassword.isNotBlank() && 
                    newPassword.isNotBlank() && 
                    confirmPassword.isNotBlank(),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Change")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun DeleteAccountDialog(
    password: String,
    confirmed: Boolean,
    isLoading: Boolean,
    onPasswordChanged: (String) -> Unit,
    onConfirmationChanged: (Boolean) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete account") },
        text = {
            Column {
                Text(
                    text = "This action cannot be undone. All your spots, likes, and data will be permanently deleted.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))
                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChanged,
                    label = { Text("Password (for verification)") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onConfirmationChanged(!confirmed) },
                ) {
                    Checkbox(
                        checked = confirmed,
                        onCheckedChange = onConfirmationChanged,
                    )
                    Spacer(modifier = Modifier.width(Dimensions.Spacing.small))
                    Text(
                        text = "I understand this permanently deletes my account.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isLoading && confirmed,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Delete")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
