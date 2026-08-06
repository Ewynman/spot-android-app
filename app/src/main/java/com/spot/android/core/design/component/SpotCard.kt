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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.spot.android.core.design.Dimensions
import com.spot.android.core.design.theme.SpotColors
import com.spot.android.core.design.theme.SpotTheme
import com.spot.android.core.media.SpotImageRequest
import com.spot.android.core.util.cityStateFromLocation
import com.spot.android.data.model.Spot

private val LikedRed = Color(0xFFE53935)
private val IdleGray = Color(0xFF9E9E9E)

/**
 * Core content card matching iOS SpotCard anatomy:
 * header (avatar/username | location) → rounded media → ♡/bookmark/⋮ | vibe pill.
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
    modifier: Modifier = Modifier,
) {
    val vibeLabels = remember(spot.vibeTags, spot.vibeTag) {
        val fromTags = spot.vibeTags?.map { it.name }?.filter { it.isNotBlank() }.orEmpty()
        when {
            fromTags.isNotEmpty() -> fromTags
            !spot.vibeTag.isNullOrBlank() -> listOf(spot.vibeTag)
            else -> emptyList()
        }
    }
    var showVibeSheet by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("spotCard.${spot.id}"),
    ) {
        SpotCardHeader(
            username = spot.username,
            userProfileImageURL = spot.userProfileImageURL,
            authorIsPro = spot.authorIsPro,
            locationName = spot.locationName,
            onUserClick = { onUserClick?.invoke(spot.userId) },
        )

        Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))

        val imageModels = spotImageModels(spot)
        if (imageModels.isNotEmpty()) {
            SpotCardMediaGallery(
                imageModels = imageModels,
                mediaDisplayAspectRatio = spot.mediaDisplayAspectRatio,
                showPagerIndicator = imageModels.size > 1,
            )
        }

        Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))

        SpotCardInteractionBar(
            isLiked = spot.isLiked,
            isSaved = spot.isSaved,
            vibeLabels = vibeLabels,
            onLikeClick = onLikeClick,
            onBookmarkClick = onBookmarkClick,
            onMoreClick = onMoreClick,
            onVibeClick = {
                if (vibeLabels.size > 1) {
                    showVibeSheet = true
                } else if (vibeLabels.isNotEmpty()) {
                    onVibeClick?.invoke(vibeLabels.first())
                }
            },
        )

        Spacer(modifier = Modifier.height(Dimensions.Spacing.xl))
    }

    if (showVibeSheet && vibeLabels.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showVibeSheet = false },
            title = { Text("Vibes") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)) {
                    vibeLabels.forEach { label ->
                        VibeChip(
                            text = label,
                            onClick = {
                                showVibeSheet = false
                                onVibeClick?.invoke(label)
                            },
                            testTag = "spotCard.vibeSheet.$label",
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showVibeSheet = false }) {
                    Text("Done")
                }
            },
            containerColor = SpotColors.Background,
        )
    }
}

@Composable
private fun SpotCardHeader(
    username: String,
    userProfileImageURL: String?,
    authorIsPro: Boolean,
    locationName: String?,
    onUserClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .testTag("spotCard.header"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f, fill = false)
                .clickable(onClick = onUserClick),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Avatar(
                imageUrl = userProfileImageURL,
                isPro = authorIsPro,
                size = 32.dp,
                contentDescription = "$username avatar",
                modifier = Modifier.testTag("spotCard.avatar"),
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = username,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = SpotColors.Primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("spotCard.username"),
            )

            if (authorIsPro) {
                Spacer(modifier = Modifier.width(4.dp))
                ProBadge()
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (!locationName.isNullOrBlank()) {
            Text(
                text = cityStateFromLocation(locationName),
                style = MaterialTheme.typography.bodySmall,
                color = SpotColors.Primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("spotCard.location"),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SpotCardMediaGallery(
    imageModels: List<Any>,
    mediaDisplayAspectRatio: Double,
    showPagerIndicator: Boolean,
) {
    val aspect = mediaDisplayAspectRatio.toFloat().takeIf { it > 0f } ?: 1f
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(Dimensions.Radius.medium))
            .aspectRatio(aspect)
            .testTag("spotCard.mediaGallery"),
    ) {
        if (imageModels.size == 1) {
            SpotAsyncImage(
                model = imageModels[0],
                contentDescription = "Spot image",
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            val pagerState = rememberPagerState(pageCount = { imageModels.size })
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                SpotAsyncImage(
                    model = imageModels[page],
                    contentDescription = "Spot image ${page + 1} of ${imageModels.size}",
                    modifier = Modifier.fillMaxSize(),
                )
            }
            if (showPagerIndicator) {
                PageIndicator(
                    pageCount = imageModels.size,
                    currentPage = pagerState.currentPage,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = Dimensions.Spacing.medium),
                )
            }
        }
    }
}

@Composable
private fun SpotAsyncImage(
    model: Any,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(model)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier,
    )
}

@Composable
private fun SpotCardInteractionBar(
    isLiked: Boolean,
    isSaved: Boolean,
    vibeLabels: List<String>,
    onLikeClick: (() -> Unit)?,
    onBookmarkClick: (() -> Unit)?,
    onMoreClick: (() -> Unit)?,
    onVibeClick: (() -> Unit)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 12.dp)
            .testTag("spotCard.interactionBar"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = { onLikeClick?.invoke() },
            modifier = Modifier
                .size(40.dp)
                .testTag("spotCard.likeButton"),
        ) {
            Icon(
                imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = if (isLiked) "Unlike" else "Like",
                tint = if (isLiked) LikedRed else IdleGray,
            )
        }

        IconButton(
            onClick = { onBookmarkClick?.invoke() },
            modifier = Modifier
                .size(40.dp)
                .testTag("spotCard.bookmarkButton"),
        ) {
            Icon(
                imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                contentDescription = if (isSaved) "Remove bookmark" else "Bookmark",
                tint = if (isSaved) SpotColors.Primary else IdleGray,
            )
        }

        IconButton(
            onClick = { onMoreClick?.invoke() },
            modifier = Modifier
                .size(40.dp)
                .testTag("spotCard.moreButton"),
        ) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "More options",
                tint = SpotColors.Primary,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        if (vibeLabels.isNotEmpty()) {
            RotatingVibeTags(
                labels = vibeLabels,
                onTap = onVibeClick,
            )
        }
    }
}

@Composable
private fun ProBadge() {
    Text(
        text = "PRO",
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = SpotColors.ProGold,
        modifier = Modifier.testTag("spotCard.proBadge"),
    )
}

@Composable
private fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(pageCount) { index ->
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(
                        color = if (index == currentPage) Color.White else Color.White.copy(alpha = 0.45f),
                        shape = CircleShape,
                    ),
            )
        }
    }
}

/**
 * Prefer SpotImageRequest (storage path) when available; otherwise use signed/public URL strings.
 */
private fun spotImageModels(spot: Spot): List<Any> {
    val fromImages = spot.images
        ?.mapNotNull { image ->
            image.storagePath.takeIf { it.isNotBlank() }?.let { path ->
                SpotImageRequest(storagePath = path, bucket = "spots")
            }
        }
        .orEmpty()
    if (fromImages.isNotEmpty()) return fromImages

    val urls = spot.imageURLs ?: listOfNotNull(spot.imageURL)
    return urls.filter { it.isNotBlank() }
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
                locationName = "San Francisco, CA, United States",
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
                isSaved = false,
            ),
        )
    }
}
