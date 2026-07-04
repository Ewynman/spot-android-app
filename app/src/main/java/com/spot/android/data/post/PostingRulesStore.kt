package com.spot.android.data.post

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.postingRulesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "posting_rules",
)

/**
 * Persists whether the user has accepted the posting rules sheet.
 *
 * Reference: PRD/08-post-flow.md
 */
@Singleton
class PostingRulesStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore = context.postingRulesDataStore

    suspend fun hasAccepted(): Boolean {
        return dataStore.data.map { prefs -> prefs[ACCEPTED_KEY] ?: false }.first()
    }

    suspend fun setAccepted(accepted: Boolean) {
        dataStore.edit { prefs ->
            prefs[ACCEPTED_KEY] = accepted
        }
    }

    private companion object {
        val ACCEPTED_KEY = booleanPreferencesKey("has_accepted_posting_rules")
    }
}
