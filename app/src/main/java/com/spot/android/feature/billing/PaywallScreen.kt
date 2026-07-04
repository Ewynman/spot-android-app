package com.spot.android.feature.billing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spot.android.core.design.Dimensions
import com.spot.android.data.billing.BillingState

/**
 * Paywall screen for Pro subscription upsell.
 *
 * Reference: PRD/12-pro-subscription.md
 */
@Composable
fun PaywallScreen(
    entryPoint: String?,
    onDismiss: () -> Unit,
    onNavigateToTerms: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onShowProSuccess: () -> Unit = {},
    onShowProOnboarding: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: BillingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is BillingEffect.ShowProSuccess -> {
                    onDismiss()
                    onShowProSuccess()
                }
                is BillingEffect.ShowProOnboarding -> {
                    onDismiss()
                    onShowProOnboarding()
                }
                is BillingEffect.ShowRestoreSuccess -> {
                    onDismiss()
                    onShowProSuccess()
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .testTag("paywall.root")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.End)
                    .testTag("paywall.closeButton"),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SPOT",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.dp.value.toInt().sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("paywall.title"),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Pro",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("paywall.subtitle"),
            )

            Spacer(modifier = Modifier.height(32.dp))

            ProFeaturesList()

            Spacer(modifier = Modifier.height(32.dp))

            when (uiState.billingState) {
                BillingState.LOADING_PRODUCTS -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("paywall.loading"),
                    )
                }
                BillingState.ERROR -> {
                    Text(
                        text = uiState.billingErrorMessage ?: "Couldn't load subscription",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("paywall.error"),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = viewModel::retryLoadProducts,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("paywall.retryButton"),
                    ) {
                        Text("Try Again")
                    }
                }
                BillingState.READY, BillingState.IDLE -> {
                    uiState.productDetails?.let { product ->
                        Text(
                            text = "${product.formattedPrice} / year",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("paywall.price"),
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Button(
                        onClick = {
                            val activity = context.findActivity()
                            if (activity != null) {
                                viewModel.purchasePro(activity, entryPoint)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("paywall.subscribeButton"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                        shape = RoundedCornerShape(Dimensions.cornerRadiusMedium),
                    ) {
                        Text(
                            text = "Subscribe",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = viewModel::restorePurchases,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("paywall.restoreButton"),
                        shape = RoundedCornerShape(Dimensions.cornerRadiusMedium),
                    ) {
                        Text("Restore Purchase")
                    }

                    uiState.userErrorMessage?.let { error ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("paywall.userError"),
                        )
                    }
                }
                BillingState.PURCHASING, BillingState.RESTORING -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("paywall.processing"),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth(),
            ) {
                TextButton(
                    onClick = onNavigateToTerms,
                    modifier = Modifier.testTag("paywall.termsButton"),
                ) {
                    Text(
                        text = "Terms",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Text(
                    text = " • ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                TextButton(
                    onClick = onNavigateToPrivacy,
                    modifier = Modifier.testTag("paywall.privacyButton"),
                ) {
                    Text(
                        text = "Privacy",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProFeaturesList() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ProFeatureItem(
            icon = Icons.Outlined.Style,
            title = "Custom vibe tags",
            subtitle = "Create your own vibes",
        )
        ProFeatureItem(
            icon = Icons.Outlined.Image,
            title = "Up to 5 images per spot",
            subtitle = "Show more of each place",
        )
        ProFeatureItem(
            icon = Icons.Outlined.Edit,
            title = "Edit spots after posting",
            subtitle = "Update your recommendations",
        )
        ProFeatureItem(
            icon = Icons.Outlined.Bookmark,
            title = "Unlimited bookmarks",
            subtitle = "Save as many spots as you want",
        )
        ProFeatureItem(
            icon = Icons.Outlined.Collections,
            title = "Collections for bookmarks",
            subtitle = "Organize your saved spots",
        )
        ProFeatureItem(
            icon = Icons.Outlined.Search,
            title = "Advanced search filters",
            subtitle = "Find exactly what you're looking for",
        )
        ProFeatureItem(
            icon = Icons.Outlined.Star,
            title = "Supporter badge",
            subtitle = "Show your Pro status",
        )
    }
}

@Composable
private fun ProFeatureItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.Top),
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun android.content.Context.findActivity(): android.app.Activity? {
    var context = this
    while (context is android.content.ContextWrapper) {
        if (context is android.app.Activity) return context
        context = context.baseContext
    }
    return null
}

private val Int.sp: androidx.compose.ui.unit.TextUnit
    get() = androidx.compose.ui.unit.TextUnit(
        this.toFloat(),
        androidx.compose.ui.unit.TextUnitType.Sp
    )
