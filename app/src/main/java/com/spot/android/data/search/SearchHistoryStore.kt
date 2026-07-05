package com.spot.android.data.search

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.spot.android.core.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.searchHistoryDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "spot_search_history",
)

/**
 * Persists per-segment search history (max 20 items each).
 *
 * Reference: PRD/09-search.md
 */
@Singleton
class SearchHistoryStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore = context.searchHistoryDataStore
    private val json = Json { ignoreUnknownKeys = true }

    fun historyFlow(segment: SearchSegment): Flow<List<SearchHistoryItem>> {
        return dataStore.data.map { prefs ->
            decodeHistory(prefs[segment.historyKey()])
        }
    }

    suspend fun getHistory(segment: SearchSegment): List<SearchHistoryItem> {
        return historyFlow(segment).first()
    }

    suspend fun addItem(segment: SearchSegment, item: SearchHistoryItem) {
        dataStore.edit { prefs ->
            val current = decodeHistory(prefs[segment.historyKey()])
            val deduped = current.filterNot {
                it.type == item.type && it.query.equals(item.query, ignoreCase = true)
            }
            val updated = listOf(item) + deduped
            prefs[segment.historyKey()] = encodeHistory(
                updated.take(Constants.Search.HISTORY_MAX_PER_SEGMENT),
            )
        }
    }

    suspend fun clearSegment(segment: SearchSegment) {
        dataStore.edit { prefs ->
            prefs.remove(segment.historyKey())
        }
    }

    private fun decodeHistory(raw: String?): List<SearchHistoryItem> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            json.decodeFromString<List<SearchHistoryItemDto>>(raw).map { it.toDomain() }
        }.getOrDefault(emptyList())
    }

    private fun encodeHistory(items: List<SearchHistoryItem>): String {
        return json.encodeToString(items.map { SearchHistoryItemDto.fromDomain(it) })
    }

    private fun SearchSegment.historyKey(): Preferences.Key<String> = when (this) {
        SearchSegment.Users -> SearchHistoryKeys.USERS
        SearchSegment.Locations -> SearchHistoryKeys.LOCATIONS
        SearchSegment.Vibes -> SearchHistoryKeys.VIBES
    }
}

private object SearchHistoryKeys {
    val USERS = stringPreferencesKey("search_history_users_v1")
    val LOCATIONS = stringPreferencesKey("search_history_locations_v1")
    val VIBES = stringPreferencesKey("search_history_vibes_v1")
}

@Serializable
private data class SearchHistoryItemDto(
    val id: String,
    val type: String,
    val query: String,
    val displayText: String,
    val timestamp: Long,
    val vibeTagIds: List<String> = emptyList(),
) {
    fun toDomain(): SearchHistoryItem = SearchHistoryItem(
        id = id,
        type = SearchHistoryType.entries.first { it.rawValue == type },
        query = query,
        displayText = displayText,
        timestamp = timestamp,
        vibeTagIds = vibeTagIds,
    )

    companion object {
        fun fromDomain(item: SearchHistoryItem): SearchHistoryItemDto = SearchHistoryItemDto(
            id = item.id,
            type = item.type.rawValue,
            query = item.query,
            displayText = item.displayText,
            timestamp = item.timestamp,
            vibeTagIds = item.vibeTagIds,
        )
    }
}
