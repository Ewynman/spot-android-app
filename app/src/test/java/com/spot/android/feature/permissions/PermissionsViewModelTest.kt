package com.spot.android.feature.permissions

import com.spot.android.core.analytics.AnalyticsTracker
import com.spot.android.core.design.component.PermissionType
import com.spot.android.core.logging.FakeLogWriter
import com.spot.android.core.logging.SpotLogger
import com.spot.android.core.logging.FakeLogPreferencesRepository
import com.spot.android.data.permissions.FakePermissionsRepository
import com.spot.android.data.permissions.PermissionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PermissionsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakePermissionsRepository
    private lateinit var fakeAnalytics: FakeAnalyticsTracker
    private lateinit var viewModel: PermissionsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakePermissionsRepository()
        fakeAnalytics = FakeAnalyticsTracker()
        val logger = SpotLogger(
            preferencesRepository = FakeLogPreferencesRepository(),
            logWriter = FakeLogWriter(),
        )
        viewModel = PermissionsViewModel(
            permissionsRepository = fakeRepository,
            analyticsTracker = fakeAnalytics,
            logger = logger,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `authorized permission completes immediately without pre-prompt`() = runTest {
        fakeRepository.setState(PermissionType.LOCATION, PermissionState.AUTHORIZED)
        advanceUntilIdle()

        var result: PermissionState? = null
        viewModel.requestPermission(PermissionType.LOCATION) { result = it }
        advanceUntilIdle()

        assertEquals(PermissionState.AUTHORIZED, result)
        assertNull(viewModel.uiState.value.activePrePrompt)
    }

    @Test
    fun `not determined permission shows pre-prompt`() = runTest {
        fakeRepository.setState(PermissionType.LOCATION, PermissionState.NOT_DETERMINED)
        advanceUntilIdle()

        viewModel.requestPermission(PermissionType.LOCATION)
        advanceUntilIdle()

        assertEquals(PermissionType.LOCATION, viewModel.uiState.value.activePrePrompt)
    }

    @Test
    fun `pre-prompt continue launches system permission request`() = runTest {
        fakeRepository.setState(PermissionType.LOCATION, PermissionState.NOT_DETERMINED)
        advanceUntilIdle()

        viewModel.requestPermission(PermissionType.LOCATION)
        advanceUntilIdle()

        viewModel.onPrePromptContinue()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.activePrePrompt)
        assertTrue(fakeAnalytics.trackedPermissions.contains("location"))
    }

    @Test
    fun `pre-prompt skip completes with denied state`() = runTest {
        fakeRepository.setState(PermissionType.CAMERA, PermissionState.NOT_DETERMINED)
        advanceUntilIdle()

        var result: PermissionState? = null
        viewModel.requestPermission(PermissionType.CAMERA) { result = it }
        advanceUntilIdle()

        viewModel.onPrePromptSkip()
        advanceUntilIdle()

        assertEquals(PermissionState.DENIED, result)
        assertNull(viewModel.uiState.value.activePrePrompt)
    }

    @Test
    fun `not required permission shows pre-prompt then completes without OS dialog`() = runTest {
        fakeRepository.setRuntimeRequired(PermissionType.PHOTOS, required = false)
        advanceUntilIdle()

        var result: PermissionState? = null
        viewModel.requestPermission(PermissionType.PHOTOS) { result = it }
        advanceUntilIdle()

        assertEquals(PermissionType.PHOTOS, viewModel.uiState.value.activePrePrompt)

        viewModel.onPrePromptContinue()
        advanceUntilIdle()

        assertEquals(PermissionState.NOT_REQUIRED, result)
        assertNull(viewModel.uiState.value.activePrePrompt)
    }

    @Test
    fun `system permission granted updates to authorized`() = runTest {
        fakeRepository.setState(PermissionType.LOCATION, PermissionState.NOT_DETERMINED)
        advanceUntilIdle()

        var result: PermissionState? = null
        viewModel.requestPermission(PermissionType.LOCATION) { result = it }
        advanceUntilIdle()

        viewModel.onPrePromptContinue()
        advanceUntilIdle()

        viewModel.onSystemPermissionResult(granted = true)
        advanceUntilIdle()

        assertEquals(PermissionState.AUTHORIZED, result)
    }

    @Test
    fun `permanently denied completes immediately without pre-prompt`() = runTest {
        fakeRepository.setState(PermissionType.LOCATION, PermissionState.PERMANENTLY_DENIED)
        advanceUntilIdle()

        var result: PermissionState? = null
        viewModel.requestPermission(PermissionType.LOCATION) { result = it }
        advanceUntilIdle()

        assertEquals(PermissionState.PERMANENTLY_DENIED, result)
        assertNull(viewModel.uiState.value.activePrePrompt)
    }
}

private class FakeAnalyticsTracker : AnalyticsTracker {
    val trackedPermissions = mutableListOf<String>()

    override fun logEvent(name: String, params: Map<String, Any?>) = Unit

    override fun trackPermissionsRequested(permissionType: String) {
        trackedPermissions.add(permissionType)
    }

    override fun trackAuthReinstall() = Unit
    override fun trackFeedDropPrivate(reason: String) = Unit
    override fun trackImageLoadFailed(source: String) = Unit
    override fun trackAuthEmailInUse() = Unit
    override fun trackAuthDeleteByEmail() = Unit
    override fun trackDeepLink(origin: com.spot.android.core.analytics.DeepLinkOrigin, route: String) = Unit
}
