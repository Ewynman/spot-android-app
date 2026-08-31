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
        const val USERNAME_SETUP_LOAD_DELAY_MS = 350L
    }

    /**
     * Launch splash timing (mirrors iOS).
     */
    object Launch {
        const val SPLASH_MIN_DURATION_MS = 1500L
        const val SPLASH_FADE_DURATION_MS = 500L
    }

    /**
     * Permission prompt timing (mirrors iOS).
     */
    object Permissions {
        const val NOTIFICATION_PROMPT_DELAY_MS = 600L
        const val TOUR_START_DELAY_MS = 500L
    }
    
    /**
     * Search constants.
     */
    object Search {
        const val DEBOUNCE_MS = 300L
        const val HISTORY_MAX_PER_SEGMENT = 20
        const val GRID_PAGE_TARGET = 24
        const val GRID_MAX_FETCH_ATTEMPTS = 5
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
     * Map tuning constants mirroring iOS `Constants.MapDesign`.
     *
     * Reference: PRD/07-map.md
     */
    object MapDesign {
        const val INITIAL_RADIUS_METERS = 4_000.0
        const val INITIAL_NEIGHBORHOOD_RADIUS_METERS = 3_200.0
        const val LOCAL_SPAN = 0.04
        const val CITY_SPAN = 0.30
        const val VISIBLE_SPOTS_CAP = 250
        const val FAR_ZOOM_PIN_CAP = 60
        const val PIN_SIZE_DP = 22
        const val PIN_SELECTED_SCALE = 1.28f
        const val PIN_PRESSED_SCALE = 0.92f
        const val AVATAR_MARKER_SIZE_DP = 38
        const val AVATAR_RING_WIDTH_DP = 3
        const val PIN_ENTRY_DURATION_MS = 280L
        const val PIN_STAGGER_STEP_MS = 12L
        const val PIN_STAGGER_CAP_MS = 250L
        const val SELECT_SPRING_RESPONSE = 0.32f
        const val SELECT_SPRING_DAMPING = 0.82f
        const val REGION_DEBOUNCE_FAST_MS = 180L
        const val REGION_DEBOUNCE_SLOW_MS = 380L
        const val VIEWPORT_FETCH_DEBOUNCE_MS = 250L
        const val SELECTED_PIN_CAMERA_LIFT_METERS = 90.0
        const val OVERLAP_BUCKET_SIZE = 0.00005
        const val OVERLAP_OFFSET_METERS = 12.0
        const val PANEL_MAX_SCREEN_FRACTION = 0.65f
        const val PANEL_MIN_HEIGHT_DP = 280
        const val MAP_DRAWER_TOP_CORNER_RADIUS_DP = 22
        const val MAP_DRAWER_GAP_BELOW_FILTER_PILLS_DP = 5
        const val MAP_MOVED_DISMISS_THRESHOLD_METERS = 150.0

        // Default continental-US fallback when location is denied.
        const val FALLBACK_LATITUDE = 39.8283
        const val FALLBACK_LONGITUDE = -98.5795
        const val FALLBACK_ZOOM = 4f
        const val NEIGHBORHOOD_ZOOM = 13f
        const val DEFAULT_ZOOM = 12f
    }

    /**
     * Feed behavioral event timing and coalescing thresholds.
     *
     * Reference: PRD/06-home-feed.md, PRD/16-feed-ranking-algorithm.md
     */
    object FeedEvents {
        const val VISIBLE_2S_MS = 2_000L
        const val LONG_DWELL_MS = 8_000L
        const val QUICK_SKIP_MAX_MS = 1_500L
        const val COALESCE_MAX_ENTRIES = 256
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
        const val MAP_MARKER_IMPRESSION = "map_marker_impression"
        const val MAP_MARKER_IMAGE_LOAD = "map_marker_image_load"
        const val MAP_MARKER_TAPPED = "map_marker_tapped"

        object Params {
            const val PERMISSION_TYPE = "permission_type"
            const val REASON = "reason"
            const val SOURCE = "source"
            const val ORIGIN = "origin"
            const val ROUTE = "route"
            const val MARKER_TYPE = "marker_type"
            const val ZOOM_LEVEL = "zoom_level"
            const val SUCCESS = "success"
            const val CACHE_HIT = "cache_hit"
        }

        object Values {
            const val MARKER_TYPE_PHOTO_PIN = "photo_pin"
            const val MARKER_TYPE_TEARDROP = "teardrop"
        }
    }

    /**
     * Photo-preview map pin (task 12).
     *
     * The bespoke marker image cache stores small downsampled bitmaps to keep
     * memory bounded during aggressive pan/zoom; sizes and cap match the iOS
     * "Concept 3" implementation.
     */
    object PhotoPin {
        const val FRAME_WIDTH_DP = 44
        const val FRAME_HEIGHT_DP = 56
        const val CIRCLE_DIAMETER_DP = 44
        const val TAIL_HEIGHT_DP = 12
        const val TAIL_WIDTH_DP = 12
        const val BORDER_DP = 2f
        const val SELECTED_SCALE = 1.10f
        const val SELECTED_Z_INDEX = 100f
        const val SELECT_ANIM_MS = 150

        const val IMAGE_CACHE_MAX_ENTRIES = 128

        /** Base target size in px at mdpi (density = 1.0). 2× 44dp = 88px. */
        const val IMAGE_TARGET_BASE_PX = 88

        /** Hard upper bound for high-density displays. */
        const val IMAGE_TARGET_MAX_PX = 176
    }
}
