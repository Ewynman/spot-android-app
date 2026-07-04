package com.spot.android.data.post

import com.spot.android.core.logging.LogCategory
import com.spot.android.core.logging.SpotLogger
import com.spot.android.core.supabase.SessionBridge
import com.spot.android.core.supabase.SupabaseClientProvider
import com.spot.android.core.util.Constants
import com.spot.android.data.dto.MediaAssetDto
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.storage.storage
import io.ktor.client.call.body
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Supabase-backed publish pipeline.
 */
@Singleton
class SupabaseSpotPublishRepository @Inject constructor(
    private val supabaseProvider: SupabaseClientProvider,
    private val sessionBridge: SessionBridge,
    private val logger: SpotLogger,
) : SpotPublishRepository {

    private val postgrest get() = supabaseProvider.client.postgrest
    private val storage get() = supabaseProvider.client.storage
    private val functions get() = supabaseProvider.client.functions

    override suspend fun publishSpot(request: PublishSpotRequest): Result<String> {
        val userId = sessionBridge.currentUserId
            ?: return Result.failure(PublishException.NotAuthenticated())

        return try {
            val mediaAssetIds = mutableListOf<String>()

            for (image in request.images) {
                val assetId = UUID.randomUUID().toString()
                val pendingPath = "$userId/$assetId.jpg"

                val asset = MediaAssetDto(
                    id = assetId,
                    owner_id = userId,
                    kind = "spot_image",
                    status = "pending",
                    pending_bucket = Constants.StorageBuckets.PENDING_IMAGES,
                    pending_path = pendingPath,
                    mime_type = "image/jpeg",
                    byte_size = image.byteSize,
                    width = image.width,
                    height = image.height,
                )

                postgrest.from("media_assets").insert(asset)

                storage.from(Constants.StorageBuckets.PENDING_IMAGES)
                    .upload(pendingPath, image.bytes, upsert = true)

                val moderation = moderateImage(assetId)
                if (!moderation.approved) {
                    val reason = moderation.reason.orEmpty()
                    return when {
                        reason.contains("image_policy_rejected") ->
                            Result.failure(PublishException.ImageRejected())
                        reason.contains("moderation_unavailable") ->
                            Result.failure(PublishException.ModerationUnavailable())
                        else ->
                            Result.failure(PublishException.ImageRejected())
                    }
                }

                mediaAssetIds.add(assetId)
            }

            val spotId = postgrest.rpc(
                function = "publish_spot_with_approved_media_assets_v1",
                parameters = PublishSpotRpcParams(
                    p_vibe_tag_ids = request.vibeTagIds,
                    p_latitude = request.latitude,
                    p_longitude = request.longitude,
                    p_location_name = request.locationName,
                    p_media_asset_ids = mediaAssetIds,
                ),
            ).decodeAs<String>()

            Result.success(spotId)
        } catch (e: PublishException) {
            Result.failure(e)
        } catch (e: Exception) {
            logger.e(LogCategory.Post, TAG, "Publish failed", e)
            Result.failure(e)
        }
    }

    private suspend fun moderateImage(mediaAssetId: String): ModerateImageResponse {
        val response = functions.invoke(
            function = "moderate-image",
            body = buildJsonObject {
                put("mediaAssetId", mediaAssetId)
            },
        )
        return response.body<ModerateImageResponse>()
    }

    private companion object {
        const val TAG = "SupabaseSpotPublishRepository"
    }
}

@Serializable
private data class PublishSpotRpcParams(
    @SerialName("p_vibe_tag_ids") val p_vibe_tag_ids: List<String>,
    @SerialName("p_latitude") val p_latitude: Double,
    @SerialName("p_longitude") val p_longitude: Double,
    @SerialName("p_location_name") val p_location_name: String,
    @SerialName("p_media_asset_ids") val p_media_asset_ids: List<String>,
)

@Serializable
data class ModerateImageResponse(
    val approved: Boolean,
    val reason: String? = null,
    val error: String? = null,
)
