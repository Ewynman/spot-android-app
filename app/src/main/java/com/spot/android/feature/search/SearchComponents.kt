package com.spot.android.feature.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.spot.android.core.design.Dimensions
import com.spot.android.core.design.component.Avatar
import com.spot.android.core.design.component.SpotCard
import com.spot.android.core.design.component.VibeChip
import com.spot.android.core.design.theme.SpotColors
import com.spot.android.data.model.Spot
import com.spot.android.data.model.User
import com.spot.android.data.model.VibeTag
import com.spot.android.data.search.SearchHistoryItem
import com.spot.android.data.search.SearchSegment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchSegmentControl(
    selectedSegment: SearchSegment,
    onSegmentSelected: (SearchSegment) -> Unit,
    modifier: Modifier = Modifier,
) {
    SingleChoiceSegmentedButtonRow(
        modifier = modifier
            .fillMaxWidth()
            .testTag("search.segmentControl"),
    ) {
        SearchSegment.entries.forEachIndexed { index, segment ->
            SegmentedButton(
                selected = selectedSegment == segment,
                onClick = { onSegmentSelected(segment) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = SearchSegment.entries.size,
                ),
                modifier = Modifier.testTag("search.segment.${segment.rawValue}"),
            ) {
                Text(segment.title)
            }
        }
    }
}

@Composable
fun SearchQueryField(
    query: String,
    onQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = modifier
            .fillMaxWidth()
            .testTag("search.queryField"),
        placeholder = { Text("Search") },
        singleLine = true,
        shape = RoundedCornerShape(Dimensions.Radius.medium),
    )
}

@Composable
fun SearchHistoryList(
    items: List<SearchHistoryItem>,
    onItemClick: (SearchHistoryItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) return

    LazyColumn(
        modifier = modifier.testTag("search.historyList"),
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        items(items, key = { it.id }) { item ->
            Text(
                text = item.displayText,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick(item) }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("search.history.${item.type.rawValue}"),
                style = MaterialTheme.typography.bodyLarge,
                color = SpotColors.Primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun SearchUserResults(
    users: List<User>,
    onUserClick: (User) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.testTag("search.userResults"),
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        items(users, key = { it.id }) { user ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onUserClick(user) }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("search.user.${user.username}"),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Avatar(
                    imageUrl = user.profileImageURL,
                    isPro = user.isPro,
                    contentDescription = "${user.username} avatar",
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = SpotColors.Primary,
                )
            }
        }
    }
}

@Composable
fun SearchLocationResults(
    locations: List<String>,
    onLocationClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.testTag("search.locationResults"),
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        items(locations, key = { it }) { location ->
            Text(
                text = location,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLocationClick(location) }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("search.location.$location"),
                style = MaterialTheme.typography.bodyLarge,
                color = SpotColors.Primary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchVibeResults(
    vibes: List<VibeTag>,
    onVibeClick: (VibeTag) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("search.vibeResults"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        vibes.forEach { vibe ->
            VibeChip(
                text = vibe.name,
                onClick = { onVibeClick(vibe) },
                modifier = Modifier.testTag("search.vibe.${vibe.nameLower}"),
            )
        }
    }
}

@Composable
fun SearchEmptyMessage(
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("search.emptyMessage"),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = SpotColors.Primary.copy(alpha = 0.7f),
        )
    }
}

@Composable
fun SearchLoadingIndicator(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("search.loading"),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = SpotColors.Accent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchGridScaffold(
    title: String,
    showFilterButton: Boolean,
    onBack: () -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier.testTag("search.gridRoot"),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("search.gridBack"),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = SpotColors.Primary,
                    )
                }
                Text(
                    text = title,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search.gridTitle"),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = SpotColors.Primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (showFilterButton) {
                    IconButton(
                        onClick = onFilterClick,
                        modifier = Modifier.testTag("search.gridFilter"),
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = SpotColors.Primary,
                        )
                    }
                }
            }
        },
        content = content,
    )
}

@Composable
fun SearchSpotGrid(
    spots: List<Spot>,
    isLoadingMore: Boolean,
    onSpotClick: (Spot) -> Unit,
    onLoadMore: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lastVisibleIndex by remember(spots.size) {
        derivedStateOf { spots.lastIndex.coerceAtLeast(0) }
    }

    LaunchedEffect(lastVisibleIndex) {
        onLoadMore(lastVisibleIndex)
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier
            .fillMaxSize()
            .testTag("search.spotGrid"),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(spots, key = { it.id }) { spot ->
            SearchGridCover(
                spot = spot,
                onClick = { onSpotClick(spot) },
            )
        }
        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = SpotColors.Accent,
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchGridCover(
    spot: Spot,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val aspectRatio = spot.mediaDisplayAspectRatio.takeIf { it > 0.0 }?.toFloat() ?: 1f
    Box(
        modifier = modifier
            .aspectRatio(aspectRatio.coerceIn(0.75f, 1.5f))
            .clip(RoundedCornerShape(Dimensions.Radius.medium))
            .clickable(onClick = onClick)
            .testTag("search.gridSpot.${spot.id}"),
    ) {
        AsyncImage(
            model = spot.thumbnailURL ?: spot.imageURL,
            contentDescription = spot.caption,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
    }
}

@Composable
fun SearchExpandedSpotView(
    spot: Spot,
    onBack: () -> Unit,
    onLikeClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onOverflowClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("search.expandedSpot"),
    ) {
        TextButton(
            onClick = onBack,
            modifier = Modifier.testTag("search.expandedSpotBack"),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = SpotColors.Primary,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Back to grid", color = SpotColors.Primary)
        }
        SpotCard(
            spot = spot,
            onLikeClick = onLikeClick,
            onBookmarkClick = onBookmarkClick,
            onMoreClick = onOverflowClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun SearchUserProfileView(
    user: User,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .testTag("search.userProfile"),
    ) {
        TextButton(
            onClick = onBack,
            modifier = Modifier.testTag("search.userProfileBack"),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = SpotColors.Primary,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Back", color = SpotColors.Primary)
        }
        Row(
            modifier = Modifier.padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Avatar(
                imageUrl = user.profileImageURL,
                isPro = user.isPro,
                contentDescription = "${user.username} avatar",
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = user.username,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = SpotColors.Primary,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchVibeFilterSheet(
    availableVibes: List<VibeTag>,
    selectedVibeIds: Set<String>,
    onVibeToggle: (String) -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier.testTag("search.vibeFilterSheet"),
    ) {
        Text(
            text = "Filter by vibe",
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            color = SpotColors.Primary,
        )
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            availableVibes.forEach { vibe ->
                val selected = selectedVibeIds.contains(vibe.id)
                FilterChip(
                    selected = selected,
                    onClick = { onVibeToggle(vibe.id) },
                    label = { Text(vibe.name) },
                    modifier = Modifier.testTag("search.vibeFilter.${vibe.nameLower}"),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SpotColors.Accent,
                        selectedLabelColor = SpotColors.Primary,
                    ),
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.CenterEnd,
        ) {
            TextButton(
                onClick = onApply,
                modifier = Modifier.testTag("search.vibeFilterApply"),
            ) {
                Text("Apply filters", color = SpotColors.Primary)
            }
        }
    }
}
