package com.spot.android.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for application-wide constants.
 * 
 * Verifies that constants match PRD specifications and maintain parity with iOS.
 */
class ConstantsTest {
    
    @Test
    fun `pagination constants match PRD specification`() {
        // PRD/02 Pagination constants
        assertEquals("Default page size should be 24", 24, Constants.Pagination.DEFAULT_PAGE_SIZE)
        assertEquals("Large page size should be 100", 100, Constants.Pagination.LARGE_PAGE_SIZE)
        assertEquals("Extra large page size should be 200", 200, Constants.Pagination.EXTRA_LARGE_PAGE_SIZE)
        assertEquals("Max page size should be 500", 500, Constants.Pagination.MAX_PAGE_SIZE)
    }
    
    @Test
    fun `post limits match PRD specification for platform parity`() {
        // Free tier limits
        assertEquals("Free users can post 1 image", 1, Constants.PostLimits.FREE_MAX_IMAGES)
        assertEquals("Free users can add 1 vibe", 1, Constants.PostLimits.FREE_MAX_VIBES)
        
        // Pro tier limits
        assertEquals("Pro users can post 5 images", 5, Constants.PostLimits.PRO_MAX_IMAGES)
        assertEquals("Pro users can add 5 vibes", 5, Constants.PostLimits.PRO_MAX_VIBES)
    }
    
    @Test
    fun `content limits match PRD specification`() {
        assertEquals("Free bookmark cap should be 50", 50, Constants.ContentLimits.FREE_BOOKMARK_CAP)
        assertEquals("Map should cap at 250 spots", 250, Constants.ContentLimits.MAP_VISIBLE_SPOTS_CAP)
    }
    
    @Test
    fun `vibe tag limits match PRD specification`() {
        // PRD/02 Vibe tag validation
        assertEquals("Vibe tags must be at least 2 characters", 2, Constants.VibeTagLimits.MIN_LENGTH)
        assertEquals("Vibe tags must be under 30 characters", 30, Constants.VibeTagLimits.MAX_LENGTH)
    }
    
    @Test
    fun `timeout values match PRD specification`() {
        assertEquals("Publish timeout should be 90 seconds", 90, Constants.Timeouts.PUBLISH_TIMEOUT_SECONDS)
        assertEquals("OTP resend cooldown should be 30 seconds", 30, Constants.Timeouts.OTP_RESEND_COOLDOWN_SECONDS)
    }
    
    @Test
    fun `auth constants match PRD specification`() {
        assertEquals("OTP should be 6 digits", 6, Constants.Auth.OTP_LENGTH)
    }
    
    @Test
    fun `search constants match PRD specification`() {
        assertEquals("Search debounce should be 300ms", 300L, Constants.Search.DEBOUNCE_MS)
        assertEquals("History max per segment should be 20", 20, Constants.Search.HISTORY_MAX_PER_SEGMENT)
        assertEquals("Grid page target should be 24", 24, Constants.Search.GRID_PAGE_TARGET)
    }
    
    @Test
    fun `social constants match PRD specification`() {
        assertEquals("Follow requests page size should be 24", 24, Constants.Social.FOLLOW_REQUESTS_PAGE_SIZE)
        assertEquals("Badge poll interval should be 8 seconds", 8, Constants.Social.FOLLOW_REQUESTS_BADGE_POLL_INTERVAL_SECONDS)
    }
    
    @Test
    fun `default vibe tags contain exactly 18 canonical tags`() {
        // PRD/02 specifies 18 default tags
        assertEquals("Should have 18 default vibe tags", 18, Constants.VibeTags.DEFAULT_TAGS.size)
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
        assertEquals("Max dimension should be 1600px", 1600, Constants.ImageProcessing.MAX_DIMENSION_PX)
        assertEquals("JPEG quality should be 0.8", 0.8f, Constants.ImageProcessing.JPEG_QUALITY, 0.001f)
    }
    
    @Test
    fun `storage bucket names match backend specification`() {
        assertEquals("Spots bucket should be 'spots'", "spots", Constants.StorageBuckets.SPOTS)
        assertEquals("Pending images bucket should be 'pending_images'", "pending_images", Constants.StorageBuckets.PENDING_IMAGES)
        assertEquals("Avatars bucket should be 'avatars'", "avatars", Constants.StorageBuckets.AVATARS)
    }
    
    @Test
    fun `signed URL expiry matches PRD specification`() {
        assertEquals("Signed URLs should expire in 7 days (604800 seconds)", 604800, Constants.SignedUrls.EXPIRY_SECONDS)
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
