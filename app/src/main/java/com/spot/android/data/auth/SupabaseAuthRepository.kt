package com.spot.android.data.auth

import com.spot.android.core.logging.LogCategory
import com.spot.android.core.logging.SpotLogger
import com.spot.android.core.supabase.SessionBridge
import com.spot.android.core.supabase.SupabaseClientProvider
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.gotrue.OtpType
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.parseSessionFromUrl
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

/**
 * Supabase-backed auth repository.
 *
 * Reference: PRD/04-backend-api.md, PRD/05-auth-onboarding.md
 */
@Singleton
class SupabaseAuthRepository @Inject constructor(
    private val supabaseProvider: SupabaseClientProvider,
    private val pendingVerificationStore: PendingVerificationStore,
    private val sessionBridge: SessionBridge,
    private val logger: SpotLogger,
) : AuthRepository {

    private val auth get() = supabaseProvider.auth
    private val postgrest get() = supabaseProvider.client.postgrest

    override val pendingVerificationEmail: Flow<String?> =
        pendingVerificationStore.pendingEmail

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        username: String,
        isPrivate: Boolean,
    ): Result<SignUpResult> = runAuthOperation {
        val available = isUsernameAvailable(username).getOrElse { throw it }
        if (!available) {
            throw AuthException(AuthError.UsernameTaken)
        }

        auth.signUpWith(Email) {
            this.email = email.trim()
            this.password = password
        }

        pendingVerificationStore.setPendingEmail(email.trim())

        val session = auth.currentSessionOrNull()
        if (session != null) {
            syncCurrentUser(
                SyncCurrentUserRequest(
                    username = username,
                    usernameLower = username.lowercase(),
                    email = email.trim(),
                    emailVerified = isCurrentEmailVerified(),
                    isPrivate = isPrivate,
                ),
            ).getOrElse { throw it }
            sessionBridge.refresh()
        }

        SignUpResult(
            email = email.trim(),
            requiresEmailVerification = session == null,
        )
    }

    override suspend fun signInWithEmailOrUsername(
        identifier: String,
        password: String,
    ): Result<Unit> = runAuthOperation {
        val email = resolveLoginIdentifier(identifier.trim())
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        syncAfterAuth().getOrElse { throw it }
    }

    override suspend fun signInWithGoogle(): Result<Unit> = runAuthOperation {
        auth.signInWith(Google)
    }

    override suspend fun handleOAuthCallback(url: String): Result<Unit> = runAuthOperation {
        val session = auth.parseSessionFromUrl(url)
        auth.importSession(session)
        syncAfterAuth().getOrElse { throw it }
    }

    override suspend fun verifyEmailOtp(email: String, token: String): Result<Unit> = runAuthOperation {
        auth.verifyEmailOtp(
            type = OtpType.Email.SIGNUP,
            email = email.trim(),
            token = token.trim(),
        )
        pendingVerificationStore.setPendingEmail(null)
        syncAfterAuth().getOrElse { throw it }
    }

    override suspend fun resendEmailOtp(email: String): Result<Unit> = runAuthOperation {
        auth.resendEmail(OtpType.Email.SIGNUP, email.trim())
    }

    override suspend fun resetPassword(email: String): Result<Unit> = runAuthOperation {
        if (!email.contains('@')) {
            throw AuthException(AuthError.EmailRequiredForReset)
        }
        auth.resetPasswordForEmail(email.trim())
    }

    override suspend fun signOut() {
        try {
            auth.signOut()
            pendingVerificationStore.setPendingEmail(null)
            sessionBridge.refresh()
        } catch (e: Exception) {
            logger.e(LogCategory.Auth, TAG, "Sign out failed", e)
        }
    }

    override suspend fun isUsernameAvailable(username: String): Result<Boolean> = runAuthOperation {
        postgrest.rpc(
            function = "is_username_available",
            parameters = IsUsernameAvailableRpcParams(p_username = username),
        ).decodeAs<Boolean>()
    }

    override suspend fun syncCurrentUser(request: SyncCurrentUserRequest): Result<String> = runAuthOperation {
        postgrest.rpc(
            function = "sync_current_user_v1",
            parameters = SyncCurrentUserRpcParams(
                p_username = request.username,
                p_username_lower = request.usernameLower,
                p_email = request.email,
                p_email_verified = request.emailVerified,
                p_is_private = request.isPrivate,
                p_locale = request.locale,
                p_last_active_at = Instant.now().toString(),
            ),
        ).decodeAs<String>()
    }

    override suspend fun clearPendingVerification() {
        pendingVerificationStore.setPendingEmail(null)
    }

    override suspend fun getCurrentAuthEmail(): String? {
        return auth.currentUserOrNull()?.email
    }

    override suspend fun isCurrentEmailVerified(): Boolean {
        return auth.currentUserOrNull()?.emailConfirmedAt != null
    }

    private suspend fun syncAfterAuth(): Result<Unit> {
        val user = auth.currentUserOrNull()
            ?: return Result.failure(AuthException(AuthError.Generic("No authenticated user")))

        val metadataUsername = user.userMetadata?.get("username")?.toString()?.trim().orEmpty()
        val username = metadataUsername.ifBlank { user.email?.substringBefore('@').orEmpty() }

        return syncCurrentUser(
            SyncCurrentUserRequest(
                username = username,
                usernameLower = username.lowercase(),
                email = user.email,
                emailVerified = user.emailConfirmedAt != null,
            ),
        ).map {
            sessionBridge.refresh()
        }
    }

    private suspend fun resolveLoginIdentifier(identifier: String): String {
        if (identifier.contains('@')) {
            return identifier
        }

        return try {
            postgrest.rpc(
                function = "resolve_login_email",
                parameters = ResolveLoginEmailRpcParams(p_username = identifier),
            ).decodeAs<String>()
        } catch (e: RestException) {
            if (e.message?.contains("not found", ignoreCase = true) == true) {
                throw AuthException(AuthError.UsernameNotFound)
            }
            throw e
        }
    }

    private inline fun <T> runAuthOperation(block: () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: AuthException) {
            Result.failure(e)
        } catch (e: RestException) {
            logger.e(LogCategory.Auth, TAG, "Auth RPC failed", e)
            Result.failure(AuthException(mapRestException(e)))
        } catch (e: Exception) {
            logger.e(LogCategory.Auth, TAG, "Auth operation failed", e)
            Result.failure(AuthException(mapGenericException(e)))
        }
    }

    private fun mapRestException(exception: RestException): AuthError {
        val message = exception.message.orEmpty()
        return when {
            message.contains("already registered", ignoreCase = true) ->
                AuthError.EmailInUse
            message.contains("invalid login credentials", ignoreCase = true) ||
                message.contains("invalid credentials", ignoreCase = true) ->
                AuthError.InvalidCredentials
            message.contains("otp", ignoreCase = true) &&
                message.contains("expired", ignoreCase = true) ->
                AuthError.OtpExpired
            message.contains("otp", ignoreCase = true) ||
                message.contains("token", ignoreCase = true) ->
                AuthError.InvalidOtp
            message.contains("network", ignoreCase = true) ||
                message.contains("timeout", ignoreCase = true) ||
                message.contains("connection", ignoreCase = true) ->
                AuthError.Network
            else -> AuthError.Generic(message)
        }
    }

    private fun mapGenericException(exception: Exception): AuthError {
        val message = exception.message.orEmpty()
        return when {
            message.contains("network", ignoreCase = true) ||
                message.contains("timeout", ignoreCase = true) ||
                message.contains("connection", ignoreCase = true) ||
                exception is java.net.UnknownHostException ||
                exception is java.net.SocketTimeoutException ->
                AuthError.Network
            message.contains("invalid login credentials", ignoreCase = true) ->
                AuthError.InvalidCredentials
            else -> AuthError.Generic(message.ifBlank { "Unknown auth error" })
        }
    }

    private companion object {
        const val TAG = "SupabaseAuthRepository"
    }
}

internal class AuthException(val authError: AuthError) : Exception()
