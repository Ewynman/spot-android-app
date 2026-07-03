package com.spot.android.data.auth

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeAuthRepository : AuthRepository {

    private val _pendingEmail = MutableStateFlow<String?>(null)
    override val pendingVerificationEmail: Flow<String?> = _pendingEmail.asStateFlow()

    var signUpResult: Result<SignUpResult> = Result.success(
        SignUpResult(email = "test@example.com", requiresEmailVerification = true),
    )
    var signInResult: Result<Unit> = Result.success(Unit)
    var verifyOtpResult: Result<Unit> = Result.success(Unit)
    var usernameAvailable: Boolean = true
    var syncResult: Result<String> = Result.success("user-id")
    var emailVerified: Boolean = true
    var currentEmail: String? = "test@example.com"

    var signUpCalls = 0
    var signInCalls = 0
    var signOutCalls = 0
    var syncCalls = 0

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        username: String,
        isPrivate: Boolean,
    ): Result<SignUpResult> {
        signUpCalls++
        return signUpResult
    }

    override suspend fun signInWithEmailOrUsername(
        identifier: String,
        password: String,
    ): Result<Unit> {
        signInCalls++
        return signInResult
    }

    override suspend fun signInWithGoogle(): Result<Unit> = Result.success(Unit)

    override suspend fun handleOAuthCallback(url: String): Result<Unit> = Result.success(Unit)

    override suspend fun verifyEmailOtp(email: String, token: String): Result<Unit> {
        return verifyOtpResult
    }

    override suspend fun resendEmailOtp(email: String): Result<Unit> = Result.success(Unit)

    override suspend fun resetPassword(email: String): Result<Unit> = Result.success(Unit)

    override suspend fun signOut() {
        signOutCalls++
        _pendingEmail.value = null
    }

    override suspend fun isUsernameAvailable(username: String): Result<Boolean> {
        return Result.success(usernameAvailable)
    }

    override suspend fun syncCurrentUser(request: SyncCurrentUserRequest): Result<String> {
        syncCalls++
        return syncResult
    }

    override suspend fun clearPendingVerification() {
        _pendingEmail.value = null
    }

    override suspend fun getCurrentAuthEmail(): String? = currentEmail

    override suspend fun isCurrentEmailVerified(): Boolean = emailVerified

    fun setPendingEmail(email: String?) {
        _pendingEmail.value = email
    }
}
