package com.spot.android.feature.collections

import app.cash.turbine.test
import com.spot.android.core.logging.FakeLogWriter
import com.spot.android.core.logging.SpotLogger
import com.spot.android.data.collections.FakeCollectionsRepository
import com.spot.android.data.model.BookmarkCollection
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

/**
 * Unit tests for CollectionsListViewModel.
 * 
 * Reference: PRD/17-non-functional-testing.md, PRD/10-profile-social.md
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CollectionsListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeRepository: FakeCollectionsRepository
    private lateinit var viewModel: CollectionsListViewModel

    private val collection1 = BookmarkCollection(
        id = "col-1",
        userId = "user-1",
        name = "Favorites",
        sortIndex = 0,
        createdAt = 1000L,
        updatedAt = 1000L,
        spotCount = 5,
    )

    private val collection2 = BookmarkCollection(
        id = "col-2",
        userId = "user-1",
        name = "To Visit",
        sortIndex = 1,
        createdAt = 2000L,
        updatedAt = 2000L,
        spotCount = 3,
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        fakeRepository = FakeCollectionsRepository()

        val logger = SpotLogger(
            preferencesRepository = com.spot.android.core.logging.FakeLogPreferencesRepository(),
            logWriter = FakeLogWriter(),
        )

        viewModel = CollectionsListViewModel(
            collectionsRepository = fakeRepository,
            logger = logger,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        fakeRepository.reset()
    }

    @Test
    fun onFirstAppear_loadsCollections() = runTest {
        fakeRepository.collections = mutableListOf(collection1, collection2)

        viewModel.onFirstAppear()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(CollectionsLoadState.READY, state.loadState)
        assertEquals(2, state.collections.size)
        assertEquals("Favorites", state.collections[0].name)
        assertEquals("To Visit", state.collections[1].name)
    }

    @Test
    fun onFirstAppear_withNoCollections_showsEmpty() = runTest {
        fakeRepository.collections = mutableListOf()

        viewModel.onFirstAppear()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(CollectionsLoadState.EMPTY, state.loadState)
        assertTrue(state.collections.isEmpty())
    }

    @Test
    fun createCollection_addsNewCollection() = runTest {
        fakeRepository.collections = mutableListOf(collection1)

        viewModel.onFirstAppear()
        advanceUntilIdle()

        viewModel.createCollection("Weekend Trips")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.collections.size)
        assertEquals("Weekend Trips", fakeRepository.createdCollections.last().name)
    }

    @Test
    fun createCollection_withEmptyName_showsError() = runTest {
        viewModel.effects.test {
            viewModel.createCollection("   ")
            val effect = awaitItem()
            assertTrue(effect is CollectionsEffect.ShowToast)
            assertEquals("Collection name cannot be empty", (effect as CollectionsEffect.ShowToast).message)
        }
    }

    @Test
    fun deleteCollection_removesCollection() = runTest {
        fakeRepository.collections = mutableListOf(collection1, collection2)

        viewModel.onFirstAppear()
        advanceUntilIdle()

        viewModel.deleteCollection("col-1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.collections.size)
        assertEquals("To Visit", state.collections[0].name)
        assertEquals(listOf("col-1"), fakeRepository.deletedCollectionIds)
    }

    @Test
    fun onCollectionSelected_emitsNavigateEffect() = runTest {
        viewModel.effects.test {
            viewModel.onCollectionSelected("col-1")
            val effect = awaitItem()
            assertTrue(effect is CollectionsEffect.NavigateToCollection)
            assertEquals("col-1", (effect as CollectionsEffect.NavigateToCollection).collectionId)
        }
    }

    @Test
    fun refresh_reloadsCollections() = runTest {
        fakeRepository.collections = mutableListOf(collection1)

        viewModel.onFirstAppear()
        advanceUntilIdle()

        fakeRepository.collections.add(collection2)

        viewModel.refresh()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.collections.size)
    }
}
