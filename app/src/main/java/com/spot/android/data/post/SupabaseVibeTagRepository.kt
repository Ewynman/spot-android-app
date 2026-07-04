package com.spot.android.data.post

import com.spot.android.core.logging.LogCategory
import com.spot.android.core.logging.SpotLogger
import com.spot.android.core.supabase.SessionBridge
import com.spot.android.core.supabase.SupabaseClientProvider
import com.spot.android.data.dto.VibeRowDto
import io.github.jan.supabase.postgrest.postgrest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Supabase-backed vibe tag resolution.
 */
@Singleton
class SupabaseVibeTagRepository @Inject constructor(
    private val supabaseProvider: SupabaseClientProvider,
    private val sessionBridge: SessionBridge,
    private val logger: SpotLogger,
) : VibeTagRepository {

    private val postgrest get() = supabaseProvider.client.postgrest

    override suspend fun resolveTagIds(tagNames: List<String>): Result<List<String>> {
        val userId = sessionBridge.currentUserId
            ?: return Result.failure(IllegalStateException("Not authenticated"))

        if (tagNames.isEmpty()) return Result.success(emptyList())

        return try {
            val ids = tagNames.map { name ->
                val lower = name.trim().lowercase()
                val existing = postgrest.from("vibe_tags")
                    .select {
                        filter { eq("name_lower", lower) }
                    }
                    .decodeList<VibeRowDto>()
                    .firstOrNull()

                if (existing != null) {
                    existing.id
                } else {
                    val created = postgrest.from("vibe_tags")
                        .insert(
                            VibeTagInsertDto(
                                name = name.trim(),
                                name_lower = lower,
                            ),
                        ) {
                            select()
                        }
                        .decodeSingle<VibeRowDto>()
                    created.id
                }
            }
            Result.success(ids)
        } catch (e: Exception) {
            logger.e(LogCategory.Post, TAG, "Failed to resolve vibe tag ids", e)
            Result.failure(e)
        }
    }

    private companion object {
        const val TAG = "SupabaseVibeTagRepository"
    }
}

@kotlinx.serialization.Serializable
private data class VibeTagInsertDto(
    val name: String,
    val name_lower: String,
)
