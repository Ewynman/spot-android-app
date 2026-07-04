package com.spot.android.feature.safety

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spot.android.core.design.theme.SpotTheme
import com.spot.android.data.model.enums.ReportReason
import com.spot.android.data.model.enums.ReportTargetType
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReportSheetTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun reportSheet_disablesSubmitUntilReasonSelected() {
        composeRule.setContent {
            SpotTheme {
                ReportSheet(
                    state = ReportSheetState(
                        targetType = ReportTargetType.SPOT,
                        targetId = "spot-1",
                        reportedUserId = "user-1",
                        reportedUsername = "demo_user",
                    ),
                    isSubmitting = false,
                    errorMessage = null,
                    onReasonSelected = {},
                    onDetailsChanged = {},
                    onBlockRequestedChanged = {},
                    onSubmit = {},
                    onDismiss = {},
                )
            }
        }

        composeRule.onNodeWithTag("safety.reportSheet").assertIsDisplayed()
        composeRule.onNodeWithTag("safety.reportSheet.submit").assertExists()
        composeRule.onNodeWithTag("safety.reportSheet.reason.spam").performClick()
    }

    @Test
    fun blockDialog_showsConfirmAndCancel() {
        composeRule.setContent {
            SpotTheme {
                BlockUserDialog(
                    state = BlockDialogState(
                        blockedUserId = "user-1",
                        blockedUsername = "demo_user",
                    ),
                    isSubmitting = false,
                    errorMessage = null,
                    onConfirm = {},
                    onDismiss = {},
                )
            }
        }

        composeRule.onNodeWithTag("safety.blockDialog").assertIsDisplayed()
        composeRule.onNodeWithTag("safety.blockDialog.confirm").assertIsDisplayed()
        composeRule.onNodeWithTag("safety.blockDialog.cancel").assertIsDisplayed()
    }
}
