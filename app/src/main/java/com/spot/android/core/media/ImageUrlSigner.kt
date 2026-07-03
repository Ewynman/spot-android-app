package com.spot.android.core.media

import com.spot.android.core.supabase.SupabaseClientProvider
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for generating and caching signed URLs for private Supabase Storage buckets.
 * 
 * Signed URLs are cached until near their expiry (7 days) to avoid re-signing on every render.
 * Public bucket (avatars) URLs are generated without signing.
 * 
 * Buckets:
 * - `spots`: Private bucket for approved spot images (requires signing)
 * - `pending_images`: Private bucket for pre-moderation uploads (requires signing)
 * - `avatars`: Public bucket for profile photos (no signing required)
 * 
 * Reference: PRD/04-backend-api.md, PRD/17-non-functional-testing.md
 */
@Singleton
class ImageUrlSigner @Inject constructor(
    private val supabaseProvider: SupabaseClientProvider
) {
    
    /**
     * Signed URL expiry: 7 days = 604800 seconds.
     * This matches the iOS implementation and storage bucket policy.
     */
    private val signedUrlExpirySeconds = 604800L
    
    /**
     * Re-sign threshold: 6 hours = 21600 seconds.
     * If a cached URL expires in less than 6 hours, we re-sign it.
     */
    private val resignThresholdSeconds = 21600L
    
    /**
     * Cache of signed URLs with their expiry timestamps.
     * Key: "bucket/path", Value: SignedUrlCache entry
     */
    private val urlCache = mutableMapOf<String, SignedUrlCache>()
    
    /**
     * Mutex for thread-safe cache access.
     */
    private val cacheMutex = Mutex()
    
    /**
     * Get a display URL for the given storage path and bucket.
     * 
     * - For private buckets (spots, pending_images): returns a signed URL with 7-day expiry
     * - For public buckets (avatars): returns a public URL
     * - Caches signed URLs and re-signs near expiry
     * 
     * @param storagePath Path within the bucket (e.g., "user-id/spot-id/image.jpg")
     * @param bucket Bucket name (defaults to "spots")
     * @return URL string for image loading
     */
    suspend fun getImageUrl(storagePath: String, bucket: String = "spots"): String {
        // Public bucket: return public URL without signing
        if (bucket == "avatars") {
            return supabaseProvider.client.storage
                .from(bucket)
                .publicUrl(storagePath)
        }
        
        // Private bucket: check cache and sign if needed
        val cacheKey = "$bucket/$storagePath"
        val now = System.currentTimeMillis() / 1000 // Current time in seconds
        
        cacheMutex.withLock {
            val cached = urlCache[cacheKey]
            
            // Return cached URL if it's still valid (not near expiry)
            if (cached != null && (cached.expiresAtSeconds - now) > resignThresholdSeconds) {
                return cached.url
            }
            
            // Generate new signed URL
            val signedUrl = supabaseProvider.client.storage
                .from(bucket)
                .createSignedUrl(storagePath, signedUrlExpirySeconds)
            
            // Cache the new URL with its expiry timestamp
            val expiresAt = now + signedUrlExpirySeconds
            urlCache[cacheKey] = SignedUrlCache(
                url = signedUrl,
                expiresAtSeconds = expiresAt
            )
            
            return signedUrl
        }
    }
    
    /**
     * Clear all cached signed URLs.
     * Useful for testing or memory pressure scenarios.
     */
    suspend fun clearCache() {
        cacheMutex.withLock {
            urlCache.clear()
        }
    }
    
    /**
     * Get cache statistics for debugging.
     * Returns the number of cached URLs.
     */
    suspend fun getCacheSize(): Int {
        cacheMutex.withLock {
            return urlCache.size
        }
    }
}

/**
 * Internal cache entry for signed URLs.
 */
private data class SignedUrlCache(
    val url: String,
    val expiresAtSeconds: Long
)
