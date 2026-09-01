package com.spot.android.feature.map

import com.spot.android.core.analytics.AnalyticsTracker
import com.spot.android.core.analytics.DeepLinkOrigin
import com.spot.android.core.logging.FakeLogWriter
import com.spot.android.core.logging.SpotLogger
import com.spot.android.core.media.ImageUrlSigner
import com.spot.android.core.media.MapMarkerImageCache
import com.spot.android.core.media.MapMarkerImageLoadResult
import com.spot.android.core.supabase.SupabaseClientProvider
import com.spot.android.core.util.Constants
import com.spot.android.data.auth.UserSessionHolder
import com.spot.android.data.content.LocalContentRemovalBus
import com.spot.android.data.dto.MapSpotRowDto
import com.spot.android.data.feed.FakeEngagementRepository
import com.spot.android.data.feed.FeedEventService
import com.spot.android.data.location.MapLocationTracker
import com.spot.android.data.location.ViewerLocation
import com.spot.android.data.map.FakeMapMarkerFeatureFlags
import com.spot.android.data.map.FakeMapRepository
import com.spot.android.data.map.FollowingIdsRepository
import com.spot.android.data.map.MapFocusCoordinator
import com.spot.android.data.map.MapSpotHydrator
import com.spot.android.data.map.MapViewportBounds
import io.github.jan.supabase.gotrue.Auth
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
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
class MapViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeMapRepository: FakeMapRepository
    private lateinit var userSessionHolder: UserSessionHolder
    private lateinit var fakeAnalytics: RecordingAnalyticsTracker
    private lateinit var fakeFeatureFlags: FakeMapMarkerFeatureFlags
    private lateinit var fakeMarkerCache: FakeMapMarkerImageCache
    private lateinit var mapFocusCoordinator: MapFocusCoordinator
    private lateinit var viewModel: MapViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        fakeMapRepository = FakeMapRepository()
        fakeMapRepository.spots = listOf(sampleRow("spot-1"), sampleRow("spot-2"))
        userSessionHolder = UserSessionHolder()
        fakeAnalytics = RecordingAnalyticsTracker()
        fakeFeatureFlags = FakeMapMarkerFeatureFlags(photoPinEnabled = true)
        fakeMarkerCache = FakeMapMarkerImageCache()
        mapFocusCoordinator = MapFocusCoordinator()

        val imageUrlSigner = mockk<ImageUrlSigner>()
        coEvery { imageUrlSigner.getImageUrl(any(), any()) } returns "https://signed.example/image.jpg"
        val hydrator = MapSpotHydrator(imageUrlSigner)

        val mockAuth = mockk<Auth>(relaxed = true)
        val mockProvider = mockk<SupabaseClientProvider>(relaxed = true)
        every { mockProvider.auth } returns mockAuth
        val logger = SpotLogger(
            preferencesRepository = com.spot.android.core.logging.FakeLogPreferencesRepository(),
            logWriter = FakeLogWriter(),
        )

        viewModel = MapViewModel(
            mapRepository = fakeMapRepository,
            mapSpotHydrator = hydrator,
            engagementRepository = FakeEngagementRepository(),
            feedEventService = FeedEventService(mockProvider, logger),
            followingIdsRepository = object : FollowingIdsRepository {
                override suspend fun getFollowedUserIds(): Result<Set<String>> =
                    Result.success(emptySet())
            },
            userSessionHolder = userSessionHolder,
            localContentRemovalBus = LocalContentRemovalBus(),
            mapLocationTracker = object : MapLocationTracker {
                override val locationUpdates = emptyFlow<ViewerLocation>()
                override fun startTracking() = Unit
                override fun stopTracking() = Unit
            },
            mapMarkerFeatureFlags = fakeFeatureFlags,
            mapFocusCoordinator = mapFocusCoordinator,
            analyticsTracker = fakeAnalytics,
            markerImageCache = fakeMarkerCache,
            logger = logger,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `viewport fetch loads and merges spots`() = runTest {
        viewModel.onFirstAppear()
        viewModel.onCameraIdle(
            centerLat = 40.7128,
            centerLng = -74.0060,
            zoom = 13f,
            userInitiated = false,
        )

        advanceTimeBy(300)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.allSpots.isNotEmpty())
        assertTrue(state.pins.isNotEmpty())
    }

    @Test
    fun `pin selection opens drawer`() = runTest {
        viewModel.onFirstAppear()
        viewModel.onCameraIdle(40.7128, -74.0060, 13f, userInitiated = false)
        advanceTimeBy(300)
        advanceUntilIdle()

        val spotId = viewModel.uiState.value.allSpots.keys.first()
        viewModel.onPinSelected(spotId)

        assertEquals(spotId, viewModel.uiState.value.selectedSpotId)
        assertEquals(MapDrawerState.PEEK, viewModel.uiState.value.drawerState)
    }

    @Test
    fun `tab reselect dismisses drawer`() = runTest {
        viewModel.onFirstAppear()
        viewModel.onCameraIdle(40.7128, -74.0060, 13f, userInitiated = false)
        advanceTimeBy(300)
        advanceUntilIdle()

        val spotId = viewModel.uiState.value.allSpots.keys.first()
        viewModel.onPinSelected(spotId)
        viewModel.onTabReselected()

        assertNull(viewModel.uiState.value.selectedSpotId)
        assertEquals(MapDrawerState.HIDDEN, viewModel.uiState.value.drawerState)
    }

    @Test
    fun `map moved away dismisses drawer`() = runTest {
        viewModel.onFirstAppear()
        viewModel.onCameraIdle(40.7128, -74.0060, 13f, userInitiated = false)
        advanceTimeBy(300)
        advanceUntilIdle()

        val spotId = viewModel.uiState.value.allSpots.keys.first()
        viewModel.onPinSelected(spotId)

        viewModel.onCameraIdle(40.0, -75.0, 13f, userInitiated = true)
        assertNull(viewModel.uiState.value.selectedSpotId)
    }

    @Test
    fun `pro filter change dismisses hidden spot`() = runTest {
        userSessionHolder.updateProStatus(isPro = true, proUntil = null)
        advanceUntilIdle()

        viewModel.onFirstAppear()
        viewModel.onCameraIdle(40.7128, -74.0060, 13f, userInitiated = false)
        advanceTimeBy(300)
        advanceUntilIdle()

        val spotId = viewModel.uiState.value.allSpots.keys.first()
        viewModel.onPinSelected(spotId)

        viewModel.toggleFilter(com.spot.android.data.map.SpotMapFilter.VIBE)
        viewModel.applyVibeFilter(setOf("Nonexistent Vibe"))

        assertNull(viewModel.uiState.value.selectedSpotId)
    }

    @Test
    fun `pin impression fires once per spot per session and re-fires after tab left`() = runTest {
        viewModel.onPinImpression("spot-1", hasImage = true)
        viewModel.onPinImpression("spot-1", hasImage = true)
        viewModel.onPinImpression("spot-2", hasImage = false)

        assertEquals(2, fakeAnalytics.impressions.size)
        assertEquals(
            Constants.Analytics.Values.MARKER_TYPE_PHOTO_PIN,
            fakeAnalytics.impressions.first(),
        )
        assertEquals(
            Constants.Analytics.Values.MARKER_TYPE_TEARDROP,
            fakeAnalytics.impressions.last(),
        )

        viewModel.onTabLeft()
        viewModel.onPinImpression("spot-1", hasImage = true)
        assertEquals(3, fakeAnalytics.impressions.size)
    }

    @Test
    fun `pin tap records marker type and zoom rounded to half`() = runTest {
        viewModel.onFirstAppear()
        viewModel.onCameraIdle(40.7128, -74.0060, 13f, userInitiated = false)
        advanceTimeBy(300)
        advanceUntilIdle()

        val spotId = viewModel.uiState.value.allSpots.keys.first()
        viewModel.onPinTapped(spotId = spotId, zoomLevel = 13.4f, hasImage = true)

        assertEquals(1, fakeAnalytics.taps.size)
        val (type, zoom) = fakeAnalytics.taps.first()
        assertEquals(Constants.Analytics.Values.MARKER_TYPE_PHOTO_PIN, type)
        assertEquals(13.5, zoom, 0.001)
    }

    @Test
    fun `pin tap without photo pin reports teardrop type`() = runTest {
        fakeFeatureFlags.setPhotoPinMarkersEnabled(false)

        viewModel.onFirstAppear()
        viewModel.onCameraIdle(40.7128, -74.0060, 13f, userInitiated = false)
        advanceTimeBy(300)
        advanceUntilIdle()

        val spotId = viewModel.uiState.value.allSpots.keys.first()
        viewModel.onPinTapped(spotId = spotId, zoomLevel = 12.1f, hasImage = true)

        val (type, zoom) = fakeAnalytics.taps.first()
        assertEquals(Constants.Analytics.Values.MARKER_TYPE_TEARDROP, type)
        assertEquals(12.0, zoom, 0.001)
    }

    @Test
    fun `focus request selects spot once viewport hydrates it`() = runTest {
        mapFocusCoordinator.openInMap(
            spotId = "spot-1",
            target = com.google.android.gms.maps.model.LatLng(40.7128, -74.0060),
        )
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.selectedSpotId)

        viewModel.onFirstAppear()
        viewModel.onCameraIdle(40.7128, -74.0060, 13f, userInitiated = false)
        advanceTimeBy(300)
        advanceUntilIdle()

        assertEquals("spot-1", viewModel.uiState.value.selectedSpotId)
        assertEquals(MapDrawerState.PEEK, viewModel.uiState.value.drawerState)
        assertNull(mapFocusCoordinator.pendingFocus.value)
    }

    @Test
    fun `marker image load callbacks track success failure and cache hit`() {
        viewModel.onMarkerImageLoaded(cacheHit = false)
        viewModel.onMarkerImageLoaded(cacheHit = true)
        viewModel.onMarkerImageFailed()

        assertEquals(
            listOf(
                Triple(true, false, false),
                Triple(true, true, false),
                Triple(false, false, false),
            ).map { it.first to it.second },
            fakeAnalytics.imageLoads,
        )
    }

    private fun sampleRow(spotId: String): MapSpotRowDto {
        return MapSpotRowDto(
            spot_id = spotId,
            user_id = "user-1",
            vibe_tag_id = null,
            caption = "A spot",
            latitude = 40.7128,
            longitude = -74.0060,
            location_name = "NYC",
            created_at = "2024-01-01T00:00:00Z",
            author_username = "testuser",
            author_profile_image_url = null,
            vibe_name = "Chill Spot",
            primary_storage_path = null,
            primary_public_url = null,
            distance_meters = 100.0,
        )
    }
}

