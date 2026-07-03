package com.spot.android.data.model.enums

/**
 * Report reason values for content moderation.
 * 
 * Used with `submit_content_report` RPC.
 * 
 * Reference: PRD/13-moderation-safety.md
 */
enum class ReportReason(val value: String, val displayName: String) {
    SPAM("spam", "Spam"),
    HARASSMENT("harassment", "Harassment"),
    HATE_SPEECH("hate_speech", "Hate Speech"),
    VIOLENCE("violence", "Violence"),
    SELF_HARM("self_harm", "Self-Harm"),
    ADULT_CONTENT("adult_content", "Adult Content"),
    IMPERSONATION("impersonation", "Impersonation"),
    COPYRIGHT("copyright", "Copyright Violation"),
    FALSE_INFO("false_info", "False Information"),
    OTHER("other", "Other");
    
    companion object {
        fun fromValue(value: String?): ReportReason? {
            return entries.find { it.value == value }
        }
    }
}
