package com.spot.android.core.design.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.spot.android.core.design.Dimensions
import com.spot.android.core.design.theme.SpotColors
import com.spot.android.core.design.theme.SpotTheme
import kotlinx.coroutines.delay

/**
 * Toast notification component for brief status messages.
 * 
 * Per PRD/06:
 * - "Spot posted!" after successful publish (~1.5s)
 * - Refresh error toast over preserved content
 * - Brief, non-blocking feedback
 * 
 * @param message The message to display
 * @param type The type of toast (success, error, info)
 * @param visible Whether the toast is currently visible
 * @param onDismiss Callback when toast auto-dismisses
 * @param durationMs How long to show the toast (default 1500ms)
 * @param modifier Optional modifier for custom styling
 */
@Composable
fun Toast(
    message: String,
    type: ToastType = ToastType.INFO,
    visible: Boolean = true,
    onDismiss: (() -> Unit)? = null,
    durationMs: Long = 1500L,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(visible) {
        if (visible && onDismiss != null) {
            delay(durationMs)
            onDismiss()
        }
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimensions.Padding.horizontal)
                .testTag("toast")
                .semantics {
                    liveRegion = LiveRegionMode.Polite
                }
        ) {
            Row(
                modifier = Modifier
                    .background(
                        color = SpotColors.Primary,
                        shape = RoundedCornerShape(Dimensions.Radius.medium)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = type.icon,
                    contentDescription = null,
                    tint = SpotColors.ButtonText,
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(Dimensions.Spacing.small))
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SpotColors.ButtonText
                )
            }
        }
    }
}

/**
 * Banner component for persistent progress or status messages.
 * 
 * Per PRD/06 and PRD/08:
 * - Publish progress banner with timeout countdown
 * - Can show loading spinner
 * - Dismissible or persistent
 * 
 * @param message The message to display
 * @param type The type of banner (info, warning, error, loading)
 * @param visible Whether the banner is currently visible
 * @param showProgress Whether to show a loading spinner
 * @param modifier Optional modifier for custom styling
 */
@Composable
fun Banner(
    message: String,
    type: BannerType = BannerType.INFO,
    visible: Boolean = true,
    showProgress: Boolean = false,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(type.backgroundColor)
                .padding(horizontal = Dimensions.Padding.horizontal, vertical = Dimensions.Padding.Medium)
                .testTag("banner")
                .semantics {
                    liveRegion = LiveRegionMode.Polite
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium)
            ) {
                if (showProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = SpotColors.Primary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = type.icon,
                        contentDescription = null,
                        tint = SpotColors.Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SpotColors.Primary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

enum class ToastType(val icon: ImageVector) {
    SUCCESS(Icons.Filled.CheckCircle),
    ERROR(Icons.Filled.Error),
    INFO(Icons.Filled.Info)
}

enum class BannerType(
    val icon: ImageVector,
    val backgroundColor: Color
) {
    INFO(Icons.Filled.Info, SpotColors.Accent),
    WARNING(Icons.Filled.Info, SpotColors.Accent),
    ERROR(Icons.Filled.Error, SpotColors.Accent),
    LOADING(Icons.Filled.Info, SpotColors.Accent)
}

@Preview(showBackground = true)
@Composable
private fun ToastSuccessPreview() {
    SpotTheme {
        Column {
            Spacer(modifier = Modifier.height(100.dp))
            Toast(
                message = "Spot posted!",
                type = ToastType.SUCCESS
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ToastErrorPreview() {
    SpotTheme {
        Column {
            Spacer(modifier = Modifier.height(100.dp))
            Toast(
                message = "Network error",
                type = ToastType.ERROR
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BannerPreview() {
    SpotTheme {
        Banner(
            message = "Publishing your spot...",
            type = BannerType.LOADING,
            showProgress = true
        )
    }
}
