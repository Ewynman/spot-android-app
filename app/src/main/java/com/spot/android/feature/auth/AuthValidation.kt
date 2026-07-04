package com.spot.android.feature.auth

/**
 * Client-side validation for auth forms.
 */
object AuthValidation {

    fun isValidEmail(email: String): Boolean {
        return email.contains('@') && email.contains('.') && email.length >= 5
    }

    fun isValidPassword(password: String): Boolean = password.length >= 8

    fun passwordsMatch(password: String, confirmPassword: String): Boolean =
        password == confirmPassword

    fun isValidUsername(username: String): Boolean {
        val trimmed = username.trim()
        return trimmed.length in 3..30 && trimmed.matches(USERNAME_PATTERN)
    }

    private val USERNAME_PATTERN = Regex("^[a-zA-Z0-9_]+$")
}
