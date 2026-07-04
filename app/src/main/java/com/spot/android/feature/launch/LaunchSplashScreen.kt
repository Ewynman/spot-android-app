package com.spot.android.feature.launch

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import com.spot.android.core.design.theme.SpotColors
import com.spot.android.core.util.Constants

/**
 * Branded launch splash shown while session restore runs.
 *
 * Mirrors iOS ~1.5s show + 0.5s fade timing.
 *
 * Reference: PRD/05-auth-onboarding.md
 */
@Composable
fun LaunchSplashScreen(
    visible: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = Constants.Launch.SPLASH_FADE_DURATION_MS.toInt()),
        label = "splash_fade",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(alpha)
            .background(SpotColors.Background)
            .testTag("launch.splash"),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "SPOT",
            style = MaterialTheme.typography.displayLarge,
            color = SpotColors.Primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("launch.splash.wordmark"),
        )
    }
}