private class RecordingAnalyticsTracker : AnalyticsTracker {
    val impressions = mutableListOf<String>()
    val taps = mutableListOf<Pair<String, Double>>()
    val imageLoads = mutableListOf<Pair<Boolean, Boolean>>()

    override fun logEvent(name: String, params: Map<String, Any?>) = Unit
    override fun trackAuthReinstall() = Unit
    override fun trackPermissionsRequested(permissionType: String) = Unit
    override fun trackFeedDropPrivate(reason: String) = Unit
    override fun trackImageLoadFailed(source: String) = Unit
    override fun trackAuthEmailInUse() = Unit
    override fun trackAuthDeleteByEmail() = Unit
    override fun trackDeepLink(origin: DeepLinkOrigin, route: String) = Unit

    override fun trackMapMarkerImpression(markerType: String) {
        impressions += markerType
    }

    override fun trackMapMarkerImageLoad(success: Boolean, cacheHit: Boolean) {
        imageLoads += success to cacheHit
    }

    override fun trackMapMarkerTapped(markerType: String, zoomLevel: Double) {
        taps += markerType to zoomLevel
    }
}

private class FakeMapMarkerImageCache : MapMarkerImageCache {
    override suspend fun load(url: String, targetPx: Int): MapMarkerImageLoadResult =
        MapMarkerImageLoadResult.Failure
    override fun onTrimMemory() = Unit
    override fun clear() = Unit
    override fun size(): Int = 0
}
