package com.spot.android.data.collections

import com.spot.android.data.model.BookmarkCollection
import com.spot.android.data.model.Spot

/**
 * Repository for Pro bookmark collections.
 *
 * Manages CRUD operations for `bookmark_collections` and `bookmark_collection_spots`.
 * 
 * Reference: PRD/10-profile-social.md, PRD/12-pro-subscription.md, PRD/04-backend-api.md
 */
interface CollectionsRepository {
    /**
     * Get all collections for the current user, ordered by sort_index.
     */
    suspend fun getCollections(): Result<List<BookmarkCollection>>

    /**
     * Get a single collection by ID.
     */
    suspend fun getCollection(collectionId: String): Result<BookmarkCollection>

    /**
     * Create a new collection.
     * 
     * @param name Collection name
     * @return The created collection
     */
    suspend fun createCollection(name: String): Result<BookmarkCollection>

    /**
     * Update collection name.
     */
    suspend fun updateCollectionName(collectionId: String, name: String): Result<Unit>

    /**
     * Delete a collection and all its spot memberships.
     */
    suspend fun deleteCollection(collectionId: String): Result<Unit>

    /**
     * Reorder collections by updating sort indices.
     * 
     * @param collectionIds List of collection IDs in the desired order
     */
    suspend fun reorderCollections(collectionIds: List<String>): Result<Unit>

    /**
     * Get spot IDs in a collection, ordered by sort_index.
     * 
     * @param collectionId Collection ID
     * @param offset Pagination offset
     * @param limit Page size
     */
    suspend fun getCollectionSpotIds(
        collectionId: String,
        offset: Int,
        limit: Int,
    ): Result<List<String>>

    /**
     * Get count of spots in a collection.
     */
    suspend fun getCollectionSpotCount(collectionId: String): Result<Int>

    /**
     * Add a spot to a collection.
     * 
     * @param collectionId Collection ID
     * @param spotId Spot ID
     */
    suspend fun addSpotToCollection(collectionId: String, spotId: String): Result<Unit>

    /**
     * Remove a spot from a collection.
     */
    suspend fun removeSpotFromCollection(collectionId: String, spotId: String): Result<Unit>

    /**
     * Move a spot from one collection to another.
     */
    suspend fun moveSpotToCollection(
        spotId: String,
        fromCollectionId: String,
        toCollectionId: String,
    ): Result<Unit>

    /**
     * Reorder spots within a collection.
     * 
     * @param collectionId Collection ID
     * @param spotIds List of spot IDs in the desired order
     */
    suspend fun reorderSpotsInCollection(
        collectionId: String,
        spotIds: List<String>,
    ): Result<Unit>

    /**
     * Get all collection IDs that contain a given spot.
     */
    suspend fun getCollectionsContainingSpot(spotId: String): Result<List<String>>

    /**
     * Hydrate spot details for a list of spot IDs (shared with ProfileRepository).
     */
    suspend fun hydrateSpots(spotIds: List<String>): Result<List<Spot>>
}
