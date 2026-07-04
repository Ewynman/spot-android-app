package com.spot.android.data.post

import com.spot.android.core.logging.LogCategory
import com.spot.android.core.logging.SpotLogger
import com.spot.android.core.util.Constants
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Background publish coordinator with queued operations and 90s timeout.
 *
 * Reference: PRD/08-post-flow.md
 */
@Singleton
class SpotPublishCoordinator @Inject constructor(
    private val vibeTagRepository: VibeTagRepository,
    private val spotPublishRepository: SpotPublishRepository,
    private val postDraftRepository: PostDraftRepository,
    private val spotPostedBus: SpotPostedBus,
    private val logger: SpotLogger,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _state = MutableStateFlow<PublishCoordinatorState>(PublishCoordinatorState.Idle)
    val state: StateFlow<PublishCoordinatorState> = _state.asStateFlow()

    fun enqueue(job: PublishJob) {
        scope.launch {
            processJob(job)
        }
    }

    private suspend fun processJob(job: PublishJob) {
        _state.value = PublishCoordinatorState.Uploading

        val result = withTimeoutOrNull(Constants.Timeouts.PUBLISH_TIMEOUT_SECONDS * 1000L) {
            runPublish(job)
        }

        when {
            result == null -> {
                logger.w(LogCategory.Post, TAG, "Publish timed out")
                _state.value = PublishCoordinatorState.Failed(
                    "Publishing timed out. Your draft was saved — try again.",
                )
            }
            result.isSuccess -> {
                val spotId = result.getOrThrow()
                postDraftRepository.deleteDraft(AUTOSAVE_DRAFT_ID)
                spotPostedBus.emit(spotId)
                _state.value = PublishCoordinatorState.Success(spotId)
                logger.i(LogCategory.Post, TAG, "Spot published successfully")
            }
            else -> {
                val error = result.exceptionOrNull()
                val message = when (error) {
                    is PublishException.ImageRejected -> error.message ?: "Image rejected"
                    is PublishException.ModerationUnavailable ->
                        error.message ?: "Moderation unavailable"
                    else -> "Couldn't publish your spot. Your draft was saved."
                }
                logger.w(LogCategory.Post, TAG, "Publish failed: $message", error)
                _state.value = PublishCoordinatorState.Failed(message)
            }
        }
    }

    private suspend fun runPublish(job: PublishJob): Result<String> {
        _state.value = PublishCoordinatorState.Moderating

        val vibeIdsResult = vibeTagRepository.resolveTagIds(job.vibeTags)
        if (vibeIdsResult.isFailure) return Result.failure(vibeIdsResult.exceptionOrNull()!!)
        val vibeIds = vibeIdsResult.getOrThrow()

        _state.value = PublishCoordinatorState.Publishing

        return spotPublishRepository.publishSpot(
            PublishSpotRequest(
                images = job.images,
                vibeTagIds = vibeIds,
                latitude = job.latitude,
                longitude = job.longitude,
                locationName = job.locationName,
            ),
        )
    }

    fun clearState() {
        _state.update { current ->
            if (current is PublishCoordinatorState.Uploading ||
                current is PublishCoordinatorState.Moderating ||
                current is PublishCoordinatorState.Publishing
            ) {
                current
            } else {
                PublishCoordinatorState.Idle
            }
        }
    }

    fun dismissBanner() {
        _state.update { current ->
            when (current) {
                is PublishCoordinatorState.Success,
                is PublishCoordinatorState.Failed,
                -> PublishCoordinatorState.Idle
                else -> current
            }
        }
    }

    private companion object {
        const val TAG = "SpotPublishCoordinator"
    }
}

data class PublishJob(
    val images: List<ProcessedImage>,
    val vibeTags: List<String>,
    val latitude: Double,
    val longitude: Double,
    val locationName: String,
)

sealed interface PublishCoordinatorState {
    data object Idle : PublishCoordinatorState
    data object Uploading : PublishCoordinatorState
    data object Moderating : PublishCoordinatorState
    data object Publishing : PublishCoordinatorState
    data class Success(val spotId: String) : PublishCoordinatorState
    data class Failed(val message: String) : PublishCoordinatorState
}
