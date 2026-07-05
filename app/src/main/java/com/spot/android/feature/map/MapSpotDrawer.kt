package com.spot.android.feature.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.spot.android.core.design.component.SpotCard
import com.spot.android.core.design.theme.SpotColors
import com.spot.android.core.util.Constants
import com.spot.android.data.model.Spot

/**
 * Bottom spot drawer with peek/expanded states.
 *
 * Reference: PRD/07-map.md
 */
@Composable
fun MapSpotDrawer(
    spot: Spot,
    drawerState: MapDrawerState,
    onClose: () -> Unit,
    onExpandToggle: () -> Unit,
    onUserClick: (String) -> Unit,
    onLikeClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val maxHeight = screenHeight * Constants.MapDesign.PANEL_MAX_SCREEN_FRACTION
    val minHeight = Constants.MapDesign.PANEL_MIN_HEIGHT_DP.dp
    val drawerHeight = when (drawerState) {
        MapDrawerState.PEEK -> minHeight
        MapDrawerState.EXPANDED -> maxHeight
        MapDrawerState.HIDDEN -> 0.dp
    }

    if (drawerState == MapDrawerState.HIDDEN) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight, max = maxHeight)
            .clip(
                RoundedCornerShape(
                    topStart = Constants.MapDesign.MAP_DRAWER_TOP_CORNER_RADIUS_DP.dp,
                    topEnd = Constants.MapDesign.MAP_DRAWER_TOP_CORNER_RADIUS_DP.dp,
                ),
            )
            .background(SpotColors.Background)
            .testTag("map.spotDrawer"),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onExpandToggle,
                modifier = Modifier.testTag("map.drawerExpandToggle"),
            ) {
                Icon(
                    imageVector = if (drawerState == MapDrawerState.EXPANDED) {
                        Icons.Default.KeyboardArrowDown
                    } else {
                        Icons.Default.KeyboardArrowUp
                    },
                    contentDescription = if (drawerState == MapDrawerState.EXPANDED) {
                        "Collapse drawer"
                    } else {
                        "Expand drawer"
                    },
                    tint = SpotColors.Primary,
                )
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier.testTag("map.drawerClose"),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close drawer",
                    tint = SpotColors.Primary,
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = drawerHeight - 48.dp),
        ) {
            SpotCard(
                spot = spot,
                onUserClick = onUserClick,
                onLikeClick = onLikeClick,
                onBookmarkClick = onBookmarkClick,
                onMoreClick = onMoreClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("map.drawerSpotCard"),
            )
        }
    }
}
