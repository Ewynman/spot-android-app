package com.spot.android.data.map

import com.spot.android.core.media.ImageUrlSigner
import com.spot.android.core.util.Constants
import com.spot.android.data.dto.MapSpotRowDto
import com.spot.android.data.mapper.SpotMapper
import com.spot.android.data.model.Spot
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Hydrates map rows into UI [Spot] models with signed image URLs and session flags.
 */
@Singleton
class MapSpotHydrator @Inject constructor(
    private val imageUrlSigner: ImageUrlSigner,
) {

    suspend fun hydrate(
        row: MapSpotRowDto,
        likedSpotIds: Set<String>,
        bookmarkedSpotIds: Set<String>,
    ): Spot {
        val base = SpotMapper.fromMapSpotRow(row)
        val imageUrl = resolveImageUrl(row)

        return base.copy(
            imageURL = imageUrl,
            thumbnailURL = imageUrl,
            isLiked = likedSpotIds.contains(base.id),
            isSaved = bookmarkedSpotIds.contains(base.id),
        )
    }

    suspend fun hydrateAll(
        rows: List<MapSpotRowDto>,
        likedSpotIds: Set<String>,
        bookmarkedSpotIds: Set<String>,
    ): List<Spot> {
        return rows.map { row ->
            hydrate(
                row = row,
                likedSpotIds = likedSpotIds,
                bookmarkedSpotIds = bookmarkedSpotIds,
            )
        }
    }

    private suspend fun resolveImageUrl(row: MapSpotRowDto): String? {
        val storagePath = row.primary_storage_path
        if (!storagePath.isNullOrBlank()) {
            return imageUrlSigner.getImageUrl(
                storagePath = storagePath,
                bucket = Constants.StorageBuckets.SPOTS,
            )
        }
        return row.primary_public_url
    }
}
