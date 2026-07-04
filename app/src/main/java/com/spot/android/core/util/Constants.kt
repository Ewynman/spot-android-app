package com.spot.android.core.util

/**
 * Application-wide constants.
 * 
 * These values must match the iOS implementation exactly to ensure
 * consistent behavior across platforms when interacting with shared backend data.
 * 
 * Reference: PRD/01-architecture-android.md, PRD/02-design-system.md
 */
object Constants {
    
    /**
     * Pagination constants for feeds and grids.
     */
    object Pagination {
        const val DEFAULT_PAGE_SIZE = 24
        const val LARGE_PAGE_SIZE = 100
        const val EXTRA_LARGE_PAGE_SIZE = 200
        const val MAX_PAGE_SIZE = 500
    }
    
    /**
     * Post limits for free vs Pro users.
     */
    object PostLimits {
        const val FREE_MAX_IMAGES = 1
        const val FREE_MAX_VIBES = 1
        const val PRO_MAX_IMAGES = 5
        const val PRO_MAX_VIBES = 5
    }
    
    /**
     * Bookmark and content limits.
     */
    object ContentLimits {
        const val FREE_BOOKMARK_CAP = 50
        const val MAP_VISIBLE_SPOTS_CAP = 250
    }
    
    /**
     * Vibe tag validation.
     */
    object VibeTagLimits {
        const val MIN_LENGTH = 2
        const val MAX_LENGTH = 30
    }
    
    /**
     * Publish and timeout limits.
     */
    object Timeouts {
        const val PUBLISH_TIMEOUT_SECONDS = 90
        const val OTP_RESEND_COOLDOWN_SECONDS = 30
    }
    
    /**
     * Auth constants.
     */
    object Auth {
        const val OTP_LENGTH = 6
    }

    /**
     * Launch splash timing (mirrors iOS).
     */
    object Launch {
        const val SPLASH_MIN_DURATION_MS = 1500L
        const val SPLASH_FADE_DURATION_MS = 500L
    }
    
    /**
     * Search constants.
     */
    object Search {
        const val DEBOUNCE_MS = 300L
        const val HISTORY_MAX_PER_SEGMENT = 20
        const val GRID_PAGE_TARGET = 24
    }
    
    /**
     * Profile and social constants.
     */
    object Social {
        const val FOLLOW_REQUESTS_PAGE_SIZE = 24
        const val FOLLOW_REQUESTS_BADGE_POLL_INTERVAL_SECONDS = 8
    }
    
    /**
     * Default vibe tags (canonical set of 18).
     */
    object VibeTags {
        val DEFAULT_TAGS = listOf(
            "Chill Spot",
            "Hidden Gem",
            "Scenic View",
            "Romantic",
            "Great For Photos",
            "Family Friendly",
            "Nature Escape",
            "Foodie Heaven",
            "Beach Day",
            "Late Night",
            "Historical",
            "People Watching",
            "Quiet Moment",
            "Cozy Corner",
            "Pet Friendly",
            "Adventure",
            "Waterfront",
            "Study Spot"
        )
    }
    
    /**
     * Image processing constants.
     */
    object ImageProcessing {
        const val MAX_DIMENSION_PX = 1600
        const val JPEG_QUALITY = 0.8f
    }
    
    /**
     * Storage bucket names.
     */
    object StorageBuckets {
        const val SPOTS = "spots"
        const val PENDING_IMAGES = "pending_images"
        const val AVATARS = "avatars"
    }
    
    /**
     * Signed URL configuration.
     */
    object SignedUrls {
        const val EXPIRY_SECONDS = 604800 // 7 days
    }

    /**
     * Analytics event names mirroring iOS `Constants.Analytics`.
     *
     * Reference: PRD/17-non-functional-testing.md
     */
    object Analytics {
        const val AUTH_REINSTALL = "AuthReinstall"
        const val PERMS_REQUESTED = "Perms.Requested"
        const val FEED_DROP_PRIVATE = "Feed.DropPrivate"
        const val IMAGE_LOAD_FAILED = "Image.LoadFailed"
        const val AUTH_EMAIL_IN_USE = "Auth.EmailInUse"
        const val AUTH_DELETE_BY_EMAIL = "Auth.DeleteByEmail"
        const val DEEP_LINK = "DeepLink"

        object Params {
            const val PERMISSION_TYPE = "permission_type"
            const val REASON = "reason"
            const val SOURCE = "source"
            const val ORIGIN = "origin"
            const val ROUTE = "route"
        }
    }
}
