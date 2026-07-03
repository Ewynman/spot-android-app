package com.spot.android.data.validator

import com.spot.android.core.util.Constants

/**
 * Vibe tag validation rules.
 * 
 * Rules:
 * - 2-30 characters
 * - Trimmed
 * - Not just whitespace
 * 
 * Reference: PRD/03-data-model.md, Constants
 */
object VibeTagValidator {
    
    /**
     * Validate a vibe tag string.
     * 
     * Returns null if valid, or an error message if invalid.
     */
    fun validate(vibe: String): String? {
        val trimmed = vibe.trim()
        
        return when {
            trimmed.length < Constants.VibeTagLimits.MIN_LENGTH -> 
                "Vibe must be at least ${Constants.VibeTagLimits.MIN_LENGTH} characters"
            trimmed.length > Constants.VibeTagLimits.MAX_LENGTH -> 
                "Vibe must be at most ${Constants.VibeTagLimits.MAX_LENGTH} characters"
            trimmed.isBlank() -> 
                "Vibe cannot be empty"
            else -> null
        }
    }
    
    /**
     * Check if vibe tag is valid.
     */
    fun isValid(vibe: String): Boolean {
        return validate(vibe) == null
    }
    
    /**
     * Normalize a vibe tag for storage (lowercase).
     */
    fun normalize(vibe: String): String {
        return vibe.trim().lowercase()
    }
}
