package com.spot.android.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.spot.android.core.design.Dimensions
import com.spot.android.core.design.theme.SpotColors

/**
 * First-run coach overlay. Full-screen cards for welcome/finale;
 * bottom instruction card for guided steps (spotlight simplified).
 */
@Composable
fun SpotFirstRunOnboardingOverlay(
    state: FirstRunUiState,
    onPrimary: () -> Unit,
    onBack: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!state.isPresented) return

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("onboarding.coachOverlay"),
    ) {
        if (state.currentStep.prefersFullScreenCard) {
            FullScreenCoachStep(
                step = state.currentStep,
                progress = state.progress,
                onPrimary = onPrimary,
                onSkip = onSkip,
            )
        } else {
            GuidedCoachStep(
                step = state.currentStep,
                progress = state.progress,
                canGoBack = state.currentStep.canGoBack,
                onPrimary = onPrimary,
                onBack = onBack,
                onSkip = onSkip,
            )
        }
    }
}

@Composable
private fun FullScreenCoachStep(
    step: FirstRunStep,
    progress: Float,
    onPrimary: () -> Unit,
    onSkip: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpotColors.Background)
            .padding(horizontal = Dimensions.Padding.horizontal),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(22.dp))
        ProgressBar(progress = progress)
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = if (step == FirstRunStep.WELCOME) {
                Icons.Filled.AutoAwesome
            } else {
                Icons.Filled.CheckCircle
            },
            contentDescription = null,
            tint = SpotColors.Primary,
            modifier = Modifier.height(62.dp),
        )
        Spacer(modifier = Modifier.height(22.dp))
        Text(
            text = step.title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = SpotColors.Primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("onboarding.coachTitle"),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = step.body,
            style = MaterialTheme.typography.bodyMedium,
            color = SpotColors.Primary.copy(alpha = 0.88f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .testTag("onboarding.coachBody"),
        )
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onPrimary,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("onboarding.coachPrimary"),
            colors = ButtonDefaults.buttonColors(
                containerColor = SpotColors.Primary,
                contentColor = SpotColors.ButtonText,
            ),
            shape = RoundedCornerShape(Dimensions.Radius.large),
        ) {
            Text("Start exploring")
        }
        if (step == FirstRunStep.WELCOME) {
            TextButton(
                onClick = onSkip,
                modifier = Modifier.testTag("onboarding.coachSkip"),
            ) {
                Text("Skip", color = SpotColors.Primary)
            }
        }
        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun GuidedCoachStep(
    step: FirstRunStep,
    progress: Float,
    canGoBack: Boolean,
    onPrimary: () -> Unit,
    onBack: () -> Unit,
    onSkip: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f)),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(Dimensions.Radius.large))
                .background(SpotColors.Background)
                .padding(20.dp),
        ) {
            ProgressBar(progress = progress)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = step.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = SpotColors.Primary,
                modifier = Modifier.testTag("onboarding.coachTitle"),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = step.body,
                style = MaterialTheme.typography.bodyMedium,
                color = SpotColors.Primary.copy(alpha = 0.88f),
                modifier = Modifier.testTag("onboarding.coachBody"),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (canGoBack) {
                    TextButton(onClick = onBack, modifier = Modifier.testTag("onboarding.coachBack")) {
                        Text("Back", color = SpotColors.Primary)
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
                TextButton(onClick = onSkip, modifier = Modifier.testTag("onboarding.coachSkip")) {
                    Text("Skip", color = SpotColors.WelcomeMutedText)
                }
                Button(
                    onClick = onPrimary,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SpotColors.Primary,
                        contentColor = SpotColors.ButtonText,
                    ),
                    shape = RoundedCornerShape(Dimensions.Radius.large),
                    modifier = Modifier.testTag("onboarding.coachPrimary"),
                ) {
                    Text(if (step == FirstRunStep.MAP_TAB) "Got it" else "Next")
                }
            }
        }
    }
}

@Composable
private fun ProgressBar(progress: Float) {
    LinearProgressIndicator(
        progress = { progress.coerceIn(0f, 1f) },
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .testTag("onboarding.coachProgress"),
        color = SpotColors.Primary,
        trackColor = SpotColors.Accent,
    )
}
