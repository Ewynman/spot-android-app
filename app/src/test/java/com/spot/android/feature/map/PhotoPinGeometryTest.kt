package com.spot.android.feature.map

import com.spot.android.core.util.Constants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PhotoPinGeometryTest {

    @Test
    fun `anchor points to bottom-center so tail tip stays at coordinate`() {
        assertEquals(0.5f, PhotoPinGeometry.ANCHOR_U, 0.0001f)
        assertEquals(1.0f, PhotoPinGeometry.ANCHOR_V, 0.0001f)
    }

    @Test
    fun `tail tip sits at bottom-center of default frame`() {
        val frameWidth = Constants.PhotoPin.FRAME_WIDTH_DP
        val frameHeight = Constants.PhotoPin.FRAME_HEIGHT_DP

        assertEquals(frameWidth / 2f, PhotoPinGeometry.tailTipXDp(frameWidth), 0.0001f)
        assertEquals(frameHeight.toFloat(), PhotoPinGeometry.tailTipYDp(frameHeight), 0.0001f)
    }

    @Test
    fun `clamped diameter accounts for border and tail height`() {
        val diameter = PhotoPinGeometry.clampedCircleDiameterPx(
            frameWidthPx = 88,
            frameHeightPx = 112,
            tailHeightPx = 24,
            borderPx = 4,
        )
        // available height = 112 - 24 = 88; outer = min(88, 88) = 88; fill = 88 - 8 = 80
        assertEquals(80, diameter)
    }

    @Test
    fun `clamped diameter never overflows a short frame`() {
        val diameter = PhotoPinGeometry.clampedCircleDiameterPx(
            frameWidthPx = 20,
            frameHeightPx = 24,
            tailHeightPx = 20,
            borderPx = 2,
        )
        // available height = 4; outer = 4; fill = max(4 - 4, 0) = 0
        assertEquals(0, diameter)
    }

    @Test
    fun `image target scales with density and is capped at max`() {
        assertEquals(Constants.PhotoPin.IMAGE_TARGET_BASE_PX, PhotoPinGeometry.imageTargetPx(1.0f))
        assertEquals(132, PhotoPinGeometry.imageTargetPx(1.5f))
        assertEquals(Constants.PhotoPin.IMAGE_TARGET_MAX_PX, PhotoPinGeometry.imageTargetPx(2.0f))
        assertEquals(Constants.PhotoPin.IMAGE_TARGET_MAX_PX, PhotoPinGeometry.imageTargetPx(3.5f))
    }

    @Test
    fun `image target has a floor equal to base at fractional density below 1`() {
        // Density below 1 (ldpi ~ 0.75) should still request the base target so
        // we never downsample below the visible circle size at mdpi.
        assertEquals(
            Constants.PhotoPin.IMAGE_TARGET_BASE_PX,
            PhotoPinGeometry.imageTargetPx(0.75f),
        )
    }

    @Test
    fun `inSampleSize is 1 when source is already at or below target`() {
        assertEquals(1, PhotoPinGeometry.computeInSampleSize(88, 88, 88))
        assertEquals(1, PhotoPinGeometry.computeInSampleSize(50, 60, 88))
    }

    @Test
    fun `inSampleSize is a power of two that fits target`() {
        // 1600 / 176 ~ 9.09; largest power of 2 <= 9 is 8. But the algorithm
        // returns the largest power of 2 such that source / sample*2 >= target,
        // meaning sample = 8 → 1600 / 16 = 100 (< 176), so sample stays at 8.
        assertEquals(8, PhotoPinGeometry.computeInSampleSize(1600, 1600, 176))

        // 800 / 88: sample 8 → 100 (>= 88), sample 16 → 50 (< 88) so stop at 8.
        assertEquals(8, PhotoPinGeometry.computeInSampleSize(800, 800, 88))

        assertEquals(2, PhotoPinGeometry.computeInSampleSize(200, 200, 88))
    }

    @Test
    fun `inSampleSize handles zero and negative gracefully`() {
        assertEquals(1, PhotoPinGeometry.computeInSampleSize(0, 100, 88))
        assertEquals(1, PhotoPinGeometry.computeInSampleSize(100, -1, 88))
        assertEquals(1, PhotoPinGeometry.computeInSampleSize(100, 100, 0))
    }

    @Test
    fun `content description is safe when username or location is null`() {
        assertEquals(
            "Spot by , . Double-tap to preview.",
            photoPinContentDescription(username = null, locationName = null),
        )
        assertTrue(
            photoPinContentDescription(username = "eddy", locationName = "NYC")
                .contains("Spot by eddy, NYC."),
        )
    }
}
