package com.spot.android.core.design.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.spot.android.core.design.Dimensions
import com.spot.android.core.design.theme.SpotColors
import com.spot.android.core.design.theme.SpotTheme

/**
 * Top navigation bar component with "SPOT" wordmark.
 * 
 * Per PRD/02:
 * - ALL-CAPS "SPOT" wordmark in primary color
 * - Optional back button when viewing pushed screens
 * - Used across all main screens
 * 
 * @param showBackButton Whether to show the back navigation button
 * @param onBackClick Callback when back button is clicked
 * @param modifier Optional modifier for custom styling
 */
@Composable
fun TopNavigationView(
    showBackButton: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(SpotColors.Background)
            .testTag("navigation.topBar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimensions.Padding.horizontal),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showBackButton && onBackClick != null) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .testTag("navigation.backButton")
                        .semantics { 
                            contentDescription = "Navigate back"
                        }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = SpotColors.Primary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            Text(
                text = "SPOT",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = SpotColors.Primary,
                modifier = Modifier.testTag("navigation.wordmark")
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TopNavigationViewPreview() {
    SpotTheme {
        TopNavigationView()
    }
}

@Preview(showBackground = true)
@Composable
private fun TopNavigationViewWithBackPreview() {
    SpotTheme {
        TopNavigationView(
            showBackButton = true,
            onBackClick = {}
        )
    }
}
