package com.spot.android.data.post

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.spot.android.core.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Downscales images to ≤1600px longest edge and JPEG-encodes before upload.
 *
 * Reference: PRD/08-post-flow.md, PRD/17-non-functional-testing.md
 */
@Singleton
class ImageProcessor @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun processFromUri(uri: Uri): Result<ProcessedImage> = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val original = BitmapFactory.decodeStream(input)
                    ?: throw IllegalArgumentException("Could not decode image")
                processBitmap(original)
            } ?: throw IllegalArgumentException("Could not open image")
        }
    }

    suspend fun processFromBytes(bytes: ByteArray, fileName: String): Result<ProcessedImage> =
        withContext(Dispatchers.IO) {
            runCatching {
                val original = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    ?: throw IllegalArgumentException("Could not decode image")
                processBitmap(original, fileName)
            }
        }

    private fun processBitmap(bitmap: Bitmap, fileName: String? = null): ProcessedImage {
        val maxDim = Constants.ImageProcessing.MAX_DIMENSION_PX
        val longest = max(bitmap.width, bitmap.height)
        val scaled = if (longest > maxDim) {
            val scale = maxDim.toFloat() / longest
            val targetWidth = (bitmap.width * scale).toInt().coerceAtLeast(1)
            val targetHeight = (bitmap.height * scale).toInt().coerceAtLeast(1)
            Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true).also {
                if (it != bitmap) bitmap.recycle()
            }
        } else {
            bitmap
        }

        val width = scaled.width
        val height = scaled.height
        val output = ByteArrayOutputStream()
        scaled.compress(
            Bitmap.CompressFormat.JPEG,
            (Constants.ImageProcessing.JPEG_QUALITY * 100).toInt(),
            output,
        )
        if (scaled != bitmap) scaled.recycle()

        return ProcessedImage(
            fileName = fileName ?: "${UUID.randomUUID()}.jpg",
            bytes = output.toByteArray(),
            width = width,
            height = height,
        )
    }
}
