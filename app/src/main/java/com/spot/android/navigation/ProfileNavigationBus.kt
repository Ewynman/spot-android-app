package com.spot.android.navigation

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Opens another user's profile as a shell-level overlay from feed/map/search.
 */
@Singleton
class ProfileNavigationBus @Inject constructor() {

    private val _activeUserId = MutableStateFlow<String?>(null)
    val activeUserId: StateFlow<String?> = _activeUserId.asStateFlow()

    fun openProfile(userId: String) {
        _activeUserId.value = userId
    }

    fun closeProfile() {
        _activeUserId.value = null
    }
}
