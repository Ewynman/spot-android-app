package com.spot.android.data.validator

/**
 * Username validation rules.
 * 
 * Rules:
 * - 3-20 characters
 * - Alphanumeric plus underscore and period
 * - Cannot start/end with period or underscore
 * - Cannot have consecutive periods or underscores
 * 
 * Reference: PRD/05-auth-onboarding.md
 */
object UsernameValidator {
    
    private const val MIN_LENGTH = 3
    private const val MAX_LENGTH = 20
    
    // Alphanumeric, underscore, period allowed
    private val VALID_CHARS_REGEX = Regex("^[a-zA-Z0-9_.]+$")
    
    // Cannot start or end with . or _
    private val START_END_REGEX = Regex("^[^_.].*[^_.]$")
    
    // No consecutive . or _
    private val CONSECUTIVE_REGEX = Regex("([._])\\1")
    
    /**
     * Validate username.
     * 
     * Returns null if valid, or an error message if invalid.
     */
    fun validate(username: String): String? {
        return when {
            username.length < MIN_LENGTH -> 
                "Username must be at least $MIN_LENGTH characters"
            username.length > MAX_LENGTH -> 
                "Username must be at most $MAX_LENGTH characters"
            !VALID_CHARS_REGEX.matches(username) -> 
                "Username can only contain letters, numbers, periods, and underscores"
            !START_END_REGEX.matches(username) -> 
                "Username cannot start or end with a period or underscore"
            CONSECUTIVE_REGEX.containsMatchIn(username) -> 
                "Username cannot have consecutive periods or underscores"
            else -> null
        }
    }
    
    /**
     * Check if username is valid.
     */
    fun isValid(username: String): Boolean {
        return validate(username) == null
    }
}
