package com.spot.android.feature.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.spot.android.core.logging.LogCategory
import com.spot.android.core.logging.SpotLogger
import com.spot.android.core.util.Constants
import com.spot.android.data.auth.UserSessionHolder
import com.spot.android.data.content.ContentRemovalEvent
import com.spot.android.data.content.LocalContentRemovalBus
import com.spot.android.data.feed.EngagementRepository
import com.spot.android.data.feed.FeedEventService
import com.spot.android.data.location.MapLocationTracker
import com.spot.android.data.location.ViewerLocation
import com.spot.android.data.map.FollowingIdsRepository
import com.spot.android.data.map.MapPinLayout
import com.spot.android.data.map.MapRepository
import com.spot.android.data.map.MapSpotFilterEngine
import com.spot.android.data.map.MapSpotHydrator
import com.spot.android.data.map.MapViewportBounds
import com.spot.android.data.map.SpotMapFilter
import com.spot.android.data.model.Spot
import com.spot.android.data.model.enums.FeedEventType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the map tab and ProfileMapView variant.
 *
 * Reference: PRD/07-map.md
 */
@HiltViewModel
class MapViewModel @Inject constructor(
    private val mapRepository: MapRepository,
    private val mapSpotHydrator: MapSpotHydrator,
    private val engagementRepository: EngagementRepository,
    private val feedEventService: FeedEventService,
    private val followingIdsRepository: FollowingIdsRepository,
    private val userSessionHolder: UserSessionHolder,
    private val localContentRemovalBus: LocalContentRemovalBus,
    private val mapLocationTracker: MapLocationTracker,
    private val logger: SpotLogger,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val _effects = Channel<MapEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var viewportFetchJob: Job? = null
    private var lastViewport: MapViewportBounds? = null
    private var followedUserIds: Set<String> = emptySet()
    private var initialLoadStarted = false
    private var profileUserIdFilter: String? = null

    init {
        observeSession()
        observeContentRemovals()
        observeLocationUpdates()
    }

    fun onFirstAppear() {
        if (initialLoadStarted) return
        initialLoadStarted = true

        viewModelScope.launch {
            followingIdsRepository.getFollowedUserIds()
                .onSuccess { followedUserIds = it }
            rebuildVisiblePins(lastViewport)
        }
    }

    fun setProfileUserFilter(userId: String?) {
        profileUserIdFilter = userId
        _uiState.update { it.copy(profileFilterUserId = userId) }
        rebuildVisiblePins(lastViewport)
    }

    fun onCameraIdle(
        centerLat: Double,
        centerLng: Double,
        zoom: Float,
        userInitiated: Boolean,
    ) {
        if (userInitiated) {
            _uiState.update { it.copy(userHasMovedMap = true) }
            maybeDismissForMapMoved(centerLat, centerLng)
        }

        val viewport = MapViewportBounds.fromCenterZoom(centerLat, centerLng, zoom)
        lastViewport = viewport

        viewportFetchJob?.cancel()
        viewportFetchJob = viewModelScope.launch {
            delay(Constants.MapDesign.VIEWPORT_FETCH_DEBOUNCE_MS)
            fetchViewport(viewport)
        }
    }

    fun onUserLocationReceived(location: ViewerLocation, isFirstFix: Boolean) {
        val latLng = LatLng(location.latitude, location.longitude)
        _uiState.update {
            it.copy(
                userLocation = latLng,
                showLocationHalo = true,
            )
        }

        viewModelScope.launch {
            delay(1_500)
            _uiState.update { it.copy(showLocationHalo = false) }
        }

        val state = _uiState.value
        if (isFirstFix && !state.hasCenteredOnFirstFix && !state.userHasMovedMap) {
            _uiState.update { it.copy(hasCenteredOnFirstFix = true) }
            viewModelScope.launch {
                _effects.send(
                    MapEffect.AnimateCamera(
                        target = latLng,
                        zoom = Constants.MapDesign.NEIGHBORHOOD_ZOOM,
                    ),
                )
            }
        }
    }

    fun onMapTapped() {
        dismissDrawer(MapDrawerDismissReason.EMPTY_MAP_TAP)
    }

    fun onPinSelected(spotId: String) {
        val currentSelection = _uiState.value.selectedSpotId
        if (currentSelection != null && currentSelection != spotId) {
            dismissDrawer(MapDrawerDismissReason.SPOT_SWITCH)
        }

        val spot = _uiState.value.allSpots[spotId] ?: return
        _uiState.update {
            it.copy(
                selectedSpotId = spotId,
                drawerState = MapDrawerState.PEEK,
            )
        }

        viewModelScope.launch {
            _effects.send(
                MapEffect.AnimateCamera(
                    target = LatLng(spot.latitude, spot.longitude),
                    liftMeters = Constants.MapDesign.SELECTED_PIN_CAMERA_LIFT_METERS,
                ),
            )
        }

        feedEventService.recordEvent(
            spotId = spotId,
            eventType = FeedEventType.MAP_PIN_TAP,
        )
    }

    fun onDrawerClose() {
        dismissDrawer(MapDrawerDismissReason.CLOSE_BUTTON)
    }

    fun onDrawerExpandToggle() {
        _uiState.update { state ->
            val next = when (state.drawerState) {
                MapDrawerState.PEEK -> MapDrawerState.EXPANDED
                MapDrawerState.EXPANDED -> MapDrawerState.PEEK
                MapDrawerState.HIDDEN -> MapDrawerState.PEEK
            }
            state.copy(drawerState = next)
        }
    }

    fun onRecenterTapped() {
        val location = _uiState.value.userLocation ?: return
        viewModelScope.launch {
            _effects.send(
                MapEffect.AnimateCamera(
                    target = location,
                    zoom = Constants.MapDesign.NEIGHBORHOOD_ZOOM,
                ),
            )
        }
    }

    fun onTabReselected() {
        _uiState.update { it.copy(showVibeFilterSheet = false) }
        dismissDrawer(MapDrawerDismissReason.TAB_RESELECTED)
    }

    fun onTabLeft() {
        _uiState.update { it.copy(showVibeFilterSheet = false) }
        dismissDrawer(MapDrawerDismissReason.TAB_LEFT)
    }

    fun toggleFilter(filter: SpotMapFilter) {
        if (!_uiState.value.isPro) return

        val wasActive = _uiState.value.activeFilters.contains(filter)
        val nextFilters = if (wasActive) {
            _uiState.value.activeFilters - filter
        } else {
            _uiState.value.activeFilters + filter
        }

        if (filter == SpotMapFilter.VIBE && !wasActive) {
            _uiState.update {
                it.copy(
                    activeFilters = nextFilters,
                    showVibeFilterSheet = true,
                )
            }
        } else {
            _uiState.update { it.copy(activeFilters = nextFilters) }
        }

        rebuildVisiblePins(lastViewport)
        maybeDismissForFilterChange()
    }

    fun onVibeFilterSheetDismissed() {
        _uiState.update { it.copy(showVibeFilterSheet = false) }
    }

    fun applyVibeFilter(names: Set<String>) {
        _uiState.update {
            it.copy(
                selectedVibeNames = names,
                showVibeFilterSheet = false,
            )
        }
        rebuildVisiblePins(lastViewport)
        maybeDismissForFilterChange()
    }

    fun toggleLike(spot: Spot) {
        val currentlyLiked = spot.isLiked
        val optimisticCount = if (currentlyLiked) {
            (spot.likes - 1).coerceAtLeast(0)
        } else {
            spot.likes + 1
        }

        updateSpot(spot.id) {
            it.copy(isLiked = !currentlyLiked, likes = optimisticCount)
        }

        if (currentlyLiked) {
            userSessionHolder.removeLike(spot.id)
        } else {
            userSessionHolder.addLike(spot.id)
        }

        viewModelScope.launch {
            val result = if (currentlyLiked) {
                engagementRepository.unlikeSpot(spot.id)
            } else {
                engagementRepository.likeSpot(spot.id)
            }

            result.fold(
                onSuccess = {
                    feedEventService.recordEvent(
                        spotId = spot.id,
                        eventType = if (currentlyLiked) FeedEventType.UNLIKE else FeedEventType.LIKE,
                    )
                },
                onFailure = {
                    logger.w(LogCategory.Map, TAG, "Like toggle failed; rolling back", it)
                    if (currentlyLiked) {
                        userSessionHolder.addLike(spot.id)
                    } else {
                        userSessionHolder.removeLike(spot.id)
                    }
                    updateSpot(spot.id) {
                        it.copy(isLiked = currentlyLiked, likes = spot.likes)
                    }
                },
            )
        }
    }

    fun toggleBookmark(spot: Spot) {
        val currentlySaved = spot.isSaved

        if (!currentlySaved && !userSessionHolder.isPro.value) {
            val bookmarkCount = userSessionHolder.bookmarkedSpots.value.size
            if (bookmarkCount >= Constants.ContentLimits.FREE_BOOKMARK_CAP) {
                viewModelScope.launch {
                    _effects.send(MapEffect.ShowPaywall(entryPoint = "bookmark_cap"))
                }
                return
            }
        }

        updateSpot(spot.id) { it.copy(isSaved = !currentlySaved) }

        if (currentlySaved) {
            userSessionHolder.removeBookmark(spot.id)
        } else {
            userSessionHolder.addBookmark(spot.id)
        }

        viewModelScope.launch {
            val result = if (currentlySaved) {
                engagementRepository.unbookmarkSpot(spot.id)
            } else {
                engagementRepository.bookmarkSpot(spot.id)
            }

            result.fold(
                onSuccess = {
                    feedEventService.recordEvent(
                        spotId = spot.id,
                        eventType = if (currentlySaved) FeedEventType.UNSAVE else FeedEventType.SAVE,
                    )
                },
                onFailure = {
                    logger.w(LogCategory.Map, TAG, "Bookmark toggle failed; rolling back", it)
                    if (currentlySaved) {
                        userSessionHolder.addBookmark(spot.id)
                    } else {
                        userSessionHolder.removeBookmark(spot.id)
                    }
                    updateSpot(spot.id) { it.copy(isSaved = currentlySaved) }
                },
            )
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun observeSession() {
        viewModelScope.launch {
            combine(
                userSessionHolder.isPro,
                userSessionHolder.likedSpots,
                userSessionHolder.bookmarkedSpots,
                userSessionHolder.customVibeTags,
                userSessionHolder.currentUserProfileImageURL,
            ) { isPro, liked, bookmarked, customVibes, profileImageUrl ->
                SessionSnapshot(isPro, liked, bookmarked, customVibes, profileImageUrl)
            }.collect { snapshot ->
                val vibeNames = (Constants.VibeTags.DEFAULT_TAGS + snapshot.customVibes).distinct()
                _uiState.update {
                    it.copy(
                        isPro = snapshot.isPro,
                        availableVibeNames = vibeNames,
                        currentUserProfileImageUrl = snapshot.profileImageUrl,
                    )
                }
                rebuildVisiblePins(lastViewport)
            }
        }
    }

    private fun observeContentRemovals() {
        viewModelScope.launch {
            localContentRemovalBus.removals.collect { event ->
                when (event) {
                    is ContentRemovalEvent.ByAuthor -> removeSpotsByAuthor(event.authorUserId)
                    is ContentRemovalEvent.BySpotId -> removeSpot(event.spotId)
                }
            }
        }
    }

    private fun observeLocationUpdates() {
        viewModelScope.launch {
            var firstFix = true
            mapLocationTracker.locationUpdates.collect { location ->
                onUserLocationReceived(location, isFirstFix = firstFix)
                firstFix = false
            }
        }
    }

    private suspend fun fetchViewport(viewport: MapViewportBounds) {
        _uiState.update { it.copy(loadState = MapLoadState.LOADING) }

        val result = mapRepository.getMapSpots(
            minLat = viewport.minLat,
            minLng = viewport.minLng,
            maxLat = viewport.maxLat,
            maxLng = viewport.maxLng,
            centerLat = viewport.centerLat,
            centerLng = viewport.centerLng,
            limit = Constants.MapDesign.VISIBLE_SPOTS_CAP,
        )

        result.fold(
            onSuccess = { rows ->
                val hydrated = mapSpotHydrator.hydrateAll(
                    rows = rows,
                    likedSpotIds = userSessionHolder.likedSpots.value,
                    bookmarkedSpotIds = userSessionHolder.bookmarkedSpots.value,
                )
                val merged = MapPinLayout.mergeSpots(
                    existing = _uiState.value.allSpots,
                    incoming = hydrated,
                    centerLat = viewport.centerLat,
                    centerLng = viewport.centerLng,
                    cap = Constants.MapDesign.VISIBLE_SPOTS_CAP,
                )
                _uiState.update {
                    it.copy(
                        allSpots = merged,
                        loadState = MapLoadState.LOADED,
                        errorMessage = null,
                    )
                }
                rebuildVisiblePins(viewport)
                maybeDismissForSpotNoLongerVisible()
            },
            onFailure = {
                logger.w(LogCategory.Map, TAG, "Viewport fetch failed", it)
                _uiState.update {
                    it.copy(
                        loadState = if (it.allSpots.isEmpty()) MapLoadState.ERROR else MapLoadState.LOADED,
                        errorMessage = "Couldn't load spots",
                    )
                }
            },
        )
    }

    private fun rebuildVisiblePins(viewport: MapViewportBounds?) {
        val state = _uiState.value
        var spots = state.allSpots.values.toList()

        profileUserIdFilter?.let { userId ->
            spots = spots.filter { it.userId == userId }
        }

        if (state.isPro && state.activeFilters.isNotEmpty()) {
            spots = MapSpotFilterEngine.applyFilters(
                spots = spots,
                activeFilters = state.activeFilters,
                selectedVibeNames = state.selectedVibeNames,
                likedSpotIds = userSessionHolder.likedSpots.value,
                bookmarkedSpotIds = userSessionHolder.bookmarkedSpots.value,
                followedUserIds = followedUserIds,
            )
        }

        val isFarZoom = viewport?.isFarZoom == true
        spots = MapPinLayout.capForZoom(spots, isFarZoom)
        val pins = MapPinLayout.layoutPins(spots)

        _uiState.update { it.copy(pins = pins) }
    }

    private fun dismissDrawer(reason: MapDrawerDismissReason) {
        if (_uiState.value.drawerState == MapDrawerState.HIDDEN &&
            _uiState.value.selectedSpotId == null
        ) {
            return
        }
        logger.d(LogCategory.Map, TAG, "Drawer dismissed: $reason")
        _uiState.update {
            it.copy(
                selectedSpotId = null,
                drawerState = MapDrawerState.HIDDEN,
            )
        }
    }

    private fun maybeDismissForMapMoved(centerLat: Double, centerLng: Double) {
        val selectedId = _uiState.value.selectedSpotId ?: return
        val spot = _uiState.value.allSpots[selectedId] ?: return
        val distance = SphericalUtil.computeDistanceBetween(
            LatLng(centerLat, centerLng),
            LatLng(spot.latitude, spot.longitude),
        )
        if (distance > Constants.MapDesign.MAP_MOVED_DISMISS_THRESHOLD_METERS) {
            dismissDrawer(MapDrawerDismissReason.MAP_MOVED)
        }
    }

    private fun maybeDismissForFilterChange() {
        val selectedId = _uiState.value.selectedSpotId ?: return
        val spot = _uiState.value.allSpots[selectedId] ?: return
        val state = _uiState.value
        if (!MapSpotFilterEngine.isSpotVisibleAfterFilter(
                spot = spot,
                activeFilters = state.activeFilters,
                selectedVibeNames = state.selectedVibeNames,
                likedSpotIds = userSessionHolder.likedSpots.value,
                bookmarkedSpotIds = userSessionHolder.bookmarkedSpots.value,
                followedUserIds = followedUserIds,
            )
        ) {
            dismissDrawer(MapDrawerDismissReason.FILTER_CHANGED)
        }
    }

    private fun maybeDismissForSpotNoLongerVisible() {
        val selectedId = _uiState.value.selectedSpotId ?: return
        val stillVisible = _uiState.value.pins.any { it.spot.id == selectedId }
        if (!stillVisible) {
            dismissDrawer(MapDrawerDismissReason.SELECTED_SPOT_NO_LONGER_VISIBLE)
        }
    }

    private fun removeSpotsByAuthor(authorUserId: String) {
        _uiState.update { state ->
            val filtered = state.allSpots.filterValues { it.userId != authorUserId }
            state.copy(allSpots = filtered)
        }
        rebuildVisiblePins(lastViewport)
        val selected = _uiState.value.selectedSpotId
        if (selected != null && _uiState.value.allSpots[selected] == null) {
            dismissDrawer(MapDrawerDismissReason.SELECTED_SPOT_NO_LONGER_VISIBLE)
        }
    }

    private fun removeSpot(spotId: String) {
        _uiState.update { state ->
            state.copy(allSpots = state.allSpots - spotId)
        }
        if (_uiState.value.selectedSpotId == spotId) {
            dismissDrawer(MapDrawerDismissReason.SELECTED_SPOT_NO_LONGER_VISIBLE)
        }
        rebuildVisiblePins(lastViewport)
    }

    private fun updateSpot(spotId: String, transform: (Spot) -> Spot) {
        _uiState.update { state ->
            val spot = state.allSpots[spotId] ?: return@update state
            val updated = transform(spot)
            state.copy(allSpots = state.allSpots + (spotId to updated))
        }
        rebuildVisiblePins(lastViewport)
    }

    private data class SessionSnapshot(
        val isPro: Boolean,
        val liked: Set<String>,
        val bookmarked: Set<String>,
        val customVibes: List<String>,
        val profileImageUrl: String?,
    )

    private companion object {
        const val TAG = "MapViewModel"
    }
}
