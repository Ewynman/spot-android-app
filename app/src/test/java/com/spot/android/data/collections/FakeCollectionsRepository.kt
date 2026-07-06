package com.spot.android.data.collections

import com.spot.android.data.model.BookmarkCollection
import com.spot.android.data.model.Spot

/**
 * Fake implementation of CollectionsRepository for testing.
 */
class FakeCollectionsRepository : CollectionsRepository {
    var collections: MutableList<BookmarkCollection> = mutableListOf()
    var collectionSpotIds: Map<String, List<String>> = emptyMap()
    var collectionsContainingSpot: Map<String, List<String>> = emptyMap()
    var hydratedSpots: List<Spot> = emptyList()
    var createCollectionResult: Result<BookmarkCollection>? = null
    var updateCollectionNameResult: Result<Unit> = Result.success(Unit)
    var deleteCollectionResult: Result<Unit> = Result.success(Unit)
    var addSpotToCollectionResult: Result<Unit> = Result.success(Unit)
    var removeSpotFromCollectionResult: Result<Unit> = Result.success(Unit)

    var createdCollections: MutableList<BookmarkCollection> = mutableListOf()
    var deletedCollectionIds: MutableList<String> = mutableListOf()
    var reorderedCollectionIds: MutableList<List<String>> = mutableListOf()

    override suspend fun getCollections(): Result<List<BookmarkCollection>> {
        return Result.success(collections.sortedBy { it.sortIndex })
    }

    override suspend fun getCollection(collectionId: String): Result<BookmarkCollection> {
        return collections.find { it.id == collectionId }?.let { Result.success(it) }
            ?: Result.failure(IllegalStateException("Collection not found"))
    }

    override suspend fun createCollection(name: String): Result<BookmarkCollection> {
        return createCollectionResult ?: run {
            val newCollection = BookmarkCollection(
                id = "collection_${collections.size}",
                userId = "test_user",
                name = name,
                sortIndex = collections.size,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
            )
            collections.add(newCollection)
            createdCollections.add(newCollection)
            Result.success(newCollection)
        }
    }

    override suspend fun updateCollectionName(
        collectionId: String,
        name: String,
    ): Result<Unit> {
        if (updateCollectionNameResult.isSuccess) {
            collections = collections.map {
                if (it.id == collectionId) it.copy(name = name) else it
            }.toMutableList()
        }
        return updateCollectionNameResult
    }

    override suspend fun deleteCollection(collectionId: String): Result<Unit> {
        if (deleteCollectionResult.isSuccess) {
            collections.removeAll { it.id == collectionId }
            deletedCollectionIds.add(collectionId)
        }
        return deleteCollectionResult
    }

    override suspend fun reorderCollections(collectionIds: List<String>): Result<Unit> {
        reorderedCollectionIds.add(collectionIds)
        collections = collectionIds.mapNotNull { id ->
            collections.find { it.id == id }
        }.mapIndexed { index, collection ->
            collection.copy(sortIndex = index)
        }.toMutableList()
        return Result.success(Unit)
    }

    override suspend fun getCollectionSpotIds(
        collectionId: String,
        offset: Int,
        limit: Int,
    ): Result<List<String>> {
        val ids = collectionSpotIds[collectionId].orEmpty()
        return Result.success(ids.drop(offset).take(limit))
    }

    override suspend fun getCollectionSpotCount(collectionId: String): Result<Int> {
        val ids = collectionSpotIds[collectionId].orEmpty()
        return Result.success(ids.size)
    }

    override suspend fun addSpotToCollection(
        collectionId: String,
        spotId: String,
    ): Result<Unit> {
        if (addSpotToCollectionResult.isSuccess) {
            val current = collectionSpotIds[collectionId].orEmpty()
            collectionSpotIds = collectionSpotIds + (collectionId to (current + spotId))
        }
        return addSpotToCollectionResult
    }

    override suspend fun removeSpotFromCollection(
        collectionId: String,
        spotId: String,
    ): Result<Unit> {
        if (removeSpotFromCollectionResult.isSuccess) {
            val current = collectionSpotIds[collectionId].orEmpty()
            collectionSpotIds = collectionSpotIds + (collectionId to current.filterNot { it == spotId })
        }
        return removeSpotFromCollectionResult
    }

    override suspend fun moveSpotToCollection(
        spotId: String,
        fromCollectionId: String,
        toCollectionId: String,
    ): Result<Unit> {
        removeSpotFromCollection(fromCollectionId, spotId).getOrElse { return Result.failure(it) }
        addSpotToCollection(toCollectionId, spotId).getOrElse { return Result.failure(it) }
        return Result.success(Unit)
    }

    override suspend fun reorderSpotsInCollection(
        collectionId: String,
        spotIds: List<String>,
    ): Result<Unit> {
        collectionSpotIds = collectionSpotIds + (collectionId to spotIds)
        return Result.success(Unit)
    }

    override suspend fun getCollectionsContainingSpot(spotId: String): Result<List<String>> {
        return Result.success(collectionsContainingSpot[spotId].orEmpty())
    }

    override suspend fun hydrateSpots(spotIds: List<String>): Result<List<Spot>> {
        val byId = hydratedSpots.associateBy { it.id }
        return Result.success(spotIds.mapNotNull { byId[it] })
    }

    fun reset() {
        collections.clear()
        collectionSpotIds = emptyMap()
        collectionsContainingSpot = emptyMap()
        hydratedSpots = emptyList()
        createdCollections.clear()
        deletedCollectionIds.clear()
        reorderedCollectionIds.clear()
        createCollectionResult = null
        updateCollectionNameResult = Result.success(Unit)
        deleteCollectionResult = Result.success(Unit)
        addSpotToCollectionResult = Result.success(Unit)
        removeSpotFromCollectionResult = Result.success(Unit)
    }
}
