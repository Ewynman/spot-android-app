package com.spot.android.feature.map

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.spot.android.core.design.theme.SpotColors
import com.spot.android.core.util.Constants

/**
 * Branded map pin marker.
 *
 * Reference: PRD/07-map.md
 */
@Composable
fun MapPinMarker(
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    testTag: String = "map.pin",
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) Constants.MapDesign.PIN_SELECTED_SCALE else 1f,
        label = "pinScale",
    )
    val pinSize = Constants.MapDesign.PIN_SIZE_DP.dp

    Box(
        modifier = modifier
            .scale(scale)
            .size(pinSize)
            .testTag(testTag),
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(pinSize + 10.dp)
                    .background(SpotColors.MapSelectedGlow, CircleShape),
            )
        }

        Box(
            modifier = Modifier
                .size(pinSize)
                .shadow(if (isSelected) 6.dp else 2.dp, CircleShape)
                .background(SpotColors.MapMarkerGreen, CircleShape)
                .border(1.5.dp, SpotColors.MapMarkerStroke, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(pinSize * 0.35f)
                    .background(SpotColors.MapMarkerDot, CircleShape),
            )
        }
    }
}
