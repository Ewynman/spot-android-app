package com.spot.android.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.spot.android.core.design.Dimensions

/**
 * Legal screen: Terms of Use, Privacy Policy, Support email.
 *
 * Reference: PRD/11-settings.md
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalScreen(
    onNavigateBack: () -> Unit,
    onOpenTerms: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onContactSupport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings.legal"),
        topBar = {
            TopAppBar(
                title = { Text("Legal documents") },
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
                LegalRow(
                    icon = Icons.Default.Description,
                    title = "Terms of Use",
                    subtitle = "spotapp.online/terms",
                    onClick = onOpenTerms,
                )
            }
            item {
                HorizontalDivider()
            }
            item {
                LegalRow(
                    icon = Icons.Default.PrivacyTip,
                    title = "Privacy Policy",
                    subtitle = "spotapp.online/privacy",
                    onClick = onOpenPrivacy,
                )
            }
            item {
                HorizontalDivider()
            }
            item {
                LegalRow(
                    icon = Icons.Default.Email,
                    title = "Support email",
                    subtitle = "support@spotapp.online",
                    onClick = onContactSupport,
                )
            }
        }
    }
}

@Composable
private fun LegalRow(
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
