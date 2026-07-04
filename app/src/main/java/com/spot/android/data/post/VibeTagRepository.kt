package com.spot.android.data.post

/**
 * Resolves vibe tag names to catalog ids, creating custom Pro tags when needed.
 *
 * Reference: PRD/08-post-flow.md, PRD/04-backend-api.md
 */
interface VibeTagRepository {
    suspend fun resolveTagIds(tagNames: List<String>): Result<List<String>>
}
