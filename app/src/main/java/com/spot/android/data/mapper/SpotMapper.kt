package com.spot.android.data.mapper

import com.spot.android.data.dto.HomeFeedRowDto
import com.spot.android.data.dto.MapSpotRowDto
import com.spot.android.data.dto.SpotImageRowDto
import com.spot.android.data.dto.SpotRowDto
import com.spot.android.data.dto.VibeRowDto
import com.spot.android.data.model.Spot
import com.spot.android.data.model.SpotImage
import com.spot.android.data.model.VibeTag
import com.spot.android.data.model.enums.ImageOrientation

/**
 * Mappers to convert DTOs to domain models.
 * 
 * Reference: PRD/04-backend-api.md, PRD/06-home-feed.md
 */
object SpotMapper {
    
    /**
     * Map HomeFeedRowDto to Spot domain model.
     */
    fun fromHomeFeedRow(dto: HomeFeedRowDto): Spot {
        return Spot(
            id = dto.spot_id,
            userId = dto.user_id,
            username = dto.author_username,
            userProfileImageURL = dto.author_profile_image_url,
            caption = dto.caption,
            latitude = dto.latitude,
            longitude = dto.longitude,
            locationName = dto.location_name,
            likes = dto.likes_count,
            saves = dto.saves_count,
            createdAt = Spot.parseTimestamp(dto.created_at) ?: 0L,
            updatedAt = Spot.parseTimestamp(dto.updated_at),
            imageURL = dto.primary_public_url, // May need signing
            thumbnailURL = dto.primary_public_url,
            mediaDisplayAspectRatio = dto.media_display_aspect_ratio ?: 1.0,
            mediaCount = dto.media_count ?: 1,
            vibeTag = dto.vibe_name,
            authorIsPrivate = dto.author_is_private,
            rankPosition = dto.rank_position,
            rankingScore = dto.ranking_score,
            seenBefore = dto.seen_before,
            lastSeenAt = Spot.parseTimestamp(dto.last_seen_at),
            sourceBucket = dto.source_bucket
        )
    }
    
    /**
     * Map MapSpotRowDto to Spot domain model.
     */
    fun fromMapSpotRow(dto: MapSpotRowDto): Spot {
        return Spot(
            id = dto.spot_id,
            userId = dto.user_id,
            username = dto.author_username,
            userProfileImageURL = dto.author_profile_image_url,
            caption = dto.caption,
            latitude = dto.latitude,
            longitude = dto.longitude,
            locationName = dto.location_name,
            likes = 0L, // Not included in map row
            saves = 0L,
            createdAt = Spot.parseTimestamp(dto.created_at) ?: 0L,
            updatedAt = null,
            imageURL = dto.primary_public_url,
            thumbnailURL = dto.primary_public_url,
            mediaDisplayAspectRatio = 1.0, // Default for map
            mediaCount = 1,
            vibeTag = dto.vibe_name,
            authorIsPrivate = false, // Map doesn't expose private spots
            distanceMeters = dto.distance_meters
        )
    }
    
    /**
     * Map SpotRowDto to partial Spot (needs enrichment with author + images).
     */
    fun fromSpotRow(dto: SpotRowDto, authorUsername: String): Spot {
        return Spot(
            id = dto.id,
            userId = dto.user_id,
            username = authorUsername,
            userProfileImageURL = null, // Enrich separately
            caption = dto.caption,
            latitude = dto.latitude,
            longitude = dto.longitude,
            locationName = dto.location_name,
            likes = dto.likes_count,
            saves = dto.saves_count,
            createdAt = Spot.parseTimestamp(dto.created_at) ?: 0L,
            updatedAt = Spot.parseTimestamp(dto.updated_at),
            imageURL = null, // Enrich from spot_images
            thumbnailURL = null,
            mediaDisplayAspectRatio = dto.media_display_aspect_ratio,
            mediaCount = dto.media_count,
            vibeTag = null, // Enrich from vibe join
            authorIsPrivate = dto.author_is_private_snapshot
        )
    }
    
    /**
     * Map SpotImageRowDto to SpotImage domain model.
     */
    fun fromSpotImageRow(dto: SpotImageRowDto): SpotImage {
        return SpotImage(
            id = dto.id,
            spotId = dto.spot_id,
            storagePath = dto.storage_path,
            publicUrl = dto.public_url,
            sortIndex = dto.sort_index,
            storageBucket = dto.storage_bucket,
            width = dto.width,
            height = dto.height,
            aspectRatio = dto.aspect_ratio,
            displayAspectRatio = dto.display_aspect_ratio,
            orientation = ImageOrientation.fromValue(dto.orientation) 
                ?: ImageOrientation.SQUARE
        )
    }
    
    /**
     * Map VibeRowDto to VibeTag domain model.
     */
    fun fromVibeRow(dto: VibeRowDto): VibeTag {
        return VibeTag(
            id = dto.id,
            name = dto.name,
            nameLower = dto.name_lower
        )
    }
}
