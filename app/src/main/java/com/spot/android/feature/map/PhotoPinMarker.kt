package com.spot.android.feature.map

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.spot.android.core.design.theme.SpotColors
import com.spot.android.core.media.MapMarkerImageCache
import com.spot.android.core.media.MapMarkerImageLoadResult
import com.spot.android.core.util.Constants

/**
 * Circular photo-preview map marker (task 12).
 *
 * The marker frame is `44 × 56 dp`. The circle sits at the top and the
 * downward tail's tip is at `(frameWidth / 2, frameHeight)` — with Google
 * Maps' default anchor of (0.5, 1.0) this keeps the tip locked to the
 * geographic coordinate at every zoom level.
 *
 * Selection scales the frame by [Constants.PhotoPin.SELECTED_SCALE] with a
 * short spring; caller lifts z-index via [MarkerComposable]'s zIndex param.
 *
 * While the bitmap is loading, an intermediate placeholder is shown; on
 * failure, the caller (see [MapPinMarker]) falls back to the teardrop.
 */
@Composable
fun PhotoPinMarker(
    imageUrl: String?,
    isSelected: Boolean,
    imageCache: MapMarkerImageCache,
    onImageLoaded: (cacheHit: Boolean) -> Unit,
    onImageFailed: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "map.photoPin",
    username: String? = null,
    locationName: String? = null,
) {
    val density = LocalDensity.current
    val targetPx = remember(density.density) {
        PhotoPinGeometry.imageTargetPx(density.density)
    }

    var loaded by remember(imageUrl) { mutableStateOf<ImageBitmap?>(null) }
    var failed by remember(imageUrl) { mutableStateOf(false) }

    LaunchedEffect(imageUrl, targetPx) {
        if (imageUrl.isNullOrBlank()) {
            failed = true
            onImageFailed()
            return@LaunchedEffect
        }
        when (val result = imageCache.load(imageUrl, targetPx)) {
            is MapMarkerImageLoadResult.Success -> {
                loaded = result.bitmap.asImageBitmap()
                failed = false
                onImageLoaded(result.cacheHit)
            }
            MapMarkerImageLoadResult.Failure -> {
                loaded = null
                failed = true
                onImageFailed()
            }
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (isSelected) Constants.PhotoPin.SELECTED_SCALE else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "photoPinScale",
    )

    val frameWidth = Constants.PhotoPin.FRAME_WIDTH_DP.dp
    val frameHeight = Constants.PhotoPin.FRAME_HEIGHT_DP.dp
    val circleDiameter = Constants.PhotoPin.CIRCLE_DIAMETER_DP.dp
    val tailWidth = Constants.PhotoPin.TAIL_WIDTH_DP.dp
    val tailHeight = Constants.PhotoPin.TAIL_HEIGHT_DP.dp
    val borderDp = Constants.PhotoPin.BORDER_DP.dp

    val contentDesc = photoPinContentDescription(username, locationName)

    Box(
        modifier = modifier
            .width(frameWidth)
            .height(frameHeight)
            .scale(scale)
            .testTag(testTag),
    ) {
        // Tail: an inverted triangle whose tip is at the bottom-center of the frame.
        Canvas(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .width(tailWidth)
                .height(tailHeight),
        ) {
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width / 2f, size.height)
                close()
            }
            drawPath(path = path, color = SpotColors.MapMarkerStroke, style = Stroke(width = 2f))
            drawPath(path = path, color = SpotColors.Background, style = Fill)
        }

        // Circle photo (or placeholder / failure state) at the top of the frame.
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(circleDiameter)
                .shadow(if (isSelected) 4.dp else 2.dp, CircleShape)
                .background(SpotColors.Background, CircleShape)
                .border(borderDp, SpotColors.Background, CircleShape)
                .clip(CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            when {
                loaded != null -> Image(
                    bitmap = loaded!!,
                    contentDescription = contentDesc,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize(),
                )
                failed -> Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(SpotColors.MapMarkerGreen),
                )
                else -> Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(SpotColors.Accent),
                )
            }
        }
    }
}

/**
 * Content description string. Never surface literal `nil`; empty strings for
 * missing values are the iOS parity behaviour.
 */
internal fun photoPinContentDescription(
    username: String?,
    locationName: String?,
): String {
    val safeUsername = username?.trim().orEmpty()
    val safeLocation = locationName?.trim().orEmpty()
    return "Spot by $safeUsername, $safeLocation. Double-tap to preview."
}
