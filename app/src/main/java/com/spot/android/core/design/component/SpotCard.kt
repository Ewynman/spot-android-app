package com.spot.android.core.design.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.spot.android.core.design.Dimensions
import com.spot.android.core.design.theme.SpotColors
import com.spot.android.core.design.theme.SpotTheme
import com.spot.android.data.model.Spot
import com.spot.android.data.model.VibeTag

/**
 * Core content card for displaying spots.
 * 
 * Per PRD/02 and PRD/06:
 * - Header: avatar, username, Pro badge, vibe chips, location
 * - Media gallery: aspect-aware, multi-image pager
 * - Interaction bar: like, bookmark, overflow menu
 * 
 * Used in feed, map drawer, profile grids, deep-link overlay.
 * 
 * @param spot The spot data to display
 * @param onUserClick Callback when username/avatar is clicked
 * @param onVibeClick Callback when a vibe chip is clicked
 * @param onLikeClick Callback when like button is clicked
 * @param onBookmarkClick Callback when bookmark button is clicked
 * @param onMoreClick Callback when overflow menu is clicked
 * @param modifier Optional modifier for custom styling
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SpotCard(
    spot: Spot,
    onUserClick: ((String) -> Unit)? = null,
    onVibeClick: ((String) -> Unit)? = null,
    onLikeClick: (() -> Unit)? = null,
    onBookmarkClick: (() -> Unit)? = null,
    onMoreClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("spotCard.${spot.id}")
    ) {
        // Header
        SpotCardHeader(
            username = spot.username,
            userProfileImageURL = spot.userProfileImageURL,
            authorIsPro = spot.authorIsPro,
            vibeTag = spot.vibeTag,
            vibeTags = spot.vibeTags,
            locationName = spot.locationName,
            onUserClick = { onUserClick?.invoke(spot.userId) },
            onVibeClick = onVibeClick
        )
        
        Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))
        
        // Media Gallery
        val imageUrls = spot.imageURLs ?: listOfNotNull(spot.imageURL)
        if (imageUrls.isNotEmpty()) {
            SpotCardMediaGallery(
                imageUrls = imageUrls,
                mediaDisplayAspectRatio = spot.mediaDisplayAspectRatio,
                showPagerIndicator = imageUrls.size > 1
            )
        }
        
        Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))
        
        // Interaction Bar
        SpotCardInteractionBar(
            isLiked = spot.isLiked,
            isSaved = spot.isSaved,
            likesCount = spot.likes,
            onLikeClick = onLikeClick,
            onBookmarkClick = onBookmarkClick,
            onMoreClick = onMoreClick
        )
        
        Spacer(modifier = Modifier.height(Dimensions.Spacing.xl))
    }
}

@Composable
private fun SpotCardHeader(
    username: String,
    userProfileImageURL: String?,
    authorIsPro: Boolean,
    vibeTag: String?,
    vibeTags: List<VibeTag>?,
    locationName: String?,
    onUserClick: () -> Unit,
    onVibeClick: ((String) -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.Padding.horizontal)
            .testTag("spotCard.header"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Avatar(
            imageUrl = userProfileImageURL,
            isPro = authorIsPro,
            contentDescription = "$username avatar",
            modifier = Modifier
                .clickable(onClick = onUserClick)
                .testTag("spotCard.avatar")
        )
        
        Spacer(modifier = Modifier.width(Dimensions.Spacing.medium))
        
        // Username, vibes, location
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Username row
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = username,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = SpotColors.Primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .clickable(onClick = onUserClick)
                        .testTag("spotCard.username")
                )
                
                if (authorIsPro) {
                    Spacer(modifier = Modifier.width(4.dp))
                    ProBadge()
                }
            }
            
            // Vibes and location
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)
            ) {
                // Primary vibe chip
                if (vibeTag != null) {
                    VibeChipDisplay(
                        text = vibeTag,
                        testTag = "spotCard.vibeChip"
                    )
                }
                
                // Location
                if (locationName != null) {
                    Text(
                        text = locationName,
                        style = MaterialTheme.typography.bodySmall,
                        color = SpotColors.Primary.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("spotCard.location")
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SpotCardMediaGallery(
    imageUrls: List<String>,
    mediaDisplayAspectRatio: Double,
    showPagerIndicator: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(mediaDisplayAspectRatio.toFloat())
            .testTag("spotCard.mediaGallery")
    ) {
        if (imageUrls.size == 1) {
            // Single image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrls[0])
                    .crossfade(true)
                    .build(),
                contentDescription = "Spot image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Multi-image pager
            val pagerState = rememberPagerState(pageCount = { imageUrls.size })
            
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrls[page])
                        .crossfade(true)
                        .build(),
                    contentDescription = "Spot image ${page + 1} of ${imageUrls.size}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            // Page indicator
            if (showPagerIndicator) {
                PageIndicator(
                    pageCount = imageUrls.size,
                    currentPage = pagerState.currentPage,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = Dimensions.Spacing.medium)
                )
            }
        }
    }
}

@Composable
private fun SpotCardInteractionBar(
    isLiked: Boolean,
    isSaved: Boolean,
    likesCount: Long,
    onLikeClick: (() -> Unit)?,
    onBookmarkClick: (() -> Unit)?,
    onMoreClick: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.Padding.horizontal)
            .testTag("spotCard.interactionBar"),
        horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Like button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = { onLikeClick?.invoke() },
                modifier = Modifier
                    .size(40.dp)
                    .testTag("spotCard.likeButton")
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (isLiked) "Unlike" else "Like",
                    tint = if (isLiked) SpotColors.Primary else SpotColors.Primary.copy(alpha = 0.7f)
                )
            }
            
            if (likesCount > 0) {
                Text(
                    text = likesCount.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = SpotColors.Primary,
                    modifier = Modifier.testTag("spotCard.likesCount")
                )
            }
        }
        
        // Bookmark button
        IconButton(
            onClick = { onBookmarkClick?.invoke() },
            modifier = Modifier
                .size(40.dp)
                .testTag("spotCard.bookmarkButton")
        ) {
            Icon(
                imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                contentDescription = if (isSaved) "Remove bookmark" else "Bookmark",
                tint = if (isSaved) SpotColors.Primary else SpotColors.Primary.copy(alpha = 0.7f)
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // More button
        IconButton(
            onClick = { onMoreClick?.invoke() },
            modifier = Modifier
                .size(40.dp)
                .testTag("spotCard.moreButton")
        ) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "More options",
                tint = SpotColors.Primary.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ProBadge() {
    Text(
        text = "PRO",
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold
        ),
        color = SpotColors.ProGold,
        modifier = Modifier.testTag("spotCard.proBadge")
    )
}

@Composable
private fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(pageCount) { index ->
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(
                        color = if (index == currentPage) {
                            SpotColors.Primary
                        } else {
                            SpotColors.Primary.copy(alpha = 0.3f)
                        },
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SpotCardPreview() {
    SpotTheme {
        SpotCard(
            spot = Spot(
                id = "1",
                userId = "user1",
                username = "johndoe",
                userProfileImageURL = null,
                caption = "Beautiful spot!",
                latitude = 37.7749,
                longitude = -122.4194,
                locationName = "San Francisco, CA",
                likes = 42,
                saves = 12,
                createdAt = System.currentTimeMillis(),
                updatedAt = null,
                imageURL = null,
                thumbnailURL = null,
                mediaDisplayAspectRatio = 1.0,
                mediaCount = 1,
                vibeTag = "Scenic View",
                authorIsPrivate = false,
                authorIsPro = true,
                isLiked = false,
                isSaved = false
            )
        )
    }
}
