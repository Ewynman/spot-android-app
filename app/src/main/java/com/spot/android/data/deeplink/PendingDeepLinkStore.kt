package com.spot.android.data.deeplink

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.deepLinkDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "spot_deep_links",
)

@Singleton
class PendingDeepLinkStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore = context.deepLinkDataStore
    private val pendingKey = stringPreferencesKey("pending_deep_link_uri")

    suspend fun setPending(uri: String) {
        dataStore.edit { it[pendingKey] = uri }
    }

    suspend fun consumePending(): String? {
        val value = dataStore.data.map { it[pendingKey] }.first()
        if (value != null) {
            dataStore.edit { it.remove(pendingKey) }
        }
        return value
    }

    suspend fun clear() {
        dataStore.edit { it.remove(pendingKey) }
    }
}
