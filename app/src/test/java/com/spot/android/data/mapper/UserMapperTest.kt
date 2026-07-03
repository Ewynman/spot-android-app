package com.spot.android.data.mapper

import com.spot.android.data.dto.UserBriefRowDto
import com.spot.android.data.dto.UserRowDto
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for UserMapper.
 */
class UserMapperTest {
    
    @Test
    fun `fromUserBriefRow maps correctly`() {
        val dto = UserBriefRowDto(
            id = "user-123",
            username = "testuser",
            profile_image_url = "https://example.com/avatar.jpg",
            is_pro = true,
            pro_until = "2027-01-15T12:00:00Z",
            is_private = false,
            spots_count = 42L,
            created_at = "2026-01-15T12:00:00Z"
        )
        
        val user = UserMapper.fromUserBriefRow(dto, isCurrentUser = true)
        
        assertEquals("user-123", user.id)
        assertEquals("testuser", user.username)
        assertEquals("https://example.com/avatar.jpg", user.profileImageURL)
        assertTrue(user.isPro)
        assertFalse(user.isPrivate)
        assertEquals(42L, user.spotsCount)
        assertTrue(user.isCurrentUser)
        assertNull(user.email)
    }
    
    @Test
    fun `fromUserRow maps correctly`() {
        val dto = UserRowDto(
            id = "user-123",
            email = "test@example.com",
            email_verified = true,
            username = "testuser",
            username_lower = "testuser",
            profile_image_url = "https://example.com/avatar.jpg",
            is_private = true,
            is_pro = false,
            pro_until = null,
            last_active_at = "2026-07-03T12:00:00Z",
            locale = "en",
            spots_count = 10L,
            reported_count = 0L,
            created_at = "2026-01-15T12:00:00Z",
            updated_at = "2026-07-03T12:00:00Z",
            profile_image_asset_id = null,
            suspended_for_reports_at = null,
            account_status = "active",
            moderation_status = "approved"
        )
        
        val user = UserMapper.fromUserRow(dto)
        
        assertEquals("user-123", user.id)
        assertEquals("testuser", user.username)
        assertEquals("test@example.com", user.email)
        assertTrue(user.emailVerified)
        assertTrue(user.isPrivate)
        assertFalse(user.isPro)
        assertEquals(10L, user.spotsCount)
        assertTrue(user.isCurrentUser)
    }
}
