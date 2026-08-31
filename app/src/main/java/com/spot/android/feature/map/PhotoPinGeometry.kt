package com.spot.android.feature.map

import com.spot.android.core.util.Constants
import kotlin.math.ceil
import kotlin.math.min

/**
 * Pure geometry helpers for [PhotoPinMarker].
 *
 * Kept UI-free so it can be unit-tested without an Android runtime; the
 * composable in [PhotoPinMarker] plugs these values into layout modifiers.
 */
object PhotoPinGeometry {

    /**
     * Anchor u-coordinate on the marker bitmap. Google Maps uses (0, 0) at top-left,
     * so the tail-tip sits at bottom-center of the frame.
     */
    const val ANCHOR_U = 0.5f

    /** Anchor v-coordinate on the marker bitmap: bottom edge. */
    const val ANCHOR_V = 1.0f

    /**
     * X-position of the tail tip inside a frame of [frameWidthDp] dp.
     */
    fun tailTipXDp(frameWidthDp: Int = Constants.PhotoPin.FRAME_WIDTH_DP): Float =
        frameWidthDp / 2f

    /**
     * Y-position of the tail tip inside a frame of [frameHeightDp] dp.
     * Always the bottom edge — the tip is what stays locked to the geographic anchor.
     */
    fun tailTipYDp(frameHeightDp: Int = Constants.PhotoPin.FRAME_HEIGHT_DP): Float =
        frameHeightDp.toFloat()

    /**
     * Circle diameter clamped so the border stays inside the frame.
     *
     * The circle sits at the top of the frame; the tail occupies the bottom
     * [tailHeightPx]. If the caller shrinks the frame (e.g. for a smaller
     * pixel target), the circle shrinks with it and never overflows.
     */
    fun clampedCircleDiameterPx(
        frameWidthPx: Int,
        frameHeightPx: Int,
        tailHeightPx: Int,
        borderPx: Int,
    ): Int {
        require(frameWidthPx >= 0) { "frameWidthPx must be non-negative" }
        require(frameHeightPx >= 0) { "frameHeightPx must be non-negative" }
        require(tailHeightPx >= 0) { "tailHeightPx must be non-negative" }
        require(borderPx >= 0) { "borderPx must be non-negative" }

        val availableHeight = (frameHeightPx - tailHeightPx).coerceAtLeast(0)
        val outer = min(frameWidthPx, availableHeight)
        // Border is drawn inside the circle, so the visible fill is (outer - 2 * borderPx).
        return (outer - borderPx * 2).coerceAtLeast(0)
    }

    /**
     * Target long-edge (in px) for the downsampled marker bitmap given the
     * display density. Bounded so xxhdpi and higher never exceed
     * [Constants.PhotoPin.IMAGE_TARGET_MAX_PX].
     */
    fun imageTargetPx(displayDensity: Float): Int {
        require(displayDensity > 0f) { "displayDensity must be positive" }
        val scaled = ceil(Constants.PhotoPin.IMAGE_TARGET_BASE_PX * displayDensity).toInt()
        return scaled.coerceAtMost(Constants.PhotoPin.IMAGE_TARGET_MAX_PX)
            .coerceAtLeast(Constants.PhotoPin.IMAGE_TARGET_BASE_PX)
    }

    /**
     * Power-of-two `inSampleSize` for [android.graphics.BitmapFactory.Options]
     * that downsamples a `sourceWidth × sourceHeight` bitmap to at most
     * `targetPx` on its shortest side.
     */
    fun computeInSampleSize(sourceWidth: Int, sourceHeight: Int, targetPx: Int): Int {
        if (sourceWidth <= 0 || sourceHeight <= 0 || targetPx <= 0) return 1
        val shortestSide = min(sourceWidth, sourceHeight)
        if (shortestSide <= targetPx) return 1
        var sample = 1
        while (shortestSide / (sample * 2) >= targetPx) {
            sample *= 2
        }
        return sample
    }
}
