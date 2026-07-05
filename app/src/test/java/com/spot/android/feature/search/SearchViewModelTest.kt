package com.spot.android.feature.search

import com.spot.android.core.logging.FakeLogWriter
import com.spot.android.core.logging.SpotLogger
import com.spot.android.data.auth.UserSessionHolder
import com.spot.android.data.feed.FakeEngagementRepository
import com.spot.android.data.model.User
import com.spot.android.data.model.VibeTag
import com.spot.android.data.search.FakeSearchRepository
import com.spot.android.data.search.SearchGridPageLoader
import com.spot.android.data.search.SearchGridRequest
import com.spot.android.data.search.SearchHistoryStore
import com.spot.android.data.search.SearchSegment
import com.spot.android.data.search.SearchSpotHydrator
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
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
class SearchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeSearchRepository: FakeSearchRepository
    private lateinit var fakeHistoryStore: SearchHistoryStore
    private lateinit var searchGridPageLoader: SearchGridPageLoader
    private lateinit var userSessionHolder: UserSessionHolder
    private lateinit var viewModel: SearchViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        fakeSearchRepository = FakeSearchRepository()
        fakeHistoryStore = mockk(relaxed = true)
        coEvery { fakeHistoryStore.historyFlow(any()) } returns flowOf(emptyList())
        coEvery { fakeHistoryStore.getHistory(any()) } returns emptyList()

        val hydrator = mockk<SearchSpotHydrator>()
        coEvery { hydrator.hydrateByIds(any()) } returns emptyList()
        searchGridPageLoader = SearchGridPageLoader(fakeSearchRepository, hydrator)

        userSessionHolder = UserSessionHolder()
        val logger = SpotLogger(
            preferencesRepository = com.spot.android.core.logging.FakeLogPreferencesRepository(),
            logWriter = FakeLogWriter(),
        )

        viewModel = SearchViewModel(
            searchRepository = fakeSearchRepository,
            searchHistoryStore = fakeHistoryStore,
            searchGridPageLoader = searchGridPageLoader,
            engagementRepository = FakeEngagementRepository(),
            userSessionHolder = userSessionHolder,
            logger = logger,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `debounced user search returns results`() = runTest {
        fakeSearchRepository.users = listOf(
            sampleUser("alice"),
            sampleUser("alex"),
        )

        viewModel.onFirstAppear()
        viewModel.onQueryChanged("al")
        advanceTimeBy(300)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(SearchLoadState.Loaded, state.loadState)
        assertEquals(2, state.users.size)
    }

    @Test
    fun `empty query on vibes segment loads catalog`() = runTest {
        fakeSearchRepository.allVibeTags = listOf(
            VibeTag(id = "1", name = "Chill Spot", nameLower = "chill spot"),
        )

        viewModel.onFirstAppear()
        viewModel.onSegmentSelected(SearchSegment.Vibes)
        advanceUntilIdle()

        assertEquals(SearchLoadState.Loaded, viewModel.uiState.value.loadState)
        assertEquals(1, viewModel.uiState.value.vibes.size)
    }

    @Test
    fun `empty users query shows empty results state`() = runTest {
        viewModel.onFirstAppear()
        viewModel.onQueryChanged("")
        advanceTimeBy(300)
        advanceUntilIdle()

        assertEquals(SearchLoadState.Loaded, viewModel.uiState.value.loadState)
        assertTrue(viewModel.uiState.value.users.isEmpty())
    }

    @Test
    fun `tab reselect clears query and grid`() = runTest {
        viewModel.onQueryChanged("alice")
        advanceTimeBy(300)
        advanceUntilIdle()

        viewModel.onLocationSelected("Central Park")
        advanceUntilIdle()
        assertEquals(SearchScreenMode.Grid, viewModel.uiState.value.mode)

        viewModel.onTabReselected()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("", state.query)
        assertEquals(SearchScreenMode.Main, state.mode)
        assertEquals(null, state.grid)
    }

    @Test
    fun `location selection opens grid`() = runTest {
        val request = SearchGridRequest.Location(locationPattern = "Central Park")
        fakeSearchRepository.spotIdsByRequest = mapOf(request to listOf("spot-1"))

        viewModel.onLocationSelected("Central Park")
        advanceUntilIdle()

        val grid = viewModel.uiState.value.grid
        assertEquals(SearchScreenMode.Grid, viewModel.uiState.value.mode)
        assertEquals("Central Park", grid?.title)
        assertEquals(false, grid?.isVibeGrid)
    }

    @Test
    fun `user selection records history`() = runTest {
        val user = sampleUser("bob")
        viewModel.onUserSelected(user)
        advanceUntilIdle()

        coVerify {
            fakeHistoryStore.addItem(
                segment = SearchSegment.Users,
                item = match { it.query == "bob" && it.displayText == "bob" },
            )
        }
        assertEquals(SearchScreenMode.UserProfile, viewModel.uiState.value.mode)
    }

    private fun sampleUser(username: String): User {
        return User(
            id = "user-$username",
            username = username,
            profileImageURL = null,
            isPrivate = false,
            isPro = false,
            proUntil = null,
            spotsCount = 0,
            createdAt = null,
        )
    }
}
