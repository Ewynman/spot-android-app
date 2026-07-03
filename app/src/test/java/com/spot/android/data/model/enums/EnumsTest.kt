package com.spot.android.data.model.enums

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for enum classes.
 */
class EnumsTest {
    
    @Test
    fun `AccountStatus fromValue works correctly`() {
        assertEquals(AccountStatus.ACTIVE, AccountStatus.fromValue("active"))
        assertEquals(AccountStatus.RESTRICTED, AccountStatus.fromValue("restricted"))
        assertEquals(AccountStatus.SUSPENDED, AccountStatus.fromValue("suspended"))
        assertEquals(AccountStatus.BANNED, AccountStatus.fromValue("banned"))
        assertNull(AccountStatus.fromValue("invalid"))
        assertNull(AccountStatus.fromValue(null))
    }
    
    @Test
    fun `ModerationStatus fromValue works correctly`() {
        assertEquals(ModerationStatus.APPROVED, ModerationStatus.fromValue("approved"))
        assertEquals(ModerationStatus.FLAGGED, ModerationStatus.fromValue("flagged"))
        assertEquals(ModerationStatus.REJECTED, ModerationStatus.fromValue("rejected"))
        assertEquals(ModerationStatus.PENDING_REVIEW, ModerationStatus.fromValue("pending_review"))
        assertNull(ModerationStatus.fromValue("invalid"))
    }
    
    @Test
    fun `MediaAssetStatus fromValue works correctly`() {
        assertEquals(MediaAssetStatus.PENDING, MediaAssetStatus.fromValue("pending"))
        assertEquals(MediaAssetStatus.APPROVED, MediaAssetStatus.fromValue("approved"))
        assertEquals(MediaAssetStatus.REJECTED, MediaAssetStatus.fromValue("rejected"))
        assertEquals(MediaAssetStatus.FAILED, MediaAssetStatus.fromValue("failed"))
        assertEquals(MediaAssetStatus.DELETED, MediaAssetStatus.fromValue("deleted"))
        assertEquals(MediaAssetStatus.LEGACY_UNMODERATED, MediaAssetStatus.fromValue("legacy_unmoderated"))
        assertNull(MediaAssetStatus.fromValue("invalid"))
    }
    
    @Test
    fun `FeedEventType fromValue works correctly`() {
        assertEquals(FeedEventType.IMPRESSION, FeedEventType.fromValue("impression"))
        assertEquals(FeedEventType.LIKE, FeedEventType.fromValue("like"))
        assertEquals(FeedEventType.UNLIKE, FeedEventType.fromValue("unlike"))
        assertEquals(FeedEventType.SAVE, FeedEventType.fromValue("save"))
        assertEquals(FeedEventType.BLOCK_AUTHOR, FeedEventType.fromValue("block_author"))
        assertNull(FeedEventType.fromValue("invalid"))
    }
    
    @Test
    fun `ReportReason has correct display names`() {
        assertEquals("Spam", ReportReason.SPAM.displayName)
        assertEquals("Harassment", ReportReason.HARASSMENT.displayName)
        assertEquals("Hate Speech", ReportReason.HATE_SPEECH.displayName)
        assertEquals("Violence", ReportReason.VIOLENCE.displayName)
        assertEquals("Other", ReportReason.OTHER.displayName)
    }
    
    @Test
    fun `ImageOrientation fromValue works correctly`() {
        assertEquals(ImageOrientation.LANDSCAPE, ImageOrientation.fromValue("landscape"))
        assertEquals(ImageOrientation.SQUARE, ImageOrientation.fromValue("square"))
        assertEquals(ImageOrientation.PORTRAIT, ImageOrientation.fromValue("portrait"))
        assertNull(ImageOrientation.fromValue("invalid"))
    }
    
    @Test
    fun `FollowRequestStatus fromValue works correctly`() {
        assertEquals(FollowRequestStatus.PENDING, FollowRequestStatus.fromValue("pending"))
        assertEquals(FollowRequestStatus.ACCEPTED, FollowRequestStatus.fromValue("accepted"))
        assertEquals(FollowRequestStatus.REJECTED, FollowRequestStatus.fromValue("rejected"))
        assertEquals(FollowRequestStatus.CANCELLED, FollowRequestStatus.fromValue("cancelled"))
        assertNull(FollowRequestStatus.fromValue("invalid"))
    }
}
