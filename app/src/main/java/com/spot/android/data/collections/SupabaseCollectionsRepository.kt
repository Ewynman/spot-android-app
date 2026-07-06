package com.spot.android.data.collections

import com.spot.android.core.logging.LogCategory
import com.spot.android.core.logging.SpotLogger
import com.spot.android.core.supabase.SessionBridge
import com.spot.android.core.supabase.SupabaseClientProvider
import com.spot.android.data.dto.BookmarkCollectionDto
import com.spot.android.data.dto.BookmarkCollectionInsertDto
import com.spot.android.data.dto.BookmarkCollectionSpotDto
import com.spot.android.data.dto.BookmarkCollectionSpotInsertDto
import com.spot.android.data.model.BookmarkCollection
import com.spot.android.data.model.Spot
import com.spot.android.data.search.SearchSpotHydrator
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Supabase-backed collections repository.
 * 
 * Reference: PRD/10-profile-social.md, PRD/12-pro-subscription.md, PRD/04-backend-api.md
 */
@Singleton
class SupabaseCollectionsRepository @Inject constructor(
    private val supabaseProvider: SupabaseClientProvider,
    private val sessionBridge: SessionBridge,
    private val searchSpotHydrator: SearchSpotHydrator,
    private val logger: SpotLogger,
) : CollectionsRepository {

    private val postgrest get() = supabaseProvider.client.postgrest

    override suspend fun getCollections(): Result<List<BookmarkCollection>> {
        val userId = sessionBridge.currentUserId
            ?: return Result.failure(IllegalStateException("Not authenticated"))

        return try {
            val rows = postgrest.from("bookmark_collections")
                .select {
                    filter { eq("user_id", userId) }
                    order(column = "sort_index", order = Order.ASCENDING)
                    order(column = "created_at", order = Order.DESCENDING)
                }
                .decodeList<BookmarkCollectionDto>()

            val collections = rows.map { dto ->
                BookmarkCollection(
                    id = dto.id,
                    userId = dto.user_id,
                    name = dto.name,
                    sortIndex = dto.sort_index,
                    createdAt = BookmarkCollection.parseTimestamp(dto.created_at),
                    updatedAt = BookmarkCollection.parseTimestamp(dto.updated_at),
                )
            }
            Result.success(collections)
        } catch (e: Exception) {
            logger.e(LogCategory.Network, TAG, "Failed to load collections", e)
            Result.failure(e)
        }
    }

    override suspend fun getCollection(collectionId: String): Result<BookmarkCollection> {
        return try {
            val dto = postgrest.from("bookmark_collections")
                .select {
                    filter { eq("id", collectionId) }
                }
                .decodeSingle<BookmarkCollectionDto>()

            val collection = BookmarkCollection(
                id = dto.id,
                userId = dto.user_id,
                name = dto.name,
                sortIndex = dto.sort_index,
                createdAt = BookmarkCollection.parseTimestamp(dto.created_at),
                updatedAt = BookmarkCollection.parseTimestamp(dto.updated_at),
            )
            Result.success(collection)
        } catch (e: Exception) {
            logger.e(LogCategory.Network, TAG, "Failed to load collection $collectionId", e)
            Result.failure(e)
        }
    }

    override suspend fun createCollection(name: String): Result<BookmarkCollection> {
        val userId = sessionBridge.currentUserId
            ?: return Result.failure(IllegalStateException("Not authenticated"))

        return try {
            val maxSortIndex = postgrest.from("bookmark_collections")
                .select(columns = Columns.list("sort_index")) {
                    filter { eq("user_id", userId) }
                    order(column = "sort_index", order = Order.DESCENDING)
                    limit(1)
                }
                .decodeList<SortIndexDto>()
                .maxOfOrNull { it.sort_index } ?: -1

            val newId = UUID.randomUUID().toString()
            val insert = BookmarkCollectionInsertDto(
                id = newId,
                user_id = userId,
                name = name,
                sort_index = maxSortIndex + 1,
            )

            postgrest.from("bookmark_collections").insert(insert)

            getCollection(newId)
        } catch (e: Exception) {
            logger.e(LogCategory.Network, TAG, "Failed to create collection", e)
            Result.failure(e)
        }
    }

    override suspend fun updateCollectionName(collectionId: String, name: String): Result<Unit> {
        return try {
            postgrest.from("bookmark_collections").update(
                update = mapOf("name" to name),
            ) {
                filter { eq("id", collectionId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            logger.e(LogCategory.Network, TAG, "Failed to update collection name", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteCollection(collectionId: String): Result<Unit> {
        return try {
            postgrest.from("bookmark_collection_spots").delete {
                filter { eq("collection_id", collectionId) }
            }

            postgrest.from("bookmark_collections").delete {
                filter { eq("id", collectionId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            logger.e(LogCategory.Network, TAG, "Failed to delete collection", e)
            Result.failure(e)
        }
    }

    override suspend fun reorderCollections(collectionIds: List<String>): Result<Unit> {
        return try {
            collectionIds.forEachIndexed { index, id ->
                postgrest.from("bookmark_collections").update(
                    update = mapOf("sort_index" to index),
                ) {
                    filter { eq("id", id) }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            logger.e(LogCategory.Network, TAG, "Failed to reorder collections", e)
            Result.failure(e)
        }
    }

    override suspend fun getCollectionSpotIds(
        collectionId: String,
        offset: Int,
        limit: Int,
    ): Result<List<String>> {
        return try {
            val rows = postgrest.from("bookmark_collection_spots")
                .select(columns = Columns.list("spot_id")) {
                    filter { eq("collection_id", collectionId) }
                    order(column = "sort_index", order = Order.ASCENDING)
                    order(column = "created_at", order = Order.DESCENDING)
                    range(offset.toLong(), (offset + limit - 1).toLong())
                }
                .decodeList<SpotIdDto>()
            Result.success(rows.map { it.spot_id })
        } catch (e: Exception) {
            logger.e(LogCategory.Network, TAG, "Failed to load collection spot ids", e)
            Result.failure(e)
        }
    }

    override suspend fun getCollectionSpotCount(collectionId: String): Result<Int> {
        return try {
            val rows = postgrest.from("bookmark_collection_spots")
                .select(columns = Columns.list("id")) {
                    filter { eq("collection_id", collectionId) }
                    count()
                }
                .decodeList<IdDto>()
            Result.success(rows.size)
        } catch (e: Exception) {
            logger.e(LogCategory.Network, TAG, "Failed to count collection spots", e)
            Result.failure(e)
        }
    }

    override suspend fun addSpotToCollection(collectionId: String, spotId: String): Result<Unit> {
        return try {
            val maxSortIndex = postgrest.from("bookmark_collection_spots")
                .select(columns = Columns.list("sort_index")) {
                    filter { eq("collection_id", collectionId) }
                    order(column = "sort_index", order = Order.DESCENDING)
                    limit(1)
                }
                .decodeList<SortIndexDto>()
                .maxOfOrNull { it.sort_index } ?: -1

            val insert = BookmarkCollectionSpotInsertDto(
                id = UUID.randomUUID().toString(),
                collection_id = collectionId,
                spot_id = spotId,
                sort_index = maxSortIndex + 1,
            )

            postgrest.from("bookmark_collection_spots").insert(insert)
            Result.success(Unit)
        } catch (e: Exception) {
            logger.e(LogCategory.Network, TAG, "Failed to add spot to collection", e)
            Result.failure(e)
        }
    }

    override suspend fun removeSpotFromCollection(
        collectionId: String,
        spotId: String,
    ): Result<Unit> {
        return try {
            postgrest.from("bookmark_collection_spots").delete {
                filter {
                    eq("collection_id", collectionId)
                    eq("spot_id", spotId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            logger.e(LogCategory.Network, TAG, "Failed to remove spot from collection", e)
            Result.failure(e)
        }
    }

    override suspend fun moveSpotToCollection(
        spotId: String,
        fromCollectionId: String,
        toCollectionId: String,
    ): Result<Unit> {
        return try {
            removeSpotFromCollection(fromCollectionId, spotId).getOrThrow()
            addSpotToCollection(toCollectionId, spotId).getOrThrow()
            Result.success(Unit)
        } catch (e: Exception) {
            logger.e(LogCategory.Network, TAG, "Failed to move spot between collections", e)
            Result.failure(e)
        }
    }

    override suspend fun reorderSpotsInCollection(
        collectionId: String,
        spotIds: List<String>,
    ): Result<Unit> {
        return try {
            spotIds.forEachIndexed { index, spotId ->
                postgrest.from("bookmark_collection_spots").update(
                    update = mapOf("sort_index" to index),
                ) {
                    filter {
                        eq("collection_id", collectionId)
                        eq("spot_id", spotId)
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            logger.e(LogCategory.Network, TAG, "Failed to reorder spots in collection", e)
            Result.failure(e)
        }
    }

    override suspend fun getCollectionsContainingSpot(spotId: String): Result<List<String>> {
        return try {
            val rows = postgrest.from("bookmark_collection_spots")
                .select(columns = Columns.list("collection_id")) {
                    filter { eq("spot_id", spotId) }
                }
                .decodeList<CollectionIdDto>()
            Result.success(rows.map { it.collection_id })
        } catch (e: Exception) {
            logger.e(LogCategory.Network, TAG, "Failed to get collections containing spot", e)
            Result.failure(e)
        }
    }

    override suspend fun hydrateSpots(spotIds: List<String>): Result<List<Spot>> {
        return try {
            Result.success(searchSpotHydrator.hydrateByIds(spotIds))
        } catch (e: Exception) {
            logger.e(LogCategory.Network, TAG, "Failed to hydrate collection spots", e)
            Result.failure(e)
        }
    }

    private companion object {
        const val TAG = "SupabaseCollectionsRepository"
    }
}

@Serializable
private data class SortIndexDto(
    val sort_index: Int,
)

@Serializable
private data class SpotIdDto(
    val spot_id: String,
)

@Serializable
private data class IdDto(
    val id: String,
)

@Serializable
private data class CollectionIdDto(
    val collection_id: String,
)
