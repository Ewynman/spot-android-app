package com.spot.android.data.model.enums

/**
 * Target types for content reports.
 *
 * Reference: PRD/13-moderation-safety.md, PRD/03-data-model.md
 */
enum class ReportTargetType(val value: String) {
    SPOT("spot"),
    PROFILE("profile"),
    SPOT_IMAGE("spot_image"),
    COMMENT("comment"),
    COLLECTION("collection"),
    OTHER("other"),
}
