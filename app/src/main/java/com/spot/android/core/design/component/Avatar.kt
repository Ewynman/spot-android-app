package com.spot.android.core.design.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.spot.android.core.design.theme.SpotColors
import com.spot.android.core.design.theme.SpotTheme

/**
 * Avatar component for displaying user profile images with optional Pro ring.
 * 
 * Per PRD/02 and PRD/10:
 * - Circular user image
 * - Optional Pro badge/colored ring (gold: #C9A24A)
 * - Used in profile headers, spot cards, follow lists
 * 
 * @param imageUrl URL of the user's profile image
 * @param isPro Whether to show the Pro gold ring
 * @param size Diameter of the avatar (default 40dp)
 * @param contentDescription Accessibility description
 * @param modifier Optional modifier for custom styling
 * @param testTag Optional test tag for UI testing
 */
@Composable
fun Avatar(
    imageUrl: String?,
    isPro: Boolean = false,
    size: Dp = 40.dp,
    contentDescription: String = "User avatar",
    modifier: Modifier = Modifier,
    testTag: String? = null
) {
    val ringColor = if (isPro) SpotColors.ProGold else Color.Transparent
    val ringWidth = if (isPro) 2.dp else 0.dp
    
    Box(
        modifier = modifier
            .then(
                if (testTag != null) {
                    Modifier.testTag(testTag)
                } else {
                    Modifier
                }
            )
            .size(size)
            .semantics { 
                this.contentDescription = if (isPro) "$contentDescription, Pro user" else contentDescription
            }
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape)
                    .border(ringWidth, ringColor, CircleShape)
            )
        } else {
            // Placeholder for missing avatar
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape)
                    .border(ringWidth, ringColor, CircleShape)
                    .background(SpotColors.Accent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = SpotColors.Primary
                )
            }
        }
    }
}

/**
 * Small avatar variant for compact layouts (24dp).
 */
@Composable
fun SmallAvatar(
    imageUrl: String?,
    isPro: Boolean = false,
    contentDescription: String = "User avatar",
    modifier: Modifier = Modifier,
    testTag: String? = null
) {
    Avatar(
        imageUrl = imageUrl,
        isPro = isPro,
        size = 24.dp,
        contentDescription = contentDescription,
        modifier = modifier,
        testTag = testTag
    )
}

/**
 * Large avatar variant for profile headers (~100dp).
 */
@Composable
fun LargeAvatar(
    imageUrl: String?,
    isPro: Boolean = false,
    contentDescription: String = "User avatar",
    modifier: Modifier = Modifier,
    testTag: String? = null
) {
    Avatar(
        imageUrl = imageUrl,
        isPro = isPro,
        size = 100.dp,
        contentDescription = contentDescription,
        modifier = modifier,
        testTag = testTag
    )
}

@Preview(showBackground = true)
@Composable
private fun AvatarPreview() {
    SpotTheme {
        Avatar(
            imageUrl = null,
            isPro = false
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AvatarProPreview() {
    SpotTheme {
        Avatar(
            imageUrl = null,
            isPro = true
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SmallAvatarPreview() {
    SpotTheme {
        SmallAvatar(
            imageUrl = null,
            isPro = false
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LargeAvatarPreview() {
    SpotTheme {
        LargeAvatar(
            imageUrl = null,
            isPro = true
        )
    }
}
