package com.spot.android.feature.profile

import com.spot.android.core.logging.FakeLogWriter
import com.spot.android.core.logging.SpotLogger
import com.spot.android.data.model.User
import com.spot.android.data.profile.FakeFollowRepository
import com.spot.android.data.model.FollowRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FollowRequestsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeFollowRepository: FakeFollowRepository
    private lateinit var viewModel: FollowRequestsViewModel

    private val requester = User(
        id = "requester-1",
        username = "requester",
        profileImageURL = null,
        isPrivate = false,
        isPro = false,
        proUntil = null,
        spotsCount = 0,
        isCurrentUser = false,
        createdAt = null,
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeFollowRepository = FakeFollowRepository()
        val logger = SpotLogger(
            preferencesRepository = com.spot.android.core.logging.FakeLogPreferencesRepository(),
            logWriter = FakeLogWriter(),
        )
        viewModel = FollowRequestsViewModel(
            followRepository = fakeFollowRepository,
            logger = logger,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun onFirstAppear_loadsPendingRequests() = runTest {
        fakeFollowRepository.pendingRequests = listOf(
            FollowRequest(
                id = "req-1",
                requester = requester,
                createdAt = null,
            ),
        )

        viewModel.onFirstAppear()
        advanceUntilIdle()

        assertEquals(FollowRequestsLoadState.READY, viewModel.uiState.value.loadState)
        assertEquals(1, viewModel.uiState.value.requests.size)
    }

    @Test
    fun acceptRequest_removesRequestFromList() = runTest {
        fakeFollowRepository.pendingRequests = listOf(
            FollowRequest(
                id = "req-1",
                requester = requester,
                createdAt = null,
            ),
        )
        viewModel.onFirstAppear()
        advanceUntilIdle()

        viewModel.acceptRequest("req-1", requester.id)
        advanceUntilIdle()

        assertEquals(FollowRequestsLoadState.EMPTY, viewModel.uiState.value.loadState)
    }
}
