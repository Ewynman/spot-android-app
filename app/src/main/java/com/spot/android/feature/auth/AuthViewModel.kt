package com.spot.android.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spot.android.core.logging.LogCategory
import com.spot.android.core.logging.SpotLogger
import com.spot.android.core.supabase.SessionBridge
import com.spot.android.core.supabase.SessionState
import com.spot.android.data.auth.AuthError
import com.spot.android.data.auth.AuthException
import com.spot.android.data.auth.AuthRepository
import com.spot.android.data.auth.SignUpResult
import com.spot.android.data.auth.UserSessionHolder
import com.spot.android.data.auth.UserSessionRepository
import com.spot.android.data.terms.TermsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Auth and session ViewModel publishing gating state to the UI.
 *
 * Reference: PRD/05-auth-onboarding.md
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userSessionRepository: UserSessionRepository,
    private val termsRepository: TermsRepository,
    private val sessionBridge: SessionBridge,
    private val userSessionHolder: UserSessionHolder,
    private val logger: SpotLogger,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private var loadedSessionUserId: String? = null

    init {
        observeSessionState()
        observeHolderState()
    }

    fun signUpWithEmail(
        email: String,
        password: String,
        username: String,
        isPrivate: Boolean,
    ) {
        viewModelScope.launch {
            setLoading(true)
            authRepository.signUpWithEmail(email, password, username, isPrivate)
                .onSuccess { result -> handleSignUpSuccess(result) }
                .onFailure { error -> setAuthError(error.toAuthError()) }
            setLoading(false)
        }
    }

    fun signInWithEmailOrUsername(identifier: String, password: String) {
        viewModelScope.launch {
            setLoading(true)
            authRepository.signInWithEmailOrUsername(identifier, password)
                .onSuccess {
                    clearAuthError()
                    refreshSessionSnapshot(force = true)
                }
                .onFailure { error -> setAuthError(error.toAuthError()) }
            setLoading(false)
        }
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            setLoading(true)
            authRepository.signInWithGoogle()
                .onFailure { error -> setAuthError(error.toAuthError()) }
            setLoading(false)
        }
    }

    fun handleOAuthCallback(url: String) {
        viewModelScope.launch {
            setLoading(true)
            authRepository.handleOAuthCallback(url)
                .onSuccess {
                    clearAuthError()
                    refreshSessionSnapshot(force = true)
                }
                .onFailure { error -> setAuthError(error.toAuthError()) }
            setLoading(false)
        }
    }

    fun verifyEmailOtp(email: String, token: String) {
        viewModelScope.launch {
            setLoading(true)
            authRepository.verifyEmailOtp(email, token)
                .onSuccess {
                    clearAuthError()
                    refreshSessionSnapshot(force = true)
                }
                .onFailure { error -> setAuthError(error.toAuthError()) }
            setLoading(false)
        }
    }

    fun resendEmailOtp(email: String) {
        viewModelScope.launch {
            authRepository.resendEmailOtp(email)
                .onFailure { error -> setAuthError(error.toAuthError()) }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            authRepository.resetPassword(email)
                .onSuccess { clearAuthError() }
                .onFailure { error -> setAuthError(error.toAuthError()) }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            loadedSessionUserId = null
            userSessionHolder.clear()
            _uiState.value = AuthUiState(isLoading = false, isResolvingLaunchGates = false)
        }
    }

    fun clearPendingVerification() {
        viewModelScope.launch {
            authRepository.clearPendingVerification()
        }
    }

    fun refreshAuthGates() {
        viewModelScope.launch {
            val userId = sessionBridge.currentUserId ?: return@launch
            if (!authRepository.isCurrentEmailVerified()) return@launch
            refreshLaunchGates(userId)
        }
    }

    fun clearAuthError() {
        _uiState.update { it.copy(authError = null) }
    }

    fun refreshSessionSnapshot(force: Boolean = false) {
        viewModelScope.launch {
            val userId = sessionBridge.currentUserId ?: return@launch
            if (!force && loadedSessionUserId == userId) return@launch
            loadSessionSnapshot(userId)
        }
    }

    private fun observeSessionState() {
        viewModelScope.launch {
            combine(
                sessionBridge.sessionState,
                authRepository.pendingVerificationEmail,
            ) { sessionState, pendingEmail ->
                sessionState to pendingEmail
            }.collect { (sessionState, pendingEmail) ->
                when (sessionState) {
                    SessionState.Loading -> {
                        _uiState.update {
                            it.copy(
                                isLoading = true,
                                isAuthenticated = false,
                                isResolvingLaunchGates = true,
                            )
                        }
                    }

                    SessionState.Unauthenticated -> {
                        loadedSessionUserId = null
                        userSessionHolder.clear()
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isAuthenticated = false,
                                userId = null,
                                isEmailVerified = false,
                                awaitingEmailVerification = pendingEmail != null,
                                pendingVerificationEmail = pendingEmail,
                                likedSpots = emptySet(),
                                bookmarkedSpots = emptySet(),
                                blockedUsers = emptySet(),
                                isPro = false,
                                proUntil = null,
                                customVibeTags = emptyList(),
                                currentUserProfileImageURL = null,
                                currentUserUsername = null,
                                needsUsernameSetup = false,
                                needsTermsAcceptance = false,
                                isResolvingLaunchGates = false,
                            )
                        }
                    }

                    is SessionState.Authenticated -> {
                        val emailVerified = authRepository.isCurrentEmailVerified()
                        val awaitingVerification = pendingEmail != null || !emailVerified
                        val needsGateResolution = !awaitingVerification &&
                            loadedSessionUserId != sessionState.userId
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isAuthenticated = !awaitingVerification,
                                userId = sessionState.userId,
                                isEmailVerified = emailVerified,
                                awaitingEmailVerification = awaitingVerification,
                                pendingVerificationEmail = pendingEmail,
                                isResolvingLaunchGates = if (awaitingVerification) {
                                    false
                                } else {
                                    needsGateResolution
                                },
                            )
                        }
                        if (!awaitingVerification && loadedSessionUserId != sessionState.userId) {
                            loadSessionSnapshot(sessionState.userId)
                        }
                    }
                }
            }
        }
    }

    private fun observeHolderState() {
        viewModelScope.launch {
            val engagementFlow = combine(
                userSessionHolder.likedSpots,
                userSessionHolder.bookmarkedSpots,
                userSessionHolder.blockedUsers,
            ) { liked, bookmarked, blocked ->
                Triple(liked, bookmarked, blocked)
            }

            val proFlow = combine(
                userSessionHolder.isPro,
                userSessionHolder.proUntil,
                userSessionHolder.customVibeTags,
            ) { isPro, proUntil, customVibes ->
                Triple(isPro, proUntil, customVibes)
            }

            val profileFlow = combine(
                userSessionHolder.currentUserUsername,
                userSessionHolder.currentUserProfileImageURL,
            ) { username, profileImage ->
                username to profileImage
            }

            combine(engagementFlow, proFlow, profileFlow) { engagement, proData, profile ->
                HolderSnapshot(
                    likedSpots = engagement.first,
                    bookmarkedSpots = engagement.second,
                    blockedUsers = engagement.third,
                    isPro = proData.first,
                    proUntil = proData.second,
                    customVibeTags = proData.third,
                    username = profile.first,
                    profileImageURL = profile.second,
                )
            }.collect { holder ->
                _uiState.update {
                    it.copy(
                        likedSpots = holder.likedSpots,
                        bookmarkedSpots = holder.bookmarkedSpots,
                        blockedUsers = holder.blockedUsers,
                        isPro = holder.isPro,
                        proUntil = holder.proUntil,
                        customVibeTags = holder.customVibeTags,
                        currentUserUsername = holder.username,
                        currentUserProfileImageURL = holder.profileImageURL,
                    )
                }
            }
        }
    }

    private suspend fun loadSessionSnapshot(userId: String) {
        _uiState.update { it.copy(isResolvingLaunchGates = true) }

        userSessionRepository.loadSessionSnapshot(userId)
            .onSuccess { snapshot ->
                loadedSessionUserId = userId
                userSessionHolder.updateFromSnapshot(snapshot)
                val needsTerms = resolveTermsAcceptance()
                _uiState.update {
                    it.copy(
                        needsUsernameSetup = snapshot.needsUsernameSetup,
                        isEmailVerified = snapshot.emailVerified,
                        isAuthenticated = snapshot.emailVerified && !it.awaitingEmailVerification,
                        needsTermsAcceptance = needsTerms,
                        isResolvingLaunchGates = false,
                    )
                }
            }
            .onFailure { error ->
                logger.e(LogCategory.Auth, TAG, "Failed to refresh session snapshot", error)
                _uiState.update { it.copy(isResolvingLaunchGates = false) }
            }
    }

    private suspend fun refreshLaunchGates(userId: String) {
        userSessionRepository.loadSessionSnapshot(userId)
            .onSuccess { snapshot ->
                loadedSessionUserId = userId
                userSessionHolder.updateFromSnapshot(snapshot)
                val needsTerms = resolveTermsAcceptance()
                _uiState.update {
                    it.copy(
                        needsUsernameSetup = snapshot.needsUsernameSetup,
                        needsTermsAcceptance = needsTerms,
                    )
                }
            }
            .onFailure { error ->
                logger.e(LogCategory.Auth, TAG, "Failed to refresh auth gates", error)
            }
    }

    /**
     * Fail-open on network error — do not lock users out of the app.
     */
    private suspend fun resolveTermsAcceptance(): Boolean {
        return termsRepository.hasAcceptedActiveTerms()
            .fold(
                onSuccess = { accepted -> !accepted },
                onFailure = {
                    logger.w(LogCategory.Auth, TAG, "Terms check failed; failing open")
                    false
                },
            )
    }

    private suspend fun handleSignUpSuccess(result: SignUpResult) {
        clearAuthError()
        if (result.requiresEmailVerification) {
            _uiState.update {
                it.copy(
                    awaitingEmailVerification = true,
                    pendingVerificationEmail = result.email,
                    isAuthenticated = false,
                )
            }
        } else {
            refreshSessionSnapshot(force = true)
        }
    }

    private fun setLoading(loading: Boolean) {
        _uiState.update { it.copy(isLoading = loading) }
    }

    private fun setAuthError(error: AuthError) {
        _uiState.update { it.copy(authError = error) }
    }

    private fun Throwable.toAuthError(): AuthError {
        return (this as? AuthException)?.authError ?: AuthError.Generic(message.orEmpty())
    }

    private data class HolderSnapshot(
        val likedSpots: Set<String>,
        val bookmarkedSpots: Set<String>,
        val blockedUsers: Set<String>,
        val isPro: Boolean,
        val proUntil: Long?,
        val customVibeTags: List<String>,
        val username: String?,
        val profileImageURL: String?,
    )

    private companion object {
        const val TAG = "AuthViewModel"
    }
}
