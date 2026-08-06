package com.spot.android.data.deeplink

import com.spot.android.core.analytics.AnalyticsTracker
import com.spot.android.core.analytics.DeepLinkOrigin
import com.spot.android.core.logging.LogCategory
import com.spot.android.core.logging.SpotLogger
import com.spot.android.core.supabase.SessionBridge
import com.spot.android.data.auth.UserSessionHolder
import com.spot.android.data.model.Spot
import com.spot.android.navigation.AppOverlay
import com.spot.android.navigation.OverlayHostViewModel
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Coordinates deep-link handling across cold/warm start and auth gates.
 */
@Singleton
class DeepLinkCoordinator @Inject constructor(
    private val pendingStore: PendingDeepLinkStore,
    private val spotDetailRepository: SpotDetailRepository,
    private val sessionBridge: SessionBridge,
    private val userSessionHolder: UserSessionHolder,
    private val analyticsTracker: AnalyticsTracker,
    private val logger: SpotLogger,
) {
    private val mutex = Mutex()
    private var lastSpotId: String? = null
    private var lastSpotAtMs: Long = 0L

    private val _loadedSpot = MutableStateFlow<Spot?>(null)
    val loadedSpot: StateFlow<Spot?> = _loadedSpot.asStateFlow()

    suspend fun handleIncomingUri(
        uriString: String,
        origin: DeepLinkOrigin,
        overlayViewModel: OverlayHostViewModel,
    ) {
        val route = DeepLinkRouter.parse(uriString)
        analyticsTracker.trackDeepLink(
            origin = origin,
            route = when (route) {
                is DeepLinkRoute.SpotDetail -> "spotDetail"
                DeepLinkRoute.SubscriptionReturn -> "subscriptionReturn"
                DeepLinkRoute.Unknown -> "unknown"
            },
        )

        when (route) {
            is DeepLinkRoute.SpotDetail -> {
                if (sessionBridge.currentUserId == null) {
                    pendingStore.setPending(uriString)
                    return
                }
                openSpot(route.spotId, overlayViewModel)
            }
            DeepLinkRoute.SubscriptionReturn -> {
                if (userSessionHolder.isPro.value) {
                    overlayViewModel.showProSuccess()
                }
            }
            DeepLinkRoute.Unknown -> {
                logger.w(LogCategory.DeepLink, TAG, "Unknown deep link: $uriString")
            }
        }
    }

    suspend fun processPending(overlayViewModel: OverlayHostViewModel) {
        if (sessionBridge.currentUserId == null) return
        val pending = pendingStore.consumePending() ?: return
        val origin = if (pending.startsWith("http")) DeepLinkOrigin.AppLink else DeepLinkOrigin.CustomScheme
        handleIncomingUri(pending, origin, overlayViewModel)
    }

    suspend fun clearOnLogout() {
        pendingStore.clear()
        _loadedSpot.value = null
    }

    private suspend fun openSpot(spotId: String, overlayViewModel: OverlayHostViewModel) {
        mutex.withLock {
            val now = System.currentTimeMillis()
            if (spotId == lastSpotId && now - lastSpotAtMs < 1000L) return
            lastSpotId = spotId
            lastSpotAtMs = now
        }

        overlayViewModel.showSpotLoading(spotId)
        spotDetailRepository.fetchSpotById(spotId).fold(
            onSuccess = { spot ->
                _loadedSpot.value = spot
                overlayViewModel.replaceOverlay(AppOverlay.SpotDetail(spotId))
            },
            onFailure = { error ->
                logger.w(LogCategory.DeepLink, TAG, "Failed to load deep-linked spot", error)
                _loadedSpot.value = null
                overlayViewModel.showSpotUnavailable(spotId)
            },
        )
    }

    companion object {
        private const val TAG = "DeepLinkCoordinator"
    }
}
