package com.spot.android.data.search

import com.spot.android.core.media.ImageUrlSigner
import com.spot.android.core.util.Constants
import com.spot.android.data.auth.UserSessionHolder
import com.spot.android.data.dto.SpotImageRowDto
import com.spot.android.data.dto.SpotRowDto
import com.spot.android.data.dto.SpotVibeJunctionRowDto
import com.spot.android.data.dto.UserBriefRowDto
import com.spot.android.data.dto.VibeRowDto
import com.spot.android.data.mapper.SpotMapper
import com.spot.android.data.mapper.UserMapper
import com.spot.android.data.model.Spot
import com.spot.android.data.model.VibeTag
import com.spot.android.core.supabase.SupabaseClientProvider
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Hydrates search grid spot ids into UI [Spot] models.
 *
 * Reference: PRD/04-backend-api.md (profile grid enrichment pattern)
 */
@Singleton
class SearchSpotHydrator @Inject constructor(
    private val supabaseProvider: SupabaseClientProvider,
    private val imageUrlSigner: ImageUrlSigner,
    private val userSessionHolder: UserSessionHolder,
) {
    private val postgrest get() = supabaseProvider.client.postgrest

    suspend fun hydrateByIds(spotIds: List<String>): List<Spot> {
        if (spotIds.isEmpty()) return emptyList()

        val blockedUsers = userSessionHolder.blockedUsers.value
        val likedSpotIds = userSessionHolder.likedSpots.value
        val bookmarkedSpotIds = userSessionHolder.bookmarkedSpots.value

        val spotRows = postgrest.from("spots")
            .select {
                filter {
                    isIn("id", spotIds)
                }
            }
            .decodeList<SpotRowDto>()
            .filterNot { blockedUsers.contains(it.user_id) }

        if (spotRows.isEmpty()) return emptyList()

        val rowById = spotRows.associateBy { it.id }
        val userIds = spotRows.map { it.user_id }.distinct()
        val resolvedSpotIds = spotRows.map { it.id }

        val users = postgrest.from("users_public")
            .select {
                filter {
                    isIn("id", userIds)
                }
            }
            .decodeList<UserBriefRowDto>()
            .associateBy { it.id }

        val images = postgrest.from("spot_images")
            .select {
                filter {
                    isIn("spot_id", resolvedSpotIds)
                }
            }
            .decodeList<SpotImageRowDto>()
            .groupBy { it.spot_id }

        val vibeJunctions = postgrest.from("spot_vibe_tags")
            .select {
                filter {
                    isIn("spot_id", resolvedSpotIds)
                }
            }
            .decodeList<SpotVibeJunctionRowDto>()
            .groupBy { it.spot_id }

        val vibeIds = vibeJunctions.values.flatten().map { it.vibe_tag_id }.distinct()
        val vibeRows = if (vibeIds.isEmpty()) {
            emptyMap()
        } else {
            postgrest.from("vibe_tags")
                .select {
                    filter {
                        isIn("id", vibeIds)
                    }
                }
                .decodeList<VibeRowDto>()
                .associateBy { it.id }
        }

        return spotIds.mapNotNull { spotId ->
            val row = rowById[spotId] ?: return@mapNotNull null
            val author = users[row.user_id] ?: return@mapNotNull null
            val spotImages = images[spotId].orEmpty().sortedBy { it.sort_index }
            val coverImage = spotImages.firstOrNull()
            val vibeLinks = vibeJunctions[spotId].orEmpty().sortedBy { it.sort_order }
            val primaryVibe = vibeLinks.firstOrNull()?.vibe_tag_id?.let { vibeRows[it] }

            val imageUrl = coverImage?.let { image ->
                if (image.storage_path.isNotBlank()) {
                    imageUrlSigner.getImageUrl(
                        storagePath = image.storage_path,
                        bucket = image.storage_bucket.ifBlank { Constants.StorageBuckets.SPOTS },
                    )
                } else {
                    image.public_url
                }
            }

            val base = SpotMapper.fromSpotRow(row, authorUsername = author.username).copy(
                userProfileImageURL = author.profile_image_url,
                imageURL = imageUrl,
                thumbnailURL = imageUrl,
                vibeTag = primaryVibe?.name,
                vibeTags = vibeLinks.mapNotNull { link ->
                    vibeRows[link.vibe_tag_id]?.let { SpotMapper.fromVibeRow(it) }
                },
                authorIsPrivate = author.is_private,
                authorIsPro = author.is_pro,
                isLiked = likedSpotIds.contains(spotId),
                isSaved = bookmarkedSpotIds.contains(spotId),
            )
            base
        }
    }
}
