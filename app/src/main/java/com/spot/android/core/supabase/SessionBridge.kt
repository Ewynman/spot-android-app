package com.spot.android.core.supabase

import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Synchronous bridge to current Supabase session state.
 * 
 * Provides immediate access to current user ID and auth state for gates
 * (deep links, publish flow, etc.) that need synchronous checks.
 * 
 * This mirrors iOS `SpotAuthBridge` pattern.
 * 
 * Usage:
 * ```kotlin
 * if (sessionBridge.isAuthenticated) {
 *     val userId = sessionBridge.currentUserId
 *     // proceed
 * }
 * ```
 * 
 * Reference: PRD/01-architecture-android.md
 */
@Singleton
class SessionBridge @Inject constructor(
    private val supabaseProvider: SupabaseClientProvider
) {
    
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Loading)
    
    /**
     * Observable session state flow.
     */
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()
    
    /**
     * Current user ID if authenticated, null otherwise.
     * This is a synchronous accessor for immediate checks.
     */
    val currentUserId: String?
        get() = when (val state = _sessionState.value) {
            is SessionState.Authenticated -> state.userId
            else -> null
        }
    
    /**
     * Quick check if user is authenticated.
     */
    val isAuthenticated: Boolean
        get() = _sessionState.value is SessionState.Authenticated
    
    /**
     * Initialize session observation.
     * Should be called from Application.onCreate().
     */
    suspend fun initialize() {
        // Observe Supabase auth session status
        supabaseProvider.auth.sessionStatus.collect { status ->
            _sessionState.value = when (status) {
                is SessionStatus.Authenticated -> {
                    SessionState.Authenticated(
                        userId = status.session.user?.id ?: "",
                        email = status.session.user?.email
                    )
                }
                is SessionStatus.NotAuthenticated -> {
                    SessionState.Unauthenticated
                }
                is SessionStatus.LoadingFromStorage -> {
                    SessionState.Loading
                }
                is SessionStatus.NetworkError -> {
                    // Keep current state on network error
                    _sessionState.value
                }
            }
        }
    }
    
    /**
     * Force refresh of session state.
     * Useful after login/logout.
     */
    suspend fun refresh() {
        val session = supabaseProvider.auth.currentSessionOrNull()
        _sessionState.value = if (session != null) {
            SessionState.Authenticated(
                userId = session.user?.id ?: "",
                email = session.user?.email
            )
        } else {
            SessionState.Unauthenticated
        }
    }
}

/**
 * Sealed class representing session states.
 */
sealed class SessionState {
    /**
     * Session is loading from storage.
     */
    data object Loading : SessionState()
    
    /**
     * User is authenticated.
     */
    data class Authenticated(
        val userId: String,
        val email: String?
    ) : SessionState()
    
    /**
     * User is not authenticated.
     */
    data object Unauthenticated : SessionState()
}
