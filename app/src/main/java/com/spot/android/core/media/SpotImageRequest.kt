package com.spot.android.core.media

/**
 * Custom image request data class for Spot images.
 * 
 * This is used to pass storage bucket and path information to Coil's custom fetcher.
 * Instead of passing a regular URL string, we pass this object which contains
 * the bucket name and storage path, allowing the fetcher to generate signed URLs.
 * 
 * Usage in Compose:
 * ```kotlin
 * AsyncImage(
 *     model = SpotImageRequest(
 *         storagePath = "user-id/spot-id/image.jpg",
 *         bucket = "spots"
 *     ),
 *     contentDescription = "Spot image"
 * )
 * ```
 * 
 * Reference: PRD/04-backend-api.md
 */
data class SpotImageRequest(
    /**
     * Path within the storage bucket.
     * Example: "user-id/spot-id/image.jpg"
     */
    val storagePath: String,
    
    /**
     * Bucket name: "spots", "pending_images", or "avatars".
     * Defaults to "spots".
     */
    val bucket: String = "spots"
)
