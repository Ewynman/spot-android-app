package com.spot.android.data.deeplink

import com.spot.android.core.media.ImageUrlSigner
import com.spot.android.core.supabase.SupabaseClientProvider
import com.spot.android.core.util.Constants
import com.spot.android.data.auth.UserSessionHolder
import com.spot.android.data.dto.SpotImageRowDto
import com.spot.android.data.dto.SpotRowDto
import com.spot.android.data.mapper.SpotMapper
import com.spot.android.data.model.Spot
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.Serializable

interface SpotDetailRepository {
    suspend fun fetchSpotById(spotId: String): Result<Spot>
}

@Singleton
class SupabaseSpotDetailRepository @Inject constructor(
    private val supabaseProvider: SupabaseClientProvider,
    private val imageUrlSigner: ImageUrlSigner,
    private val userSessionHolder: UserSessionHolder,
) : SpotDetailRepository {

    private val postgrest get() = supabaseProvider.client.postgrest

    override suspend fun fetchSpotById(spotId: String): Result<Spot> = runCatching {
        val rows = postgrest.from("spots")
            .select {
                filter { eq("id", spotId) }
            }
            .decodeList<SpotRowDto>()

        val row = rows.firstOrNull() ?: error("Spot not found")
        if (row.hidden_at != null) error("Spot hidden")

        val images = postgrest.from("spot_images")
            .select {
                filter { eq("spot_id", spotId) }
            }
            .decodeList<SpotImageRowDto>()
            .sortedBy { it.sort_index }

        val imageUrls = images.map { image ->
            imageUrlSigner.getImageUrl(
                storagePath = image.storage_path,
                bucket = image.storage_bucket.ifBlank { Constants.StorageBuckets.SPOTS },
            )
        }

        val authors = postgrest.from("users")
            .select {
                filter { eq("id", row.user_id) }
            }
            .decodeList<AuthorRow>()
        val author = authors.firstOrNull()

        SpotMapper.fromSpotRow(row, authorUsername = author?.username ?: "user").copy(
            userProfileImageURL = author?.profile_image_url,
            authorIsPro = author?.is_pro == true,
            imageURL = imageUrls.firstOrNull(),
            thumbnailURL = imageUrls.firstOrNull(),
            imageURLs = imageUrls.takeIf { it.isNotEmpty() },
            isLiked = userSessionHolder.likedSpots.value.contains(spotId),
            isSaved = userSessionHolder.bookmarkedSpots.value.contains(spotId),
        )
    }

    @Serializable
    private data class AuthorRow(
        val username: String? = null,
        val profile_image_url: String? = null,
        val is_pro: Boolean? = null,
    )
}
