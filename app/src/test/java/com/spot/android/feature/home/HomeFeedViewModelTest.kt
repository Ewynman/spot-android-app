package com.spot.android.feature.home

import com.spot.android.core.logging.FakeLogWriter
import com.spot.android.core.logging.SpotLogger
import com.spot.android.core.media.ImageUrlSigner
import com.spot.android.core.util.Constants
import com.spot.android.data.auth.UserSessionHolder
import com.spot.android.data.content.LocalContentRemovalBus
import com.spot.android.data.dto.HomeFeedRowDto
import com.spot.android.data.feed.FakeEngagementRepository
import com.spot.android.data.feed.FakeFeedRepository
import com.spot.android.data.feed.FeedEventService
import com.spot.android.data.feed.FeedSpotHydrator
import com.spot.android.data.feed.HomeFeedEmptyReason
import com.spot.android.data.feed.HomeFeedStatusDto
import com.spot.android.data.location.ViewerLocationProvider
import com.spot.android.data.post.SpotPostedBus
import com.spot.android.data.post.SpotPublishCoordinator
import com.spot.android.data.post.FakeSpotPublishRepository
import com.spot.android.data.post.FakeVibeTagRepository
import com.spot.android.data.post.FakePostDraftRepository
import com.spot.android.core.supabase.SupabaseClientProvider
import io.github.jan.supabase.gotrue.Auth
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeFeedViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeFeedRepository: FakeFeedRepository
    private lateinit var fakeEngagementRepository: FakeEngagementRepository
    private lateinit var userSessionHolder: UserSessionHolder
    private lateinit var localContentRemovalBus: LocalContentRemovalBus
    private lateinit var feedSpotHydrator: FeedSpotHydrator
    private lateinit var feedEventService: FeedEventService
    private lateinit var viewerLocationProvider: ViewerLocationProvider
    private lateinit var viewModel: HomeFeedViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        fakeFeedRepository = FakeFeedRepository()
        fakeEngagementRepository = FakeEngagementRepository()
        userSessionHolder = UserSessionHolder()
        localContentRemovalBus = LocalContentRemovalBus()

        val imageUrlSigner = mockk<ImageUrlSigner>()
        coEvery { imageUrlSigner.getImageUrl(any(), any()) } returns "https://signed.example/image.jpg"
        feedSpotHydrator = FeedSpotHydrator(imageUrlSigner)

        val mockAuth = mockk<Auth>(relaxed = true)
        val mockProvider = mockk<SupabaseClientProvider>(relaxed = true)
        every { mockProvider.auth } returns mockAuth
        val logger = SpotLogger(
            preferencesRepository = com.spot.android.core.logging.FakeLogPreferencesRepository(),
            logWriter = FakeLogWriter(),
        )
        feedEventService = FeedEventService(mockProvider, logger)

        viewerLocationProvider = object : ViewerLocationProvider {
            override suspend fun getLocation() = null
        }

        viewModel = HomeFeedViewModel(
            feedRepository = fakeFeedRepository,
            engagementRepository = fakeEngagementRepository,
            feedSpotHydrator = feedSpotHydrator,
            feedEventService = feedEventService,
            userSessionHolder = userSessionHolder,
            localContentRemovalBus = localContentRemovalBus,
            viewerLocationProvider = viewerLocationProvider,
            spotPostedBus = SpotPostedBus(),
            spotPublishCoordinator = SpotPublishCoordinator(
                vibeTagRepository = FakeVibeTagRepository(),
                spotPublishRepository = FakeSpotPublishRepository(),
                postDraftRepository = FakePostDraftRepository(),
                spotPostedBus = SpotPostedBus(),
                logger = logger,
            ),
            logger = logger,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onFirstAppear loads feed spots`() = runTest {
        fakeFeedRepository.homeFeedResult = Result.success(listOf(sampleRow("spot-1")))

        viewModel.onFirstAppear()
        advanceUntilIdle()

        assertEquals(FeedLoadState.LOADED, viewModel.uiState.value.loadState)
        assertEquals(1, viewModel.uiState.value.spots.size)
        assertEquals("spot-1", viewModel.uiState.value.spots.first().id)
    }

    @Test
    fun `caught up empty status triggers seen fallback fetch`() = runTest {
        fakeFeedRepository.homeFeedResult = Result.success(emptyList())
        fakeFeedRepository.statusResult = Result.success(
            HomeFeedStatusDto(
                total_spots = 10,
                eligible_spots = 10,
                unseen_eligible_spots = 0,
                seen_eligible_spots = 10,
                status = "caught_up",
            ),
        )

        viewModel.onFirstAppear()
        advanceUntilIdle()

        assertEquals(2, fakeFeedRepository.getHomeFeedCallCount)
        assertEquals(true, fakeFeedRepository.lastForceSeenFallback)
    }

    @Test
    fun `empty feed shows empty reason`() = runTest {
        fakeFeedRepository.homeFeedResult = Result.success(emptyList())
        fakeFeedRepository.statusResult = Result.success(
            HomeFeedStatusDto(
                total_spots = 0,
                eligible_spots = 0,
                unseen_eligible_spots = 0,
                seen_eligible_spots = 0,
                status = "no_spots_global",
            ),
        )

        viewModel.onFirstAppear()
        advanceUntilIdle()

        assertEquals(FeedLoadState.EMPTY, viewModel.uiState.value.loadState)
        assertEquals(HomeFeedEmptyReason.NO_SPOTS_GLOBAL, viewModel.uiState.value.emptyReason)
    }

    @Test
    fun `toggleLike rolls back on failure`() = runTest {
        fakeFeedRepository.homeFeedResult = Result.success(listOf(sampleRow("spot-1")))
        viewModel.onFirstAppear()
        advanceUntilIdle()

        val spot = viewModel.uiState.value.spots.first()
        fakeEngagementRepository.likeResult = Result.failure(IllegalStateException("fail"))

        viewModel.toggleLike(spot)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.spots.first().isLiked)
        assertFalse(userSessionHolder.likedSpots.value.contains("spot-1"))
    }

    @Test
    fun `bookmark cap emits paywall effect for free users`() = runTest {
        fakeFeedRepository.homeFeedResult = Result.success(listOf(sampleRow("spot-1")))
        viewModel.onFirstAppear()
        advanceUntilIdle()

        repeat(Constants.ContentLimits.FREE_BOOKMARK_CAP) { index ->
            userSessionHolder.addBookmark("existing-$index")
        }

        val effects = mutableListOf<HomeFeedEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.toggleBookmark(viewModel.uiState.value.spots.first())
        advanceUntilIdle()

        assertTrue(effects.any { it is HomeFeedEffect.ShowPaywall })
        job.cancel()
    }

    @Test
    fun `content removal by author removes spots from feed`() = runTest {
        fakeFeedRepository.homeFeedResult = Result.success(
            listOf(
                sampleRow("spot-1", userId = "author-a"),
                sampleRow("spot-2", userId = "author-b"),
            ),
        )
        viewModel.onFirstAppear()
        advanceUntilIdle()

        localContentRemovalBus.removeByAuthor("author-a")
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.spots.size)
        assertEquals("author-b", viewModel.uiState.value.spots.first().userId)
    }

    @Test
    fun `refresh error preserves existing spots and shows toast`() = runTest {
        fakeFeedRepository.homeFeedResult = Result.success(listOf(sampleRow("spot-1")))
        viewModel.onFirstAppear()
        advanceUntilIdle()

        fakeFeedRepository.homeFeedResult = Result.failure(IllegalStateException("network"))
        viewModel.refresh()
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.spots.size)
        assertTrue(viewModel.uiState.value.errorToast != null)
    }

    private fun sampleRow(
        spotId: String,
        userId: String = "user-1",
    ): HomeFeedRowDto {
        return HomeFeedRowDto(
            spot_id = spotId,
            user_id = userId,
            vibe_tag_id = null,
            caption = "caption",
            latitude = 1.0,
            longitude = 2.0,
            location_name = "Test",
            likes_count = 1,
            saves_count = 0,
            created_at = "2024-01-01T00:00:00Z",
            updated_at = "2024-01-01T00:00:00Z",
            author_username = "author",
            author_profile_image_url = null,
            author_is_private = false,
            vibe_name = "Chill Spot",
            primary_storage_path = "path/image.jpg",
            primary_public_url = null,
            source_bucket = "personalized_unseen",
            rank_position = 1,
            ranking_score = 1.0,
            seen_before = false,
            last_seen_at = null,
            media_display_aspect_ratio = 1.0,
            media_count = 1,
        )
    }
}
