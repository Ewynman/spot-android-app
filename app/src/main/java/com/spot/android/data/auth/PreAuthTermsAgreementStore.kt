package com.spot.android.data.auth

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

private val Context.preAuthTermsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "pre_auth_terms_agreement",
)

/**
 * Persists pre-auth terms agreement until it is recorded server-side after login.
 *
 * Reference: PRD/05-auth-onboarding.md (PreAuthTermsAgreementStore)
 */
@Singleton
class PreAuthTermsAgreementStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore = context.preAuthTermsDataStore

    suspend fun setAgreed(agreed: Boolean) {
        dataStore.edit { prefs ->
            prefs[AGREED_KEY] = agreed
        }
    }

    suspend fun hasAgreed(): Boolean {
        return dataStore.data.map { prefs -> prefs[AGREED_KEY] ?: false }.first()
    }

    suspend fun clear() {
        dataStore.edit { prefs ->
            prefs.remove(AGREED_KEY)
        }
    }

    private companion object {
        val AGREED_KEY = booleanPreferencesKey("pre_auth_terms_agreed")
    }
}
