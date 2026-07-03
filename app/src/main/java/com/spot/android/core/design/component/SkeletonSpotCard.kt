package com.spot.android.core.design.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.spot.android.core.design.Dimensions
import com.spot.android.core.design.theme.SpotColors
import com.spot.android.core.design.theme.SpotTheme

/**
 * Skeleton loading placeholder for SpotCard.
 * 
 * Per PRD/06:
 * - Shows 3× skeleton cards during initial feed load
 * - Matches SpotCard layout with shimmer effect
 * - Header → media → interaction bar structure
 */
@Composable
fun SkeletonSpotCard(
    modifier: Modifier = Modifier
) {
    // Shimmer animation
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer"
    )
    
    val shimmerColor = SpotColors.Accent.copy(alpha = alpha)
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("skeletonSpotCard")
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.Padding.horizontal),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(shimmerColor)
            )
            
            Spacer(modifier = Modifier.width(Dimensions.Spacing.medium))
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Username
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(Dimensions.Radius.small))
                        .background(shimmerColor)
                )
                
                // Vibe + location
                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(Dimensions.Radius.small))
                        .background(shimmerColor)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))
        
        // Media placeholder (1:1 aspect ratio)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(shimmerColor)
        )
        
        Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))
        
        // Interaction bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimensions.Padding.horizontal),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.large)
        ) {
            // Like button
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(shimmerColor)
            )
            
            // Bookmark button
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(shimmerColor)
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // More button
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(shimmerColor)
            )
        }
        
        Spacer(modifier = Modifier.height(Dimensions.Spacing.XL))
    }
}

/**
 * Displays 3 skeleton cards for initial loading state.
 */
@Composable
fun SkeletonFeed(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("skeletonFeed")
    ) {
        repeat(3) {
            SkeletonSpotCard()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SkeletonSpotCardPreview() {
    SpotTheme {
        SkeletonSpotCard()
    }
}

@Preview(showBackground = true)
@Composable
private fun SkeletonFeedPreview() {
    SpotTheme {
        SkeletonFeed()
    }
}
