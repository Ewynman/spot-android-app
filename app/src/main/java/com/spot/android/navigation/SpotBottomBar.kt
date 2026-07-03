package com.spot.android.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spot.android.core.design.theme.SpotColors

/**
 * Custom 5-item bottom tab bar for the main app shell.
 *
 * Not the system default NavigationBar — matches iOS custom tab bar styling.
 * Reselecting the active tab invokes [onTabReselected] instead of navigating.
 *
 * Reference: PRD/00-overview.md, PRD/02-design-system.md
 */
@Composable
fun SpotBottomBar(
    selectedTab: SpotTab,
    onTabSelected: (SpotTab) -> Unit,
    onTabReselected: (SpotTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SpotColors.Background)
            .navigationBarsPadding()
            .padding(vertical = 8.dp)
            .testTag("navigation.bottomBar"),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SpotTab.entries.forEach { tab ->
            val isSelected = tab == selectedTab
            val label = stringResource(tab.labelRes)

            Column(
                modifier = Modifier
                    .testTag(tab.testTag)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            if (isSelected) {
                                onTabReselected(tab)
                            } else {
                                onTabSelected(tab)
                            }
                        },
                    )
                    .semantics {
                        contentDescription = label
                    }
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = null,
                    tint = if (isSelected) SpotColors.Primary else SpotColors.WelcomeMutedText,
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) SpotColors.Primary else SpotColors.WelcomeMutedText,
                )
            }
        }
    }
}
