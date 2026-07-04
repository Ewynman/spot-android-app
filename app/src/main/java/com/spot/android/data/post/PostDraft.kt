package com.spot.android.data.post

import kotlinx.serialization.Serializable

/**
 * Local post draft models stored on-device only.
 *
 * Reference: PRD/08-post-flow.md
 */
enum class PostDraftStatus {
    AUTOSAVED,
    SAVED,
}

enum class PostDraftStep(val index: Int) {
    PHOTOS(1),
    LOCATION(2),
    VIBES(3),
    ;

    companion object {
        fun fromIndex(index: Int): PostDraftStep =
            entries.firstOrNull { it.index == index } ?: PHOTOS
    }
}

@Serializable
data class PostDraft(
    val id: String,
    val step: Int = PostDraftStep.PHOTOS.index,
    val status: String = PostDraftStatus.AUTOSAVED.name,
    val vibeTags: List<String> = emptyList(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val placeName: String? = null,
    val address: String? = null,
    val isCustomName: Boolean = false,
    val imageFileNames: List<String> = emptyList(),
    val updatedAt: Long = System.currentTimeMillis(),
) {
    val draftStatus: PostDraftStatus
        get() = PostDraftStatus.valueOf(status)

    val draftStep: PostDraftStep
        get() = PostDraftStep.fromIndex(step)
}

data class PostDraftSummary(
    val id: String,
    val status: PostDraftStatus,
    val previewImageFileName: String?,
    val placeName: String?,
    val vibeTags: List<String>,
    val updatedAt: Long,
    val step: PostDraftStep,
)

const val AUTOSAVE_DRAFT_ID = "autosave"
