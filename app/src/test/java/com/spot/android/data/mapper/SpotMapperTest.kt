package com.spot.android.data.mapper

import com.spot.android.data.dto.HomeFeedRowDto
import com.spot.android.data.dto.MapSpotRowDto
import com.spot.android.data.dto.VibeRowDto
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for SpotMapper.
 */
class SpotMapperTest {
    
    @Test
    fun `fromHomeFeedRow maps correctly`() {
        val dto = HomeFeedRowDto(
            spot_id = "spot-123",
            user_id = "user-456",
            vibe_tag_id = "vibe-789",
            caption = "Test caption",
            latitude = 37.7749,
            longitude = -122.4194,
            location_name = "San Francisco",
            likes_count = 42L,
            saves_count = 10L,
            created_at = "2026-01-15T12:00:00Z",
            updated_at = "2026-01-16T12:00:00Z",
            author_username = "testuser",
            author_profile_image_url = "https://example.com/avatar.jpg",
            author_is_private = false,
            vibe_name = "Chill Spot",
            primary_storage_path = "path/to/image.jpg",
            primary_public_url = "https://example.com/image.jpg",
            source_bucket = "personalized_unseen",
            rank_position = 1,
            ranking_score = 0.95,
            seen_before = false,
            last_seen_at = null,
            media_display_aspect_ratio = 1.5,
            media_count = 3
        )
        
        val spot = SpotMapper.fromHomeFeedRow(dto)
        
        assertEquals("spot-123", spot.id)
        assertEquals("user-456", spot.userId)
        assertEquals("testuser", spot.username)
        assertEquals("Test caption", spot.caption)
        assertEquals(37.7749, spot.latitude, 0.0001)
        assertEquals(-122.4194, spot.longitude, 0.0001)
        assertEquals("San Francisco", spot.locationName)
        assertEquals(42L, spot.likes)
        assertEquals(10L, spot.saves)
        assertEquals("Chill Spot", spot.vibeTag)
        assertFalse(spot.authorIsPrivate)
        assertEquals(1.5, spot.mediaDisplayAspectRatio, 0.0001)
        assertEquals(3, spot.mediaCount)
        assertEquals(1, spot.rankPosition)
        assertEquals(0.95, spot.rankingScore ?: 0.0, 0.0001)
        assertEquals(false, spot.seenBefore)
    }
    
    @Test
    fun `fromMapSpotRow maps correctly`() {
        val dto = MapSpotRowDto(
            spot_id = "spot-123",
            user_id = "user-456",
            vibe_tag_id = "vibe-789",
            caption = "Test caption",
            latitude = 37.7749,
            longitude = -122.4194,
            location_name = "San Francisco",
            created_at = "2026-01-15T12:00:00Z",
            author_username = "testuser",
            author_profile_image_url = "https://example.com/avatar.jpg",
            vibe_name = "Hidden Gem",
            primary_storage_path = "path/to/image.jpg",
            primary_public_url = "https://example.com/image.jpg",
            distance_meters = 1234.56
        )
        
        val spot = SpotMapper.fromMapSpotRow(dto)
        
        assertEquals("spot-123", spot.id)
        assertEquals("testuser", spot.username)
        assertEquals("Hidden Gem", spot.vibeTag)
        assertEquals(1234.56, spot.distanceMeters ?: 0.0, 0.01)
        assertFalse(spot.authorIsPrivate)
    }
    
    @Test
    fun `fromVibeRow maps correctly`() {
        val dto = VibeRowDto(
            id = "vibe-123",
            name = "Chill Spot",
            name_lower = "chill spot",
            created_at = "2026-01-15T12:00:00Z"
        )
        
        val vibeTag = SpotMapper.fromVibeRow(dto)
        
        assertEquals("vibe-123", vibeTag.id)
        assertEquals("Chill Spot", vibeTag.name)
        assertEquals("chill spot", vibeTag.nameLower)
    }
}
