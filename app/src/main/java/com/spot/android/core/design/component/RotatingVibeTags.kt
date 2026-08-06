package com.spot.android.core.design.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.spot.android.core.design.Dimensions
import com.spot.android.core.design.theme.SpotColors
import com.spot.android.core.design.theme.SpotTheme
import kotlinx.coroutines.delay

/**
 * Single-row vibe display: one pill at a time with optional fade rotation.
 * Ported from iOS RotatingVibeTags.
 */
@Composable
fun RotatingVibeTags(
    labels: List<String>,
    onTap: (() -> Unit)? = null,
    showPlusCount: Boolean = true,
    intervalMs: Long = 2200L,
    modifier: Modifier = Modifier,
) {
    if (labels.isEmpty()) return

    val shouldRotate = labels.size > 1
    var index by remember(labels) { mutableIntStateOf(0) }

    LaunchedEffect(labels, shouldRotate) {
        if (!shouldRotate) return@LaunchedEffect
        while (true) {
            delay(intervalMs)
            index = (index + 1) % labels.size
        }
    }

    val current = labels.getOrElse(index) { labels.first() }
    val a11y = "Vibes: ${labels.joinToString(", ")}"

    Row(
        modifier = modifier
            .semantics { contentDescription = a11y }
            .then(
                if (onTap != null) {
                    Modifier.clickable(onClick = onTap)
                } else {
                    Modifier
                },
            )
            .testTag("spotCard.vibeChip"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        if (shouldRotate) {
            AnimatedContent(
                targetState = current,
                transitionSpec = {
                    fadeIn(tween(250)) togetherWith fadeOut(tween(250))
                },
                label = "vibeRotate",
            ) { label ->
                VibePill(text = label)
            }
        } else {
            VibePill(text = labels.first())
        }

        if (showPlusCount && labels.size > 1) {
            Text(
                text = "+${labels.size - 1}",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = SpotColors.Primary,
            )
        }
    }
}

@Composable
private fun VibePill(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = SpotColors.Primary,
        modifier = Modifier
            .clip(RoundedCornerShape(Dimensions.Radius.medium))
            .background(SpotColors.Accent)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

@Preview(showBackground = true)
@Composable
private fun RotatingVibeTagsPreview() {
    SpotTheme {
        RotatingVibeTags(labels = listOf("Scenic View", "Hidden Gem", "Quiet Moment"))
    }
}
