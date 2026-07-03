package com.spot.android.core.design.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.spot.android.core.design.Dimensions
import com.spot.android.core.design.theme.SpotColors
import com.spot.android.core.design.theme.SpotTheme

/**
 * Empty state view for feed and list screens.
 * 
 * Per PRD/06:
 * - Shows status-specific title and subtitle
 * - Used in home feed, profile grids, bookmarks, etc.
 * - Centered layout with large text
 * 
 * @param title Primary empty state message
 * @param subtitle Secondary message or call-to-action
 * @param modifier Optional modifier for custom styling
 */
@Composable
fun EmptyFeedView(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Dimensions.Padding.horizontal)
            .testTag("emptyFeedView"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = SpotColors.Primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("emptyFeedView.title")
        )
        
        Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))
        
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = SpotColors.Primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("emptyFeedView.subtitle")
        )
    }
}

/**
 * Empty state variants for common scenarios.
 */

@Composable
fun EmptyFeedCaughtUp(modifier: Modifier = Modifier) {
    EmptyFeedView(
        title = "You're all caught up",
        subtitle = "Pull to refresh or follow more people",
        modifier = modifier
    )
}

@Composable
fun EmptyFeedNoEligibleSpots(modifier: Modifier = Modifier) {
    EmptyFeedView(
        title = "Nothing to show yet",
        subtitle = "Follow people, unblock, or change filters",
        modifier = modifier
    )
}

@Composable
fun EmptyFeedNoSpotsGlobal(modifier: Modifier = Modifier) {
    EmptyFeedView(
        title = "No Spots Yet",
        subtitle = "Be the first to post",
        modifier = modifier
    )
}

@Composable
fun EmptyProfileSpots(modifier: Modifier = Modifier) {
    EmptyFeedView(
        title = "No spots yet",
        subtitle = "Share your first spot",
        modifier = modifier
    )
}

@Composable
fun EmptyBookmarks(modifier: Modifier = Modifier) {
    EmptyFeedView(
        title = "No bookmarks",
        subtitle = "Bookmark spots to see them here",
        modifier = modifier
    )
}

@Composable
fun EmptyLikes(modifier: Modifier = Modifier) {
    EmptyFeedView(
        title = "No likes yet",
        subtitle = "Like spots to see them here",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun EmptyFeedViewPreview() {
    SpotTheme {
        EmptyFeedCaughtUp()
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyFeedNoEligibleSpotsPreview() {
    SpotTheme {
        EmptyFeedNoEligibleSpots()
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyFeedNoSpotsGlobalPreview() {
    SpotTheme {
        EmptyFeedNoSpotsGlobal()
    }
}
