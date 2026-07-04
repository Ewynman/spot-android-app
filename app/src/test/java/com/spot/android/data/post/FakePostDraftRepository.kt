package com.spot.android.data.post

class FakePostDraftRepository : PostDraftRepository {
    val drafts = mutableMapOf<String, PostDraft>()
    val images = mutableMapOf<String, List<ProcessedImage>>()

    override suspend fun listSummaries(): Result<List<PostDraftSummary>> =
        Result.success(
            drafts.values.map { draft ->
                PostDraftSummary(
                    id = draft.id,
                    status = draft.draftStatus,
                    previewImageFileName = draft.imageFileNames.firstOrNull(),
                    placeName = draft.placeName,
                    vibeTags = draft.vibeTags,
                    updatedAt = draft.updatedAt,
                    step = draft.draftStep,
                )
            },
        )

    override suspend fun loadDraft(id: String): Result<PostDraft?> =
        Result.success(drafts[id])

    override suspend fun saveDraft(draft: PostDraft, images: List<ProcessedImage>): Result<Unit> {
        drafts[draft.id] = draft
        this.images[draft.id] = images
        return Result.success(Unit)
    }

    override suspend fun deleteDraft(id: String): Result<Unit> {
        drafts.remove(id)
        images.remove(id)
        return Result.success(Unit)
    }

    override suspend fun loadDraftImages(draftId: String): Result<List<ProcessedImage>> =
        Result.success(images[draftId].orEmpty())

    override suspend fun autosave(draft: PostDraft, images: List<ProcessedImage>): Result<Unit> =
        saveDraft(draft, images)
}
