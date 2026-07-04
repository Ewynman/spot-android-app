package com.spot.android.data.post

class FakeVibeTagRepository : VibeTagRepository {
    var tagIds: List<String> = listOf("vibe-1")
    var shouldFail: Boolean = false

    override suspend fun resolveTagIds(tagNames: List<String>): Result<List<String>> {
        if (shouldFail) return Result.failure(IllegalStateException("resolve failed"))
        return Result.success(tagIds.take(tagNames.size).ifEmpty { listOf("vibe-1") })
    }
}
