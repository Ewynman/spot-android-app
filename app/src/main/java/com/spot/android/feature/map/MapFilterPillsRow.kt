package com.spot.android.feature.map

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.spot.android.core.design.theme.SpotColors
import com.spot.android.core.util.Constants
import com.spot.android.data.map.SpotMapFilter

/**
 * Pro-only map filter pill row.
 *
 * Reference: PRD/07-map.md
 */
@Composable
fun MapFilterPillsRow(
    activeFilters: Set<SpotMapFilter>,
    onFilterToggle: (SpotMapFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(
                horizontal = 16.dp,
                vertical = Constants.MapDesign.MAP_DRAWER_GAP_BELOW_FILTER_PILLS_DP.dp,
            )
            .testTag("map.filterPills"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SpotMapFilter.entries.forEach { filter ->
            val selected = activeFilters.contains(filter)
            FilterChip(
                selected = selected,
                onClick = { onFilterToggle(filter) },
                label = {
                    Text(
                        text = when (filter) {
                            SpotMapFilter.VIBE -> "Vibe"
                            SpotMapFilter.SAVED -> "Saved"
                            SpotMapFilter.LIKED -> "Liked"
                            SpotMapFilter.FOLLOWING -> "Following"
                        },
                    )
                },
                modifier = Modifier.testTag("map.filter.${filter.name.lowercase()}"),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SpotColors.Accent,
                    selectedLabelColor = SpotColors.Primary,
                    containerColor = SpotColors.Background,
                    labelColor = SpotColors.Primary,
                ),
            )
        }
    }
}
