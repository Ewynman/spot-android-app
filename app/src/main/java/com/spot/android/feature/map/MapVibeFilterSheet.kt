package com.spot.android.feature.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.spot.android.core.design.theme.SpotColors

/**
 * Pro vibe filter sheet for map pins.
 *
 * Reference: PRD/07-map.md
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MapVibeFilterSheet(
    availableVibes: List<String>,
    selectedVibes: Set<String>,
    onVibeToggle: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier.testTag("map.vibeFilterSheet"),
    ) {
        Text(
            text = "Filter by vibe",
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            color = SpotColors.Primary,
        )

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            availableVibes.forEach { vibe ->
                val selected = selectedVibes.contains(vibe)
                FilterChip(
                    selected = selected,
                    onClick = { onVibeToggle(vibe) },
                    label = { Text(vibe) },
                    modifier = Modifier.testTag("map.vibeFilter.$vibe"),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SpotColors.Accent,
                        selectedLabelColor = SpotColors.Primary,
                    ),
                )
            }
        }
    }
}
