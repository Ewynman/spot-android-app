package com.spot.android.di

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.util.DebugLogger
import com.spot.android.BuildConfig
import com.spot.android.core.media.ImageUrlSigner
import com.spot.android.core.media.SpotImageFetcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

/**
 * Hilt module for image loading dependencies.
 * 
 * Configures Coil ImageLoader with:
 * - Custom SpotImageFetcher for signed URL support
 * - Memory and disk caching
 * - OkHttp client
 * 
 * Reference: PRD/01-architecture-android.md, PRD/17-non-functional-testing.md
 */
@Module
@InstallIn(SingletonComponent::class)
object ImageModule {
    
    /**
     * Provides OkHttpClient for image fetching.
     * This can be customized with interceptors if needed.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .build()
    }
    
    /**
     * Provides configured Coil ImageLoader with Spot-specific fetcher.
     * 
     * Features:
     * - Custom fetcher for SpotImageRequest (signed URLs)
     * - Memory cache for fast repeated access
     * - Disk cache for offline support
     * - Debug logging in debug builds
     */
    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context,
        imageUrlSigner: ImageUrlSigner,
        okHttpClient: OkHttpClient
    ): ImageLoader {
        return ImageLoader.Builder(context)
            // Register custom fetcher for SpotImageRequest
            .components {
                add(SpotImageFetcher.Factory(imageUrlSigner, okHttpClient))
            }
            // Memory cache: ~25% of available app memory
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25)
                    .build()
            }
            // Disk cache: 100MB in app cache directory
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(100 * 1024 * 1024) // 100 MB
                    .build()
            }
            // OkHttp client for network requests
            .okHttpClient(okHttpClient)
            // Enable debug logging in debug builds
            .apply {
                if (BuildConfig.DEBUG) {
                    logger(DebugLogger())
                }
            }
            // Respect memory pressure by clearing caches
            .respectCacheHeaders(false) // We manage expiry ourselves
            .build()
    }
}
