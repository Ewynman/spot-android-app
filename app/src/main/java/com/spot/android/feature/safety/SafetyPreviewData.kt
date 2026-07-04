package com.spot.android.feature.safety

import com.spot.android.data.model.Spot

/**
 * Shared preview/demo data for safety flow wiring before full feed/profile land.
 */
object SafetyPreviewData {
    val demoSpot = Spot(
        id = "00000000-0000-4000-8000-000000000001",
        userId = "00000000-0000-4000-8000-000000000099",
        username = "demo_user",
        userProfileImageURL = null,
        caption = "Demo spot for safety overflow menu",
        latitude = 37.7749,
        longitude = -122.4194,
        locationName = "San Francisco, CA",
        likes = 12,
        saves = 3,
        createdAt = System.currentTimeMillis(),
        updatedAt = null,
        imageURL = null,
        thumbnailURL = null,
        mediaDisplayAspectRatio = 1.0,
        mediaCount = 1,
        vibeTag = "Scenic View",
        authorIsPrivate = false,
        authorIsPro = false,
        isLiked = false,
        isSaved = false,
    )

    const val DEMO_PROFILE_USER_ID = "00000000-0000-4000-8000-000000000099"
    const val DEMO_PROFILE_USERNAME = "demo_user"
}
