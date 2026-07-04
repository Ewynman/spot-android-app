package com.spot.android.feature.post

import com.spot.android.core.util.Constants

/**
 * Client-side vibe tag validation for custom Pro tags.
 *
 * Reference: PRD/02-design-system.md, PRD/08-post-flow.md
 */
object VibeTagValidator {

    sealed interface ValidationResult {
        data object Valid : ValidationResult
        data class Invalid(val message: String) : ValidationResult
    }

    fun validate(tag: String): ValidationResult {
        val trimmed = tag.trim()
        if (trimmed.length < Constants.VibeTagLimits.MIN_LENGTH) {
            return ValidationResult.Invalid("Please use at least 2 characters.")
        }
        if (trimmed.length > Constants.VibeTagLimits.MAX_LENGTH) {
            return ValidationResult.Invalid("Please keep it under 30 characters.")
        }
        val lower = trimmed.lowercase()
        if (BLOCKED_TAGS.any { lower.contains(it) }) {
            return ValidationResult.Invalid("That tag isn't allowed.")
        }
        return ValidationResult.Valid
    }

    private val BLOCKED_TAGS = setOf(
        "nude",
        "naked",
        "porn",
        "xxx",
        "nsfw",
        "sex",
        "fuck",
        "shit",
        "bitch",
        "asshole",
        "slut",
        "whore",
        "rape",
        "kill",
        "murder",
        "drug",
        "cocaine",
        "heroin",
        "meth",
    )
}
