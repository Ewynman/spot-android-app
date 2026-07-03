package com.spot.android.core.design.component

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.spot.android.core.design.theme.SpotTheme
import com.spot.android.data.model.Spot
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented tests for SpotCard component.
 */
class SpotCardTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    private val testSpot = Spot(
        id = "test-spot-1",
        userId = "test-user-1",
        username = "testuser",
        userProfileImageURL = null,
        caption = "Test caption",
        latitude = 37.7749,
        longitude = -122.4194,
        locationName = "San Francisco, CA",
        likes = 42,
        saves = 12,
        createdAt = System.currentTimeMillis(),
        updatedAt = null,
        imageURL = null,
        thumbnailURL = null,
        mediaDisplayAspectRatio = 1.0,
        mediaCount = 1,
        vibeTag = "Scenic View",
        authorIsPrivate = false,
        authorIsPro = true,
        isLiked = false,
        isSaved = false
    )
    
    @Test
    fun spotCard_displaysUsername() {
        composeTestRule.setContent {
            SpotTheme {
                SpotCard(spot = testSpot)
            }
        }
        
        composeTestRule
            .onNodeWithTag("spotCard.username")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("testuser")
            .assertIsDisplayed()
    }
    
    @Test
    fun spotCard_displaysProBadge_whenAuthorIsPro() {
        composeTestRule.setContent {
            SpotTheme {
                SpotCard(spot = testSpot.copy(authorIsPro = true))
            }
        }
        
        composeTestRule
            .onNodeWithTag("spotCard.proBadge")
            .assertIsDisplayed()
    }
    
    @Test
    fun spotCard_hidesProBadge_whenAuthorIsNotPro() {
        composeTestRule.setContent {
            SpotTheme {
                SpotCard(spot = testSpot.copy(authorIsPro = false))
            }
        }
        
        composeTestRule
            .onNodeWithTag("spotCard.proBadge")
            .assertDoesNotExist()
    }
    
    @Test
    fun spotCard_displaysVibeTag() {
        composeTestRule.setContent {
            SpotTheme {
                SpotCard(spot = testSpot)
            }
        }
        
        composeTestRule
            .onNodeWithTag("spotCard.vibeChip")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Scenic View")
            .assertIsDisplayed()
    }
    
    @Test
    fun spotCard_displaysLocation() {
        composeTestRule.setContent {
            SpotTheme {
                SpotCard(spot = testSpot)
            }
        }
        
        composeTestRule
            .onNodeWithTag("spotCard.location")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("San Francisco, CA")
            .assertIsDisplayed()
    }
    
    @Test
    fun spotCard_displaysInteractionBar() {
        composeTestRule.setContent {
            SpotTheme {
                SpotCard(spot = testSpot)
            }
        }
        
        composeTestRule
            .onNodeWithTag("spotCard.interactionBar")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithTag("spotCard.likeButton")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithTag("spotCard.bookmarkButton")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithTag("spotCard.moreButton")
            .assertIsDisplayed()
    }
    
    @Test
    fun spotCard_likeButtonClick_triggersCallback() {
        var likeClicked = false
        
        composeTestRule.setContent {
            SpotTheme {
                SpotCard(
                    spot = testSpot,
                    onLikeClick = { likeClicked = true }
                )
            }
        }
        
        composeTestRule
            .onNodeWithTag("spotCard.likeButton")
            .performClick()
        
        assert(likeClicked)
    }
    
    @Test
    fun spotCard_bookmarkButtonClick_triggersCallback() {
        var bookmarkClicked = false
        
        composeTestRule.setContent {
            SpotTheme {
                SpotCard(
                    spot = testSpot,
                    onBookmarkClick = { bookmarkClicked = true }
                )
            }
        }
        
        composeTestRule
            .onNodeWithTag("spotCard.bookmarkButton")
            .performClick()
        
        assert(bookmarkClicked)
    }
    
    @Test
    fun spotCard_moreButtonClick_triggersCallback() {
        var moreClicked = false
        
        composeTestRule.setContent {
            SpotTheme {
                SpotCard(
                    spot = testSpot,
                    onMoreClick = { moreClicked = true }
                )
            }
        }
        
        composeTestRule
            .onNodeWithTag("spotCard.moreButton")
            .performClick()
        
        assert(moreClicked)
    }
    
    @Test
    fun spotCard_displaysLikesCount_whenGreaterThanZero() {
        composeTestRule.setContent {
            SpotTheme {
                SpotCard(spot = testSpot.copy(likes = 42))
            }
        }
        
        composeTestRule
            .onNodeWithTag("spotCard.likesCount")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("42")
            .assertIsDisplayed()
    }
}
