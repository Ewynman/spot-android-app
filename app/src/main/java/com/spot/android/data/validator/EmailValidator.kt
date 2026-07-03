package com.spot.android.data.validator

import android.util.Patterns

/**
 * Email validation using Android's standard pattern.
 * 
 * Reference: PRD/05-auth-onboarding.md
 */
object EmailValidator {
    
    /**
     * Validate email format.
     * 
     * Returns null if valid, or an error message if invalid.
     */
    fun validate(email: String): String? {
        val trimmed = email.trim()
        
        return when {
            trimmed.isBlank() -> "Email cannot be empty"
            !Patterns.EMAIL_ADDRESS.matcher(trimmed).matches() -> 
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
