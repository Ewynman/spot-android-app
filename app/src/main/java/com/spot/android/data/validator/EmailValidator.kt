package com.spot.android.data.validator

/**
 * Email validation using a standard email regex pattern.
 * 
 * Reference: PRD/05-auth-onboarding.md
 */
object EmailValidator {
    
    /**
     * Basic email validation regex.
     * Matches standard email format: local@domain.tld
     */
    private val EMAIL_REGEX = Regex(
        "[a-zA-Z0-9+._%\\-]{1,256}" +
        "@" +
        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
        "(" +
            "\\." +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
        ")+"
    )
    
    /**
     * Validate email format.
     * 
     * Returns null if valid, or an error message if invalid.
     */
    fun validate(email: String): String? {
        val trimmed = email.trim()
        
        return when {
            trimmed.isBlank() -> "Email cannot be empty"
            !EMAIL_REGEX.matches(trimmed) -> 
                "Please enter a valid email address"
            else -> null
        }
    }
    
    /**
     * Check if email is valid.
     */
    fun isValid(email: String): Boolean {
        return validate(email) == null
    }
    
    /**
     * Normalize email for storage (lowercase).
     */
    fun normalize(email: String): String {
        return email.trim().lowercase()
    }
}
