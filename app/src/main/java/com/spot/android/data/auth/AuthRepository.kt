package com.spot.android.data.auth

import kotlinx.coroutines.flow.Flow

/**
 * Auth operations backed by Supabase GoTrue + RPCs.
 *
 * Reference: PRD/04-backend-api.md, PRD/05-auth-onboarding.md
 */
interface AuthRepository {

    val pendingVerificationEmail: Flow<String?>

    suspend fun signUpWithEmail(
        email: String,
        password: String,
        username: String,
        isPrivate: Boolean,
    ): Result<SignUpResult>

    suspend fun signInWithEmailOrUsername(
        identifier: String,
        password: String,
    ): Result<Unit>

    suspend fun signInWithGoogle(): Result<Unit>

    suspend fun handleOAuthCallback(url: String): Result<Unit>

    suspend fun verifyEmailOtp(email: String, token: String): Result<Unit>

    suspend fun resendEmailOtp(email: String): Result<Unit>

    suspend fun resetPassword(email: String): Result<Unit>

    suspend fun signOut()

    suspend fun isUsernameAvailable(username: String): Result<Boolean>

    suspend fun syncCurrentUser(request: SyncCurrentUserRequest): Result<String>

    suspend fun clearPendingVerification()

    suspend fun getCurrentAuthEmail(): String?

    suspend fun isCurrentEmailVerified(): Boolean
}
