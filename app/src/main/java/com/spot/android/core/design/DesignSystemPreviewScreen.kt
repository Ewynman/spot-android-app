package com.spot.android.core.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.spot.android.core.design.component.VibeChipDisplay
import com.spot.android.core.design.component.VibeChipSelectable
import com.spot.android.core.design.theme.SpotColors
import com.spot.android.core.design.theme.SpotTheme
import com.spot.android.core.util.Constants

/**
 * Design System Preview Screen
 * 
 * This screen demonstrates all design tokens from the Spot design system.
 * It serves as both documentation and validation that tokens are properly defined.
 * 
 * Reference: PRD/02-design-system.md
 */
@Composable
fun DesignSystemPreviewScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(Dimensions.Padding.horizontal)
            .padding(vertical = Dimensions.Padding.verticalLarge)
            .testTag("designSystem.preview"),
        verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.xl)
    ) {
        // Header
        Text(
            text = "SPOT",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "Design System Preview",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        
        HorizontalDivider(color = SpotColors.Primary)
        
        // Core Colors Section
        SectionHeader("Core Colors")
        ColorSwatch("Background (Cream)", SpotColors.Background)
        ColorSwatch("Primary (Deep Forest Green)", SpotColors.Primary)
        ColorSwatch("Button Text", SpotColors.ButtonText)
        ColorSwatch("Accent (Vibe Tag Surface)", SpotColors.Accent)
        
        HorizontalDivider(color = SpotColors.Primary.copy(alpha = 0.2f))
        
        // Map Colors Section
        SectionHeader("Map Colors")
        ColorSwatch("Map Marker Green", SpotColors.MapMarkerGreen)
        ColorSwatch("Map Marker Dot", SpotColors.MapMarkerDot)
        ColorSwatch("Map Marker Stroke", SpotColors.MapMarkerStroke)
        ColorSwatch("Map Density Fill", SpotColors.MapDensityFill)
        ColorSwatch("Map Filter Match", SpotColors.MapFilterMatch)
        ColorSwatch("Map Selected Glow", SpotColors.MapSelectedGlow)
        ColorSwatch("Pro Gold", SpotColors.ProGold)
        ColorSwatch("Map Avatar Ring", SpotColors.MapAvatarRing)
        ColorSwatch("Map Avatar Halo", SpotColors.MapAvatarHalo)
        
        HorizontalDivider(color = SpotColors.Primary.copy(alpha = 0.2f))
        
        // Welcome Screen Colors Section
        SectionHeader("Welcome Screen Colors")
        ColorSwatch("Welcome Glow", SpotColors.WelcomeGlow)
        ColorSwatch("Welcome Surface", SpotColors.WelcomeSurface)
        ColorSwatch("Welcome Muted Text", SpotColors.WelcomeMutedText)
        ColorSwatch("Welcome Line", SpotColors.WelcomeLine)
        ColorSwatch("Welcome Chip Fill", SpotColors.WelcomeChipFill)
        ColorSwatch("Welcome Card Shadow", SpotColors.WelcomeCardShadow)
        
        HorizontalDivider(color = SpotColors.Primary.copy(alpha = 0.2f))
        
        // Typography Section
        SectionHeader("Typography")
        TypographySample("Display Large (Wordmark)", MaterialTheme.typography.displayLarge)
        TypographySample("Title Large (Headers)", MaterialTheme.typography.titleLarge)
        TypographySample("Title Medium", MaterialTheme.typography.titleMedium)
        TypographySample("Body Large", MaterialTheme.typography.bodyLarge)
        TypographySample("Body Medium", MaterialTheme.typography.bodyMedium)
        TypographySample("Body Small", MaterialTheme.typography.bodySmall)
        TypographySample("Label Large", MaterialTheme.typography.labelLarge)
        TypographySample("Label Medium (Vibe Chips)", MaterialTheme.typography.labelMedium)
        TypographySample("Label Small", MaterialTheme.typography.labelSmall)
        
        HorizontalDivider(color = SpotColors.Primary.copy(alpha = 0.2f))
        
        // Spacing Section
        SectionHeader("Spacing")
        SpacingSample("Small", Dimensions.Spacing.small)
        SpacingSample("Medium", Dimensions.Spacing.medium)
        SpacingSample("Large", Dimensions.Spacing.large)
        SpacingSample("XL", Dimensions.Spacing.xl)
        
        HorizontalDivider(color = SpotColors.Primary.copy(alpha = 0.2f))
        
        // Padding Section
        SectionHeader("Padding")
        PaddingSample("Horizontal", Dimensions.Padding.horizontal)
        PaddingSample("Vertical Small", Dimensions.Padding.verticalSmall)
        PaddingSample("Vertical Medium", Dimensions.Padding.verticalMedium)
        PaddingSample("Vertical Large", Dimensions.Padding.verticalLarge)
        PaddingSample("Vertical XL", Dimensions.Padding.verticalXL)
        
        HorizontalDivider(color = SpotColors.Primary.copy(alpha = 0.2f))
        
        // Corner Radius Section
        SectionHeader("Corner Radius")
        RadiusSample("Small", Dimensions.Radius.small)
        RadiusSample("Medium", Dimensions.Radius.medium)
        RadiusSample("Large (Vibe Chips)", Dimensions.Radius.large)
        
        HorizontalDivider(color = SpotColors.Primary.copy(alpha = 0.2f))
        
        // Vibe Chips Section
        SectionHeader("Vibe Chips")
        VibeChipsDemo()
        
        HorizontalDivider(color = SpotColors.Primary.copy(alpha = 0.2f))
        
        // Constants Section
        SectionHeader("Constants")
        ConstantItem("Pagination - Default Page Size", Constants.Pagination.DEFAULT_PAGE_SIZE)
        ConstantItem("Pagination - Large Page Size", Constants.Pagination.LARGE_PAGE_SIZE)
        ConstantItem("Free Bookmark Cap", Constants.ContentLimits.FREE_BOOKMARK_CAP)
        ConstantItem("Free Max Images", Constants.PostLimits.FREE_MAX_IMAGES)
        ConstantItem("Pro Max Images", Constants.PostLimits.PRO_MAX_IMAGES)
        ConstantItem("Vibe Tag Min Length", Constants.VibeTagLimits.MIN_LENGTH)
        ConstantItem("Vibe Tag Max Length", Constants.VibeTagLimits.MAX_LENGTH)
        ConstantItem("OTP Length", Constants.Auth.OTP_LENGTH)
        
        // Bottom spacing
        Spacer(modifier = Modifier.height(Dimensions.Padding.verticalXL))
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun ColorSwatch(name: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.Padding.verticalSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(color, RoundedCornerShape(Dimensions.Radius.small))
                .border(
                    width = 1.dp,
                    color = SpotColors.Primary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(Dimensions.Radius.small)
                )
        )
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun TypographySample(name: String, style: TextStyle) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.Padding.verticalSmall)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Text(
            text = "The quick brown fox jumps over the lazy dog",
            style = style,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SpacingSample(name: String, size: Dp) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.Padding.verticalSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium)
    ) {
        Box(
            modifier = Modifier
                .width(size)
                .height(24.dp)
                .background(SpotColors.Primary)
        )
        Text(
            text = "$name: ${size.value}dp",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun PaddingSample(name: String, size: Dp) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.Padding.verticalSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(SpotColors.Accent)
                .padding(size)
                .background(SpotColors.Primary)
        )
        Text(
            text = "$name: ${size.value}dp",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun RadiusSample(name: String, radius: Dp) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.Padding.verticalSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(SpotColors.Primary, RoundedCornerShape(radius))
        )
        Text(
            text = "$name: ${radius.value}dp",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VibeChipsDemo() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium)
    ) {
        Text(
            text = "Display (Non-Interactive)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small),
            verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)
        ) {
            Constants.VibeTags.DEFAULT_TAGS.take(6).forEach { tag ->
                VibeChipDisplay(text = tag)
            }
        }
        
        Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))
        
        Text(
            text = "Selectable (Interactive)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small),
            verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)
        ) {
            VibeChipSelectable(text = "Chill Spot", selected = true, onSelectedChange = {})
            VibeChipSelectable(text = "Hidden Gem", selected = false, onSelectedChange = {})
            VibeChipSelectable(text = "Scenic View", selected = true, onSelectedChange = {})
        }
    }
}

@Composable
private fun ConstantItem(name: String, value: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.Padding.verticalSmall),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true, heightDp = 2000)
@Composable
private fun DesignSystemPreviewScreenPreview() {
    SpotTheme {
        Surface {
            DesignSystemPreviewScreen()
        }
    }
}
