package com.spot.android.feature.profile

import com.spot.android.core.logging.FakeLogWriter
import com.spot.android.core.logging.SpotLogger
import com.spot.android.data.auth.UserSessionHolder
import com.spot.android.data.content.LocalContentRemovalBus
import com.spot.android.data.feed.FakeEngagementRepository
import com.spot.android.data.model.FollowRelationship
import com.spot.android.data.model.Spot
import com.spot.android.data.model.User
import com.spot.android.data.profile.FakeFollowRepository
import com.spot.android.data.profile.FakeProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeProfileRepository: FakeProfileRepository
    private lateinit var fakeFollowRepository: FakeFollowRepository
    private lateinit var fakeEngagementRepository: FakeEngagementRepository
    private lateinit var userSessionHolder: UserSessionHolder
    private lateinit var localContentRemovalBus: LocalContentRemovalBus
    private lateinit var viewModel: ProfileViewModel

    private val ownUser = User(
        id = "user-1",
        username = "spotter",
        profileImageURL = null,
        isPrivate = true,
        isPro = false,
        proUntil = null,
        spotsCount = 2,
        isCurrentUser = true,
        createdAt = null,
    )

    private val otherUser = User(
        id = "user-2",
        username = "friend",
        profileImageURL = null,
        isPrivate = false,
        isPro = false,
        proUntil = null,
        spotsCount = 1,
        isCurrentUser = false,
        createdAt = null,
    )

    private val spot = Spot(
        id = "spot-1",
        userId = "user-1",
        username = "spotter",
        userProfileImageURL = null,
        caption = "Nice place",
        imageURL = "https://example.com/spot.jpg",
        thumbnailURL = "https://example.com/spot.jpg",
        latitude = 1.0,
        longitude = 2.0,
        locationName = "Park",
        vibeTag = "Chill Spot",
        vibeTags = emptyList(),
        likes = 1,
        saves = 0,
        createdAt = 0L,
        updatedAt = null,
        mediaDisplayAspectRatio = 1.0,
        mediaCount = 1,
        authorIsPrivate = false,
        authorIsPro = false,
        isLiked = false,
        isSaved = false,
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        fakeProfileRepository = FakeProfileRepository()
        fakeFollowRepository = FakeFollowRepository()
        fakeEngagementRepository = FakeEngagementRepository()
        userSessionHolder = UserSessionHolder()
        localContentRemovalBus = LocalContentRemovalBus()

        val logger = SpotLogger(
            preferencesRepository = com.spot.android.core.logging.FakeLogPreferencesRepository(),
            logWriter = FakeLogWriter(),
        )

        viewModel = ProfileViewModel(
            profileRepository = fakeProfileRepository,
            followRepository = fakeFollowRepository,
            engagementRepository = fakeEngagementRepository,
            userSessionHolder = userSessionHolder,
            localContentRemovalBus = localContentRemovalBus,
            logger = logger,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun configureOwnProfile_loadsUserAndSpots() = runTest {
        fakeProfileRepository.ownProfile = ownUser
        fakeProfileRepository.spotIdsByUser = mapOf("user-1" to listOf("spot-1"))
        fakeProfileRepository.hydratedSpots = listOf(spot)
        fakeFollowRepository.pendingCount = 2

        viewModel.configure(userId = null)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(ownUser.username, state.user?.username)
        assertEquals(1, state.spots.size)
        assertEquals(2, state.pendingFollowRequestCount)
        assertEquals(FollowRelationship.Self, state.followRelationship)
    }

    @Test
    fun configureOtherUser_loadsFollowRelationship() = runTest {
        fakeProfileRepository.publicProfiles = mapOf("user-2" to otherUser)
        fakeProfileRepository.spotIdsByUser = mapOf("user-2" to emptyList())
        fakeFollowRepository.relationship = FollowRelationship.NotFollowing

        viewModel.configure(userId = "user-2")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(otherUser.username, state.user?.username)
        assertEquals(FollowRelationship.NotFollowing, state.followRelationship)
        assertEquals(ProfileLoadState.EMPTY, state.loadState)
    }

    @Test
    fun onFollowButtonClick_followsPublicUser() = runTest {
        fakeProfileRepository.publicProfiles = mapOf("user-2" to otherUser)
        fakeProfileRepository.spotIdsByUser = mapOf("user-2" to emptyList())
        fakeFollowRepository.relationship = FollowRelationship.NotFollowing

        viewModel.configure(userId = "user-2")
        advanceUntilIdle()

        viewModel.onFollowButtonClick()
        advanceUntilIdle()

        assertEquals(1, fakeFollowRepository.followCalls)
        assertEquals(FollowRelationship.Following, viewModel.uiState.value.followRelationship)
    }

    @Test
    fun confirmDeleteSpot_removesSpotOptimistically() = runTest {
        fakeProfileRepository.ownProfile = ownUser
        fakeProfileRepository.spotIdsByUser = mapOf("user-1" to listOf("spot-1"))
        fakeProfileRepository.hydratedSpots = listOf(spot)

        viewModel.configure(userId = null)
        advanceUntilIdle()

        viewModel.requestDeleteSpot(spot)
        viewModel.confirmDeleteSpot()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.spots.isEmpty())
        assertEquals(listOf("spot-1"), fakeProfileRepository.deletedSpotIds)
    }
}
