package com.spot.android.core.design.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.spot.android.core.design.Dimensions
import com.spot.android.core.design.theme.SpotColors
import com.spot.android.core.design.theme.SpotTheme

/**
 * Permission pre-prompt component for neutral permission requests.
 * 
 * Per PRD/02 and PRD/05:
 * - Neutral "Continue" screen before OS permission dialog
 * - Shows icon, title, explanation, and continue/skip buttons
 * - Never blocking - users can always skip
 * - Used for location, camera, photos, notifications
 * 
 * @param type The type of permission being requested
 * @param title The title text
 * @param message The explanation message
 * @param onContinue Callback when user taps Continue
 * @param onSkip Callback when user taps Skip
 * @param modifier Optional modifier for custom styling
 */
@Composable
fun PermissionPrePrompt(
    type: PermissionType,
    title: String,
    message: String,
    onContinue: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SpotColors.Background)
            .padding(horizontal = Dimensions.Padding.horizontal)
            .testTag("permissionPrePrompt"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon
        Icon(
            imageVector = type.icon,
            contentDescription = null,
            tint = SpotColors.Primary,
            modifier = Modifier
                .size(80.dp)
                .testTag("permissionPrePrompt.icon")
        )
        
        Spacer(modifier = Modifier.height(Dimensions.Spacing.XL))
        
        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = SpotColors.Primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("permissionPrePrompt.title")
        )
        
        Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))
        
        // Message
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = SpotColors.Primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("permissionPrePrompt.message")
        )
        
        Spacer(modifier = Modifier.height(Dimensions.Spacing.XL * 2))
        
        // Continue button
        Button(
            onClick = onContinue,
            colors = ButtonDefaults.buttonColors(
                containerColor = SpotColors.Primary,
                contentColor = SpotColors.ButtonText
            ),
            shape = RoundedCornerShape(Dimensions.Radius.medium),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("permissionPrePrompt.continueButton")
        ) {
            Text(
                text = "Continue",
                style = MaterialTheme.typography.labelLarge
            )
        }
        
        Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))
        
        // Skip button
        TextButton(
            onClick = onSkip,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("permissionPrePrompt.skipButton")
        ) {
            Text(
                text = "Skip",
                style = MaterialTheme.typography.labelLarge,
                color = SpotColors.Primary
            )
        }
    }
}

enum class PermissionType(val icon: ImageVector) {
    LOCATION(Icons.Filled.LocationOn),
    CAMERA(Icons.Filled.CameraAlt),
    PHOTOS(Icons.Filled.Image),
    NOTIFICATIONS(Icons.Filled.Notifications)
}

@Preview(showBackground = true)
@Composable
private fun PermissionPrePromptLocationPreview() {
    SpotTheme {
        PermissionPrePrompt(
            type = PermissionType.LOCATION,
            title = "Enable Location",
            message = "We need your location to show you spots nearby and help you discover new places.",
            onContinue = {},
            onSkip = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PermissionPrePromptCameraPreview() {
    SpotTheme {
        PermissionPrePrompt(
            type = PermissionType.CAMERA,
            title = "Camera Access",
            message = "Take photos to share your favorite spots with the community.",
            onContinue = {},
            onSkip = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PermissionPrePromptNotificationsPreview() {
    SpotTheme {
        PermissionPrePrompt(
            type = PermissionType.NOTIFICATIONS,
            title = "Enable Notifications",
            message = "Get notified when someone follows you or likes your spots.",
            onContinue = {},
            onSkip = {}
        )
    }
}
