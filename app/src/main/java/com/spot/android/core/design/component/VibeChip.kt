package com.spot.android.core.design.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.spot.android.core.design.Dimensions
import com.spot.android.core.design.theme.SpotColors
import com.spot.android.core.design.theme.SpotTheme

/**
 * Vibe chip component for displaying and selecting vibe tags.
 * 
 * Styling per PRD/02:
 * - Surface color: accent (#DEE6D8)
 * - Text: primary (#1D2C24)
 * - Rounded with large radius
 * - Compact padding
 * 
 * Used on spot cards, pickers, and filters.
 * 
 * @param text The vibe tag text to display
 * @param selected Whether the chip is currently selected (shows border)
 * @param onClick Optional click handler for selectable chips
 * @param modifier Optional modifier for custom styling
 * @param testTag Optional test tag for UI testing
 */
@Composable
fun VibeChip(
    text: String,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    testTag: String? = null
) {
    val shape = RoundedCornerShape(Dimensions.Radius.large)
    
    val chipModifier = modifier
        .then(
            if (testTag != null) {
                Modifier.testTag(testTag)
            } else {
                Modifier
            }
        )
        .clip(shape)
        .background(SpotColors.Accent)
        .then(
            if (selected) {
                Modifier.border(
                    width = 2.dp,
                    color = SpotColors.Primary,
                    shape = shape
                )
            } else {
                Modifier
            }
        )
        .then(
            if (onClick != null) {
                Modifier
                    .clickable(onClick = onClick)
                    .semantics {
                        role = Role.Button
                        contentDescription = if (selected) {
                            "$text vibe tag, selected"
                        } else {
                            "$text vibe tag, not selected"
                        }
                    }
            } else {
                Modifier.semantics {
                    contentDescription = "$text vibe tag"
                }
            }
        )
        .padding(horizontal = 12.dp, vertical = 6.dp)
    
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = SpotColors.Primary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = chipModifier
    )
}

/**
 * Display-only variant of VibeChip (non-interactive).
 * Used for showing vibes on spot cards.
 */
@Composable
fun VibeChipDisplay(
    text: String,
    modifier: Modifier = Modifier,
    testTag: String? = null
) {
    VibeChip(
        text = text,
        selected = false,
        onClick = null,
        modifier = modifier,
        testTag = testTag
    )
}

/**
 * Selectable variant of VibeChip.
 * Used in pickers and filters.
 */
@Composable
fun VibeChipSelectable(
    text: String,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null
) {
    VibeChip(
        text = text,
        selected = selected,
        onClick = { onSelectedChange(!selected) },
        modifier = modifier,
        testTag = testTag
    )
}

@Preview(showBackground = true)
@Composable
private fun VibeChipPreview() {
    SpotTheme {
        VibeChipDisplay(text = "Chill Spot")
    }
}

@Preview(showBackground = true)
@Composable
private fun VibeChipSelectedPreview() {
    SpotTheme {
        VibeChipSelectable(
            text = "Hidden Gem",
            selected = true,
            onSelectedChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun VibeChipUnselectedPreview() {
    SpotTheme {
        VibeChipSelectable(
            text = "Scenic View",
            selected = false,
            onSelectedChange = {}
        )
    }
}
