package com.spot.android.feature.post

import com.spot.android.core.logging.FakeLogPreferencesRepository
import com.spot.android.core.logging.FakeLogWriter
import com.spot.android.core.logging.SpotLogger
import com.spot.android.core.supabase.SessionBridge
import com.spot.android.data.auth.UserSessionHolder
import com.spot.android.data.auth.FakeAuthRepository
import com.spot.android.data.location.FakePlaceSearchProvider
import com.spot.android.data.location.PlaceSuggestion
import com.spot.android.data.post.FakePostDraftRepository
import com.spot.android.data.post.FakeSpotPublishRepository
import com.spot.android.data.post.FakeVibeTagRepository
import com.spot.android.data.post.PostingRulesStore
import com.spot.android.data.post.SpotPostedBus
import com.spot.android.data.post.SpotPublishCoordinator
import com.spot.android.navigation.ShellNavigationBus
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class PostViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var sessionBridge: SessionBridge
    private lateinit var userSessionHolder: UserSessionHolder
    private lateinit var fakeDraftRepository: FakePostDraftRepository
    private lateinit var postingRulesStore: PostingRulesStore
    private lateinit var spotPublishCoordinator: SpotPublishCoordinator
    private lateinit var viewModel: PostViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        fakeAuthRepository = FakeAuthRepository()
        fakeAuthRepository.emailVerified = true

        val mockProvider = mockk<com.spot.android.core.supabase.SupabaseClientProvider>(relaxed = true)
        sessionBridge = SessionBridge(mockProvider)
        every { mockProvider.client } returns mockk(relaxed = true)

        userSessionHolder = UserSessionHolder()
        fakeDraftRepository = FakePostDraftRepository()

        val logger = SpotLogger(
            preferencesRepository = FakeLogPreferencesRepository(),
            logWriter = FakeLogWriter(),
        )

        postingRulesStore = mockk(relaxed = true)
        coEvery { postingRulesStore.hasAccepted() } returns true
        coEvery { postingRulesStore.setAccepted(any()) } returns Unit

        spotPublishCoordinator = SpotPublishCoordinator(
            vibeTagRepository = FakeVibeTagRepository(),
            spotPublishRepository = FakeSpotPublishRepository(),
            postDraftRepository = fakeDraftRepository,
            spotPostedBus = SpotPostedBus(),
            logger = logger,
        )

        viewModel = PostViewModel(
            authRepository = fakeAuthRepository,
            sessionBridge = sessionBridge,
            userSessionHolder = userSessionHolder,
            imageProcessor = mockk(relaxed = true),
            postDraftRepository = fakeDraftRepository,
            postingRulesStore = postingRulesStore,
            spotPublishCoordinator = spotPublishCoordinator,
            placeSearchProvider = FakePlaceSearchProvider(),
            shellNavigationBus = ShellNavigationBus(),
            logger = logger,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun onFirstAppear_withVerifiedEmail_showsComposer() = runTest {
        viewModel.onFirstAppear()
        advanceUntilIdle()

        assertEquals(PostEntryState.COMPOSER, viewModel.uiState.value.entryState)
    }

    @Test
    fun onFirstAppear_withUnverifiedEmail_showsGate() = runTest {
        fakeAuthRepository.emailVerified = false

        viewModel.onFirstAppear()
        advanceUntilIdle()

        assertEquals(PostEntryState.EMAIL_NOT_VERIFIED, viewModel.uiState.value.entryState)
    }

    @Test
    fun toggleVibe_freeUser_limitsToOne() = runTest {
        viewModel.onFirstAppear()
        advanceUntilIdle()

        viewModel.toggleVibe("Chill Spot")
        viewModel.toggleVibe("Hidden Gem")

        assertEquals(setOf("Chill Spot"), viewModel.uiState.value.selectedVibes)
        assertEquals(
            "Multiple vibes are available with Pro.",
            viewModel.uiState.value.validationMessage,
        )
    }

    @Test
    fun selectPlace_updatesLocationState() = runTest {
        viewModel.onFirstAppear()
        advanceUntilIdle()

        val place = PlaceSuggestion(
            name = "Central Park",
            address = "NYC",
            latitude = 40.0,
            longitude = -73.0,
        )
        viewModel.selectPlace(place)

        assertEquals(place, viewModel.uiState.value.selectedPlace)
    }

    @Test
    fun canProceedCurrentStep_requiresImagesOnPhotosStep() = runTest {
        viewModel.onFirstAppear()
        advanceUntilIdle()

        assertFalse(viewModel.canProceedCurrentStep())
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class VibeTagValidatorTest {

    @Test
    fun validate_rejectsTooShort() {
        val result = VibeTagValidator.validate("a")
        assertTrue(result is VibeTagValidator.ValidationResult.Invalid)
    }

    @Test
    fun validate_acceptsValidTag() {
        val result = VibeTagValidator.validate("Cozy")
        assertEquals(VibeTagValidator.ValidationResult.Valid, result)
    }

    @Test
    fun validate_rejectsBlockedTag() {
        val result = VibeTagValidator.validate("nsfw vibes")
        assertTrue(result is VibeTagValidator.ValidationResult.Invalid)
    }
}
