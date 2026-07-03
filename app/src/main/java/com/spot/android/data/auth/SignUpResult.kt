package com.spot.android.data.auth

/**
 * Outcome of email sign-up before OTP confirmation.
 */
data class SignUpResult(
    val email: String,
    val requiresEmailVerification: Boolean,
)
