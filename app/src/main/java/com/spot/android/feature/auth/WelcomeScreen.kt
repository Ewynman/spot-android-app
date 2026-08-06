package com.spot.android.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spot.android.core.design.Dimensions
import com.spot.android.core.design.theme.SpotColors
import com.spot.android.feature.auth.component.AuthPrimaryButton
import com.spot.android.feature.auth.component.AuthSecondaryButton
import com.spot.android.feature.auth.component.TermsAgreementCheckbox

/**
 * Welcome screen matching iOS WelcomeView: wordmark, serif hero, collage, CTAs.
 *
 * Reference: PRD/05-auth-onboarding.md
 */
@Composable
fun WelcomeScreen(
    isLoading: Boolean,
    onGetStarted: () -> Unit,
    onLogIn: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onTermsAgreed: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var termsChecked by rememberSaveable { mutableStateOf(false) }
    val actionsEnabled = termsChecked && !isLoading

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SpotColors.Background)
            .testTag("welcome.screen"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimensions.Padding.horizontal)
                .padding(top = Dimensions.Padding.verticalXL, bottom = Dimensions.Padding.verticalLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Spacer(modifier = Modifier.height(Dimensions.Spacing.xl))

                Text(
                    text = "SPOT",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 6.sp,
                    ),
                    color = SpotColors.Primary,
                    modifier = Modifier.testTag("welcome.wordmark"),
                )

                Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))

                Text(
                    text = "Places hit different when they come from your people.",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        lineHeight = 34.sp,
                    ),
                    color = SpotColors.Primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("welcome.headline"),
                )

                Spacer(modifier = Modifier.height(Dimensions.Spacing.small))

                Text(
                    text = "Save the places you love. Discover the ones your friends can’t stop talking about.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SpotColors.WelcomeMutedText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .testTag("welcome.tagline"),
                )

                Spacer(modifier = Modifier.height(Dimensions.Spacing.xl))

                WelcomeHeroCollage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .testTag("welcome.hero"),
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small),
            ) {
                TermsAgreementCheckbox(
                    checked = termsChecked,
                    onCheckedChange = { checked ->
                        termsChecked = checked
                        onTermsAgreed(checked)
                    },
                    checkboxTestTag = "welcome.termsCheckbox",
                )

                AuthPrimaryButton(
                    text = "Get started",
                    onClick = {
                        onTermsAgreed(true)
                        onGetStarted()
                    },
                    enabled = actionsEnabled,
                    testTag = "welcome.getStartedButton",
                )

                AuthSecondaryButton(
                    text = "Log in",
                    onClick = {
                        onTermsAgreed(true)
                        onLogIn()
                    },
                    enabled = actionsEnabled,
                    testTag = "welcome.logInButton",
                )

                AuthSecondaryButton(
                    text = "Continue with Google",
                    onClick = {
                        onTermsAgreed(true)
                        onGoogleSignIn()
                    },
                    enabled = actionsEnabled,
                    testTag = "welcome.googleSignInButton",
                )
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    color = SpotColors.Primary,
                    modifier = Modifier.testTag("welcome.loading"),
                )
            }
        }
    }
}

@Composable
private fun WelcomeHeroCollage(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(SpotColors.WelcomeGlow.copy(alpha = 0.22f)),
        )

        WelcomeChip(
            text = "Scenic View",
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 8.dp, y = 12.dp)
                .rotate(-8f),
        )
        WelcomeChip(
            text = "Foodie Heaven",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-4).dp, y = 28.dp)
                .rotate(6f),
        )
        WelcomeChip(
            text = "Quiet Moment",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-8).dp)
                .rotate(-3f),
        )

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SpotColors.Primary)
                .align(Alignment.Center),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(SpotColors.ButtonText),
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 20.dp, y = 36.dp),
            horizontalArrangement = Arrangement.spacedBy((-8).dp),
        ) {
            WelcomeAvatar(initials = "N", palette = 0)
            WelcomeAvatar(initials = "J", palette = 1)
        }
    }
}

@Composable
private fun WelcomeChip(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = SpotColors.Primary,
        modifier = modifier
            .clip(RoundedCornerShape(Dimensions.Radius.medium))
            .background(SpotColors.WelcomeChipFill)
            .border(1.dp, SpotColors.WelcomeLine.copy(alpha = 0.5f), RoundedCornerShape(Dimensions.Radius.medium))
            .padding(horizontal = 12.dp, vertical = 8.dp),
    )
}

@Composable
private fun WelcomeAvatar(initials: String, palette: Int) {
    val fill = if (palette % 2 == 0) SpotColors.Accent else SpotColors.WelcomeChipFill
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(fill)
            .border(2.dp, SpotColors.Background, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = SpotColors.Primary,
        )
    }
}
