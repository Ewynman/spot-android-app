package com.spot.android.feature.safety

import com.spot.android.core.logging.FakeLogWriter
import com.spot.android.core.logging.SpotLogger
import com.spot.android.core.supabase.SessionBridge
import com.spot.android.core.supabase.SupabaseClientProvider
import com.spot.android.data.auth.UserSessionHolder
import com.spot.android.data.content.ContentRemovalEvent
import com.spot.android.data.content.LocalContentRemovalBus
import com.spot.android.data.model.enums.ReportReason
import com.spot.android.data.model.enums.ReportTargetType
import com.spot.android.data.safety.FakeSafetyRepository
import com.spot.android.data.feed.FeedEventService
import io.github.jan.supabase.gotrue.Auth
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SafetyViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeSafetyRepository: FakeSafetyRepository
    private lateinit var userSessionHolder: UserSessionHolder
    private lateinit var localContentRemovalBus: LocalContentRemovalBus
    private lateinit var sessionBridge: SessionBridge
    private lateinit var viewModel: SafetyViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        fakeSafetyRepository = FakeSafetyRepository()
        userSessionHolder = UserSessionHolder()
        localContentRemovalBus = LocalContentRemovalBus()

        val mockAuth = mockk<Auth>(relaxed = true)
        val mockProvider = mockk<SupabaseClientProvider>(relaxed = true)
        every { mockProvider.auth } returns mockAuth
        sessionBridge = SessionBridge(mockProvider)

        val logger = SpotLogger(
            preferencesRepository = com.spot.android.core.logging.FakeLogPreferencesRepository(),
            logWriter = FakeLogWriter(),
        )

        viewModel = SafetyViewModel(
            safetyRepository = fakeSafetyRepository,
            userSessionHolder = userSessionHolder,
            localContentRemovalBus = localContentRemovalBus,
            feedEventService = FeedEventService(mockProvider, logger),
            sessionBridge = sessionBridge,
            logger = logger,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `openReportSpot shows report sheet with spot target`() = runTest {
        val spot = SafetyPreviewData.demoSpot

        viewModel.openReportSpot(spot)
        advanceUntilIdle()

        val sheet = viewModel.uiState.value.reportSheet
        assertNotNull(sheet)
        assertEquals(ReportTargetType.SPOT, sheet?.targetType)
        assertEquals(spot.id, sheet?.targetId)
        assertEquals(spot.userId, sheet?.reportedUserId)
    }

    @Test
    fun `submitReport calls repository and shows success toast`() = runTest {
        val spot = SafetyPreviewData.demoSpot
        viewModel.openReportSpot(spot)
        viewModel.selectReportReason(ReportReason.SPAM)
        viewModel.submitReport()
        advanceUntilIdle()

        assertEquals(1, fakeSafetyRepository.submitReportCallCount)
        assertEquals(ReportReason.SPAM, fakeSafetyRepository.lastReportReason)
        assertNull(viewModel.uiState.value.reportSheet)
        assertEquals("Report submitted. Thank you.", viewModel.uiState.value.successToast)
    }

    @Test
    fun `submitReport with block requested updates blocked users locally`() = runTest {
        val spot = SafetyPreviewData.demoSpot
        viewModel.openReportSpot(spot)
        viewModel.selectReportReason(ReportReason.HARASSMENT)
        viewModel.toggleBlockRequested()
        viewModel.submitReport()
        advanceUntilIdle()

        assertTrue(fakeSafetyRepository.lastBlockRequested == true)
        assertTrue(userSessionHolder.blockedUsers.value.contains(spot.userId))
    }

    @Test
    fun `confirmBlock calls repository and updates session`() = runTest {
        val userId = SafetyPreviewData.DEMO_PROFILE_USER_ID
        viewModel.openBlockUserFromProfile(userId, SafetyPreviewData.DEMO_PROFILE_USERNAME)
        viewModel.confirmBlock()
        advanceUntilIdle()

        assertEquals(1, fakeSafetyRepository.blockUserCallCount)
        assertEquals(userId, fakeSafetyRepository.lastBlockedUserId)
        assertTrue(userSessionHolder.blockedUsers.value.contains(userId))
        assertNull(viewModel.uiState.value.blockDialog)
        assertEquals("User blocked.", viewModel.uiState.value.successToast)
    }

    @Test
    fun `confirmBlock emits local removal by author`() = runTest {
        val userId = SafetyPreviewData.DEMO_PROFILE_USER_ID
        val events = mutableListOf<ContentRemovalEvent>()
        val job = launch {
            localContentRemovalBus.removals.collect { events.add(it) }
        }

        viewModel.openBlockUserFromProfile(userId, SafetyPreviewData.DEMO_PROFILE_USERNAME)
        viewModel.confirmBlock()
        advanceUntilIdle()

        assertTrue(events.any { it is ContentRemovalEvent.ByAuthor && it.authorUserId == userId })
        job.cancel()
    }

    @Test
    fun `submitReport failure shows error message`() = runTest {
        fakeSafetyRepository.submitReportResult = Result.failure(IllegalStateException("network"))
        val spot = SafetyPreviewData.demoSpot
        viewModel.openReportSpot(spot)
        viewModel.selectReportReason(ReportReason.OTHER)
        viewModel.submitReport()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)
        assertNotNull(viewModel.uiState.value.reportSheet)
    }

    @Test
    fun `openSpotOverflowMenu marks owner spots correctly`() = runTest {
        val spot = SafetyPreviewData.demoSpot.copy(userId = "current-user")
        sessionBridge.refresh()

        viewModel.openSpotOverflowMenu(spot)
        advanceUntilIdle()

        // Without authenticated session, isOwner is false
        assertNotNull(viewModel.uiState.value.spotOverflowMenu)
        assertEquals(false, viewModel.uiState.value.spotOverflowMenu?.isOwner)
    }
}
