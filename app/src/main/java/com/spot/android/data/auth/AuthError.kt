package com.spot.android.data.auth

/**
 * User-facing auth error categories mapped from Supabase / network failures.
 *
 * Reference: PRD/05-auth-onboarding.md
 */
sealed class AuthError {
    data object Network : AuthError()
    data object InvalidCredentials : AuthError()
    data object UsernameNotFound : AuthError()
    data object EmailInUse : AuthError()
    data object UsernameTaken : AuthError()
    data object InvalidOtp : AuthError()
    data object OtpExpired : AuthError()
    data object EmailRequiredForReset : AuthError()
    data class Generic(val message: String) : AuthError()
}
