package com.spot.android.core.media

import coil.ImageLoader
import coil.decode.DataSource
import coil.decode.ImageSource
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.request.Options
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.source

/**
 * Coil Fetcher for Spot images stored in Supabase Storage.
 * 
 * This fetcher intercepts [SpotImageRequest] objects and generates signed URLs
 * for private buckets or public URLs for public buckets, then fetches the image data.
 * 
 * Signed URLs are cached by [ImageUrlSigner] to avoid re-signing on every render.
 * 
 * Reference: PRD/01-architecture-android.md, PRD/04-backend-api.md
 */
class SpotImageFetcher(
    private val data: SpotImageRequest,
    private val options: Options,
    private val imageUrlSigner: ImageUrlSigner,
    private val httpClient: OkHttpClient
) : Fetcher {
    
    override suspend fun fetch(): FetchResult {
        // Get the appropriate URL (signed or public) from ImageUrlSigner
        val url = imageUrlSigner.getImageUrl(
            storagePath = data.storagePath,
            bucket = data.bucket
        )
        
        // Fetch the image data using OkHttp
        val request = Request.Builder()
            .url(url)
            .build()
        
        val response = httpClient.newCall(request).execute()
        
        if (!response.isSuccessful) {
            throw Exception("Failed to fetch image: HTTP ${response.code}")
        }
        
        val body = response.body ?: throw Exception("Empty response body")
        
        return SourceResult(
            source = ImageSource(
                source = body.source(),
                context = options.context
            ),
            mimeType = body.contentType()?.toString(),
            dataSource = DataSource.NETWORK
        )
    }
    
    /**
     * Factory for creating SpotImageFetcher instances.
     * This is registered with Coil's ImageLoader.
     */
    class Factory(
        private val imageUrlSigner: ImageUrlSigner,
        private val httpClient: OkHttpClient
    ) : Fetcher.Factory<SpotImageRequest> {
        
        override fun create(
            data: SpotImageRequest,
            options: Options,
            imageLoader: ImageLoader
        ): Fetcher {
            return SpotImageFetcher(
                data = data,
                options = options,
                imageUrlSigner = imageUrlSigner,
                httpClient = httpClient
            )
        }
    }
}
