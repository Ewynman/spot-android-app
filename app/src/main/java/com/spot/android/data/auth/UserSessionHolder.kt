package com.spot.android.data.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Shared holder for the current user's engagement and entitlement sets.
 *
 * Feed, map, search, and profile read from here so optimistic updates stay consistent.
 *
 * Reference: PRD/05-auth-onboarding.md, PRD/10-profile-social.md
 */
@Singleton
class UserSessionHolder @Inject constructor() {

    private val _likedSpots = MutableStateFlow<Set<String>>(emptySet())
    val likedSpots: StateFlow<Set<String>> = _likedSpots.asStateFlow()

    private val _bookmarkedSpots = MutableStateFlow<Set<String>>(emptySet())
    val bookmarkedSpots: StateFlow<Set<String>> = _bookmarkedSpots.asStateFlow()

    private val _blockedUsers = MutableStateFlow<Set<String>>(emptySet())
    val blockedUsers: StateFlow<Set<String>> = _blockedUsers.asStateFlow()

    private val _isPro = MutableStateFlow(false)
    val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    private val _proUntil = MutableStateFlow<Long?>(null)
    val proUntil: StateFlow<Long?> = _proUntil.asStateFlow()

    private val _customVibeTags = MutableStateFlow<List<String>>(emptyList())
    val customVibeTags: StateFlow<List<String>> = _customVibeTags.asStateFlow()

    private val _currentUserUsername = MutableStateFlow<String?>(null)
    val currentUserUsername: StateFlow<String?> = _currentUserUsername.asStateFlow()

    private val _currentUserProfileImageURL = MutableStateFlow<String?>(null)
    val currentUserProfileImageURL: StateFlow<String?> = _currentUserProfileImageURL.asStateFlow()

    fun updateFromSnapshot(snapshot: UserSessionSnapshot) {
        _likedSpots.value = snapshot.likedSpots
        _bookmarkedSpots.value = snapshot.bookmarkedSpots
        _blockedUsers.value = snapshot.blockedUsers
        _isPro.value = snapshot.isPro
        _proUntil.value = snapshot.proUntil
        _customVibeTags.value = snapshot.customVibeTags
        _currentUserUsername.value = snapshot.username
        _currentUserProfileImageURL.value = snapshot.profileImageURL
    }

    fun clear() {
        _likedSpots.value = emptySet()
        _bookmarkedSpots.value = emptySet()
        _blockedUsers.value = emptySet()
        _isPro.value = false
        _proUntil.value = null
        _customVibeTags.value = emptyList()
        _currentUserUsername.value = null
        _currentUserProfileImageURL.value = null
    }

    fun addLike(spotId: String) {
        _likedSpots.update { it + spotId }
    }

    fun removeLike(spotId: String) {
        _likedSpots.update { it - spotId }
    }

    fun addBookmark(spotId: String) {
        _bookmarkedSpots.update { it + spotId }
    }

    fun removeBookmark(spotId: String) {
        _bookmarkedSpots.update { it - spotId }
    }

    fun addBlockedUser(userId: String) {
        _blockedUsers.update { it + userId }
    }

    fun removeBlockedUser(userId: String) {
        _blockedUsers.update { it - userId }
    }

    fun updateProStatus(isPro: Boolean, proUntil: Long?) {
        _isPro.value = isPro
        _proUntil.value = proUntil
    }
}
