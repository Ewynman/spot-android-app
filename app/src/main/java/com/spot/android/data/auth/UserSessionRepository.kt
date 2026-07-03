package com.spot.android.data.auth

import kotlinx.coroutines.flow.Flow

/**
 * Loads the authenticated user's profile and engagement sets from Supabase.
 */
interface UserSessionRepository {

    suspend fun loadSessionSnapshot(userId: String): Result<UserSessionSnapshot>
}
