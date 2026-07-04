package com.spot.android.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spot.android.core.design.component.Avatar
import com.spot.android.core.design.component.TopNavigationView
import com.spot.android.core.design.theme.SpotColors
import com.spot.android.feature.safety.LocalSafetyActions
import com.spot.android.feature.safety.SafetyPreviewData

/**
 * Profile tab placeholder with a demo other-user header wired to safety overflow actions.
 *
 * Full profile implementation lands in Phase 3.5 (PRD/10).
 */
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
) {
    val safetyActions = LocalSafetyActions.current

    Scaffold(
        modifier = modifier.testTag("profile.profileRoot"),
        topBar = { TopNavigationView() },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(
                text = "Your Profile",
                style = MaterialTheme.typography.titleMedium,
                color = SpotColors.Primary.copy(alpha = 0.7f),
            )

            if (safetyActions != null) {
                DemoOtherUserProfileHeader(
                    userId = SafetyPreviewData.DEMO_PROFILE_USER_ID,
                    username = SafetyPreviewData.DEMO_PROFILE_USERNAME,
                    onOverflowClick = {
                        safetyActions.openProfileOverflowMenu(
                            SafetyPreviewData.DEMO_PROFILE_USER_ID,
                            SafetyPreviewData.DEMO_PROFILE_USERNAME,
                        )
                    },
                )
            } else {
                Text(
                    text = "Profile",
                    color = SpotColors.Primary,
                )
            }
        }
    }
}

@Composable
private fun DemoOtherUserProfileHeader(
    userId: String,
    username: String,
    onOverflowClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("profile.otherUserHeader"),
    ) {
        Text(
            text = "Other user preview",
            style = MaterialTheme.typography.labelMedium,
            color = SpotColors.Primary.copy(alpha = 0.6f),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Avatar(
                imageUrl = null,
                isPro = false,
                contentDescription = "$username avatar",
                modifier = Modifier.testTag("profile.otherUserAvatar"),
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = username,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = SpotColors.Primary,
                modifier = Modifier
                    .weight(1f)
                    .testTag("profile.otherUserUsername"),
            )

            IconButton(
                onClick = onOverflowClick,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("profile.otherUserOverflow"),
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Profile options",
                    tint = SpotColors.Primary,
                )
            }
        }
    }
}
