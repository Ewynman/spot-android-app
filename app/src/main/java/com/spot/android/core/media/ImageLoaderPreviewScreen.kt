package com.spot.android.core.media

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.spot.android.core.design.theme.SpotTheme

/**
 * Preview/demo screen for testing the Coil image loader with signed URLs.
 * 
 * This screen demonstrates:
 * - Loading images from private buckets (spots, pending_images) via SpotImageRequest
 * - Loading images from public bucket (avatars) via SpotImageRequest
 * - Caching and re-use of signed URLs
 * 
 * Usage: Navigate to this screen in debug builds to verify image loading works.
 * Replace the example paths with real storage paths from your Supabase project.
 * 
 * Reference: Build order 1.4 done criteria - "renders a signed image"
 */
@Composable
fun ImageLoaderPreviewScreen(
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Image Loader Test",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Text(
                text = "This screen demonstrates the custom Coil image loader with signed URL support.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Example: Private bucket image (spots)
            ImageCard(
                title = "Private Bucket (spots)",
                description = "Signed URL with 7-day expiry, cached",
                imageRequest = SpotImageRequest(
                    storagePath = "example-user/example-spot/image.jpg",
                    bucket = "spots"
                )
            )
            
            // Example: Private bucket image (pending_images)
            ImageCard(
                title = "Private Bucket (pending_images)",
                description = "Signed URL for pre-moderation uploads",
                imageRequest = SpotImageRequest(
                    storagePath = "example-user/pending-asset.jpg",
                    bucket = "pending_images"
                )
            )
            
            // Example: Public bucket image (avatars)
            ImageCard(
                title = "Public Bucket (avatars)",
                description = "Public URL, no signing needed",
                imageRequest = SpotImageRequest(
                    storagePath = "example-user/avatar.jpg",
                    bucket = "avatars"
                )
            )
            
            Text(
                text = "Note: Replace the example paths with real storage paths to test actual image loading.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun ImageCard(
    title: String,
    description: String,
    imageRequest: SpotImageRequest,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "Path: ${imageRequest.storagePath}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            AsyncImage(
                model = imageRequest,
                contentDescription = title,
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.CenterHorizontally),
                contentScale = ContentScale.Crop,
                onError = {
                    // In a real app, handle errors appropriately
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ImageLoaderPreviewScreenPreview() {
    SpotTheme {
        ImageLoaderPreviewScreen()
    }
}
