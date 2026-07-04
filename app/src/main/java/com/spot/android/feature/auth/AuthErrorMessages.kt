package com.spot.android.feature.auth

import com.spot.android.data.auth.AuthError

/**
 * User-facing auth error messages.
 *
 * Reference: PRD/05-auth-onboarding.md
 */
object AuthErrorMessages {

    fun messageFor(error: AuthError): String = when (error) {
        AuthError.Network -> "Network error. Check your connection and try again."
        AuthError.InvalidCredentials -> "Incorrect email/username or password."
        AuthError.UsernameNotFound -> "Username not found."
        AuthError.EmailInUse -> "This email is already registered."
        AuthError.UsernameTaken -> "This username is taken."
        AuthError.InvalidOtp -> "Invalid code. Please try again."
        AuthError.OtpExpired -> "This code has expired. Request a new one."
        AuthError.EmailRequiredForReset -> "Enter your email address to reset your password."
        is AuthError.Generic -> error.message.ifBlank { "Something went wrong. Please try again." }
    }
}

/**
 * Masks an email for OTP display: first 2 chars + **** + @domain.
 */
fun maskEmail(email: String): String {
    val trimmed = email.trim()
    val atIndex = trimmed.indexOf('@')
    if (atIndex <= 0) return trimmed

    val localPart = trimmed.substring(0, atIndex)
    val domain = trimmed.substring(atIndex)
    val prefix = localPart.take(2)
    return "$prefix****$domain"
}
