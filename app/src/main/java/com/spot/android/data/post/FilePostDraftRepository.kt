package com.spot.android.data.post

import android.content.Context
import com.spot.android.core.logging.LogCategory
import com.spot.android.core.logging.SpotLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * File-based local draft storage mirroring iOS Application Support/PostDrafts.
 *
 * Reference: PRD/08-post-flow.md
 */
@Singleton
class FilePostDraftRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val logger: SpotLogger,
) : PostDraftRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val draftsDir: File
        get() = File(context.filesDir, "post_drafts").also { it.mkdirs() }

    override suspend fun listSummaries(): Result<List<PostDraftSummary>> = withContext(Dispatchers.IO) {
        runCatching {
            indexFile().listFiles()
                ?.filter { it.extension == "json" && it.nameWithoutExtension != INDEX_FILE }
                ?.mapNotNull { file ->
                    runCatching { loadDraftFromFile(file) }.getOrNull()
                }
                ?.map { draft ->
                    PostDraftSummary(
                        id = draft.id,
                        status = draft.draftStatus,
                        previewImageFileName = draft.imageFileNames.firstOrNull(),
                        placeName = draft.placeName,
                        vibeTags = draft.vibeTags,
                        updatedAt = draft.updatedAt,
                        step = draft.draftStep,
                    )
                }
                ?.sortedByDescending { it.updatedAt }
                .orEmpty()
        }.onFailure {
            logger.e(LogCategory.Post, TAG, "Failed to list drafts", it)
        }
    }

    override suspend fun loadDraft(id: String): Result<PostDraft?> = withContext(Dispatchers.IO) {
        runCatching {
            val file = draftFile(id)
            if (!file.exists()) return@runCatching null
            loadDraftFromFile(file)
        }.onFailure {
            logger.e(LogCategory.Post, TAG, "Failed to load draft $id", it)
        }
    }

    override suspend fun saveDraft(draft: PostDraft, images: List<ProcessedImage>): Result<Unit> =
        persistDraft(draft, images)

    override suspend fun autosave(draft: PostDraft, images: List<ProcessedImage>): Result<Unit> =
        persistDraft(draft.copy(id = AUTOSAVE_DRAFT_ID, status = PostDraftStatus.AUTOSAVED.name), images)

    override suspend fun deleteDraft(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            draftImagesDir(id).deleteRecursively()
            draftFile(id).delete()
            Unit
        }.onFailure {
            logger.e(LogCategory.Post, TAG, "Failed to delete draft $id", it)
        }
    }

    override suspend fun loadDraftImages(draftId: String): Result<List<ProcessedImage>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val dir = draftImagesDir(draftId)
                if (!dir.exists()) return@runCatching emptyList()
                dir.listFiles()
                    ?.filter { it.extension == "jpg" }
                    ?.sortedBy { it.name }
                    ?.map { file ->
                        ProcessedImage(
                            fileName = file.name,
                            bytes = file.readBytes(),
                            width = 0,
                            height = 0,
                        )
                    }
                    .orEmpty()
            }.onFailure {
                logger.e(LogCategory.Post, TAG, "Failed to load draft images for $draftId", it)
            }
        }

    private suspend fun persistDraft(draft: PostDraft, images: List<ProcessedImage>): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val imageDir = draftImagesDir(draft.id)
                imageDir.deleteRecursively()
                imageDir.mkdirs()

                val fileNames = images.map { image ->
                    val file = File(imageDir, image.fileName)
                    file.writeBytes(image.bytes)
                    image.fileName
                }

                val persisted = draft.copy(
                    imageFileNames = fileNames,
                    updatedAt = System.currentTimeMillis(),
                )
                draftFile(draft.id).writeText(json.encodeToString(persisted))
            }.onFailure {
                logger.e(LogCategory.Post, TAG, "Failed to persist draft ${draft.id}", it)
            }
        }

    private fun loadDraftFromFile(file: File): PostDraft {
        return json.decodeFromString<PostDraft>(file.readText())
    }

    private fun indexFile(): File = draftsDir

    private fun draftFile(id: String): File = File(draftsDir, "$id.json")

    private fun draftImagesDir(id: String): File = File(draftsDir, id)

    private companion object {
        const val TAG = "FilePostDraftRepository"
        const val INDEX_FILE = "index"
    }
}
