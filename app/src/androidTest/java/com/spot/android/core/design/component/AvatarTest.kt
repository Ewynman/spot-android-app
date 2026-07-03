package com.spot.android.core.design.component

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import com.spot.android.core.design.theme.SpotTheme
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented tests for Avatar component.
 */
class AvatarTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun avatar_displaysWithoutImage() {
        composeTestRule.setContent {
            SpotTheme {
                Avatar(
                    imageUrl = null,
                    testTag = "testAvatar"
                )
            }
        }
        
        composeTestRule
            .onNodeWithTag("testAvatar")
            .assertIsDisplayed()
    }
    
    @Test
    fun avatar_showsProRing_whenProIsTrue() {
        composeTestRule.setContent {
            SpotTheme {
                Avatar(
                    imageUrl = null,
                    isPro = true,
                    testTag = "testAvatar"
                )
            }
        }
        
        composeTestRule
            .onNodeWithContentDescription("User avatar, Pro user")
            .assertIsDisplayed()
    }
    
    @Test
    fun avatar_showsNoProRing_whenProIsFalse() {
        composeTestRule.setContent {
            SpotTheme {
                Avatar(
                    imageUrl = null,
                    isPro = false,
                    contentDescription = "User avatar",
                    testTag = "testAvatar"
                )
            }
        }
        
        composeTestRule
            .onNodeWithContentDescription("User avatar")
            .assertIsDisplayed()
    }
    
    @Test
    fun smallAvatar_displays() {
        composeTestRule.setContent {
            SpotTheme {
                SmallAvatar(
                    imageUrl = null,
                    testTag = "testSmallAvatar"
                )
            }
        }
        
        composeTestRule
            .onNodeWithTag("testSmallAvatar")
            .assertIsDisplayed()
    }
    
    @Test
    fun largeAvatar_displays() {
        composeTestRule.setContent {
            SpotTheme {
                LargeAvatar(
                    imageUrl = null,
                    testTag = "testLargeAvatar"
                )
            }
        }
        
        composeTestRule
            .onNodeWithTag("testLargeAvatar")
            .assertIsDisplayed()
    }
}
