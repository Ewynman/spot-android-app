package com.spot.android.data.dto

import kotlinx.serialization.Serializable

/**
 * DTO for `bookmark_collections` table reads.
 * 
 * Field names match snake_case Postgres columns exactly.
 * 
 * Reference: PRD/03-data-model.md (Pro bookmark folders)
 */
@Serializable
data class BookmarkCollectionDto(
    val id: String,
    val user_id: String,
    val name: String,
    val sort_index: Int,
    val created_at: String, // ISO-8601 timestamptz
    val updated_at: String,
)

/**
 * DTO for `bookmark_collection_spots` table reads.
 */
@Serializable
data class BookmarkCollectionSpotDto(
    val id: String,
    val collection_id: String,
    val spot_id: String,
    val sort_index: Int,
    val created_at: String, // ISO-8601 timestamptz
)

/**
 * Insert payload for creating a new collection.
 */
@Serializable
data class BookmarkCollectionInsertDto(
    val id: String,
    val user_id: String,
    val name: String,
    val sort_index: Int,
)

/**
 * Insert payload for adding a spot to a collection.
 */
@Serializable
data class BookmarkCollectionSpotInsertDto(
    val id: String,
    val collection_id: String,
    val spot_id: String,
    val sort_index: Int,
)
