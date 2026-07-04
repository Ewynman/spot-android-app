package com.spot.android.data.post

/**
 * Repository for local post drafts.
 *
 * Reference: PRD/08-post-flow.md
 */
interface PostDraftRepository {
    suspend fun listSummaries(): Result<List<PostDraftSummary>>
    suspend fun loadDraft(id: String): Result<PostDraft?>
    suspend fun saveDraft(draft: PostDraft, images: List<ProcessedImage>): Result<Unit>
    suspend fun deleteDraft(id: String): Result<Unit>
    suspend fun loadDraftImages(draftId: String): Result<List<ProcessedImage>>
    suspend fun autosave(
        draft: PostDraft,
        images: List<ProcessedImage>,
    ): Result<Unit>
}
