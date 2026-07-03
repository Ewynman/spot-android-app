package com.spot.android.core.util

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for application-wide constants.
 * 
 * Verifies that constants match PRD specifications and maintain parity with iOS.
 */
class ConstantsTest {
    
    @Test
    fun `pagination constants match PRD specification`() {
        // PRD/02 Pagination constants
        assertEquals(24, Constants.Pagination.DEFAULT_PAGE_SIZE, "Default page size should be 24")
        assertEquals(100, Constants.Pagination.LARGE_PAGE_SIZE, "Large page size should be 100")
        assertEquals(200, Constants.Pagination.EXTRA_LARGE_PAGE_SIZE, "Extra large page size should be 200")
        assertEquals(500, Constants.Pagination.MAX_PAGE_SIZE, "Max page size should be 500")
    }
    
    @Test
    fun `post limits match PRD specification for platform parity`() {
        // Free tier limits
        assertEquals(1, Constants.PostLimits.FREE_MAX_IMAGES, "Free users can post 1 image")
        assertEquals(1, Constants.PostLimits.FREE_MAX_VIBES, "Free users can add 1 vibe")
        
        // Pro tier limits
        assertEquals(5, Constants.PostLimits.PRO_MAX_IMAGES, "Pro users can post 5 images")
        assertEquals(5, Constants.PostLimits.PRO_MAX_VIBES, "Pro users can add 5 vibes")
    }
    
    @Test
    fun `content limits match PRD specification`() {
        assertEquals(50, Constants.ContentLimits.FREE_BOOKMARK_CAP, "Free bookmark cap should be 50")
        assertEquals(250, Constants.ContentLimits.MAP_VISIBLE_SPOTS_CAP, "Map should cap at 250 spots")
    }
    
    @Test
    fun `vibe tag limits match PRD specification`() {
        // PRD/02 Vibe tag validation
        assertEquals(2, Constants.VibeTagLimits.MIN_LENGTH, "Vibe tags must be at least 2 characters")
        assertEquals(30, Constants.VibeTagLimits.MAX_LENGTH, "Vibe tags must be under 30 characters")
    }
    
    @Test
    fun `timeout values match PRD specification`() {
        assertEquals(90, Constants.Timeouts.PUBLISH_TIMEOUT_SECONDS, "Publish timeout should be 90 seconds")
        assertEquals(30, Constants.Timeouts.OTP_RESEND_COOLDOWN_SECONDS, "OTP resend cooldown should be 30 seconds")
    }
    
    @Test
    fun `auth constants match PRD specification`() {
        assertEquals(6, Constants.Auth.OTP_LENGTH, "OTP should be 6 digits")
    }
    
    @Test
    fun `search constants match PRD specification`() {
        assertEquals(300L, Constants.Search.DEBOUNCE_MS, "Search debounce should be 300ms")
        assertEquals(20, Constants.Search.HISTORY_MAX_PER_SEGMENT, "History max per segment should be 20")
        assertEquals(24, Constants.Search.GRID_PAGE_TARGET, "Grid page target should be 24")
    }
    
    @Test
    fun `social constants match PRD specification`() {
        assertEquals(24, Constants.Social.FOLLOW_REQUESTS_PAGE_SIZE, "Follow requests page size should be 24")
        assertEquals(8, Constants.Social.FOLLOW_REQUESTS_BADGE_POLL_INTERVAL_SECONDS, "Badge poll interval should be 8 seconds")
    }
    
    @Test
    fun `default vibe tags contain exactly 18 canonical tags`() {
        // PRD/02 specifies 18 default tags
        assertEquals(18, Constants.VibeTags.DEFAULT_TAGS.size, "Should have 18 default vibe tags")
    }
    
    @Test
    fun `default vibe tags match PRD specification`() {
        val expectedTags = setOf(
            "Chill Spot", "Hidden Gem", "Scenic View", "Romantic", "Great For Photos",
            "Family Friendly", "Nature Escape", "Foodie Heaven", "Beach Day", "Late Night",
            "Historical", "People Watching", "Quiet Moment", "Cozy Corner", "Pet Friendly",
            "Adventure", "Waterfront", "Study Spot"
        )
        
        assertEquals(expectedTags, Constants.VibeTags.DEFAULT_TAGS.toSet(), "Default tags should match PRD")
    }
    
    @Test
    fun `image processing constants match PRD specification`() {
        assertEquals(1600, Constants.ImageProcessing.MAX_DIMENSION_PX, "Max dimension should be 1600px")
        assertEquals(0.8f, Constants.ImageProcessing.JPEG_QUALITY, "JPEG quality should be 0.8")
    }
    
    @Test
    fun `storage bucket names match backend specification`() {
        assertEquals("spots", Constants.StorageBuckets.SPOTS, "Spots bucket should be 'spots'")
        assertEquals("pending_images", Constants.StorageBuckets.PENDING_IMAGES, "Pending images bucket should be 'pending_images'")
        assertEquals("avatars", Constants.StorageBuckets.AVATARS, "Avatars bucket should be 'avatars'")
    }
    
    @Test
    fun `signed URL expiry matches PRD specification`() {
        assertEquals(604800, Constants.SignedUrls.EXPIRY_SECONDS, "Signed URLs should expire in 7 days (604800 seconds)")
    }
    
    @Test
    fun `pro tier offers more capacity than free tier`() {
        assertTrue(
            Constants.PostLimits.PRO_MAX_IMAGES > Constants.PostLimits.FREE_MAX_IMAGES,
            "Pro should allow more images than free"
        )
        assertTrue(
            Constants.PostLimits.PRO_MAX_VIBES > Constants.PostLimits.FREE_MAX_VIBES,
            "Pro should allow more vibes than free"
        )
    }
}
