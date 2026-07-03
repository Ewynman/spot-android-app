package com.spot.android.data.model

import com.spot.android.data.model.enums.ImageOrientation

/**
 * Domain model for a spot image.
 * 
 * Assembled from SpotImageRowDto.
 * 
 * Reference: PRD/03-data-model.md
 */
data class SpotImage(
    val id: String,
    val spotId: String,
    val storagePath: String,
    val publicUrl: String,
    val sortIndex: Int,
    val storageBucket: String,
    val width: Int?,
    val height: Int?,
    val aspectRatio: Double?,
    val displayAspectRatio: Double,
    val orientation: ImageOrientation,
    
    // Signed URL (computed client-side)
    var signedUrl: String? = null,
    var signedUrlExpiresAt: Long? = null // Epoch millis
) {
    /**
     * Check if the signed URL needs refreshing.
     * Refresh 1 hour before expiry.
     */
    fun needsSignedUrlRefresh(): Boolean {
        val expiresAt = signedUrlExpiresAt ?: return true
        val now = System.currentTimeMillis()
        val oneHourMs = 60 * 60 * 1000
        return (expiresAt - now) < oneHourMs
    }
}
