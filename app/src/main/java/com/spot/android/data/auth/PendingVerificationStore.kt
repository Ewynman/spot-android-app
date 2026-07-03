package com.spot.android.data.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.pendingVerificationDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "pending_email_verification",
)

/**
 * Persists the email awaiting OTP confirmation across process death.
 */
@Singleton
class PendingVerificationStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore = context.pendingVerificationDataStore

    val pendingEmail: Flow<String?> = dataStore.data.map { prefs ->
        prefs[PENDING_EMAIL_KEY]
    }

    suspend fun setPendingEmail(email: String?) {
        dataStore.edit { prefs ->
            if (email == null) {
                prefs.remove(PENDING_EMAIL_KEY)
            } else {
                prefs[PENDING_EMAIL_KEY] = email
            }
        }
    }

    suspend fun getPendingEmail(): String? {
        return dataStore.data.first()[PENDING_EMAIL_KEY]
    }

    private companion object {
        val PENDING_EMAIL_KEY = stringPreferencesKey("pending_verification_email")
    }
}
