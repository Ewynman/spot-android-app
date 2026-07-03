package com.spot.android.core.supabase

import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.user.UserInfo
import io.github.jan.supabase.gotrue.user.UserSession
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SessionBridge.
 * 
 * Tests session state management and synchronous user ID access.
 */
class SessionBridgeTest {
    
    private lateinit var mockSupabaseProvider: SupabaseClientProvider
    private lateinit var mockAuth: Auth
    private lateinit var sessionBridge: SessionBridge
    
    private val sessionStatusFlow = MutableStateFlow<SessionStatus>(
        SessionStatus.LoadingFromStorage
    )
    
    @Before
    fun setup() {
        mockAuth = mockk(relaxed = true)
        mockSupabaseProvider = mockk(relaxed = true)
        
        every { mockSupabaseProvider.auth } returns mockAuth
        every { mockAuth.sessionStatus } returns sessionStatusFlow
        
        sessionBridge = SessionBridge(mockSupabaseProvider)
    }
    
    @Test
    fun `initial state is Loading`() {
        assertEquals(SessionState.Loading, sessionBridge.sessionState.value)
        assertFalse(sessionBridge.isAuthenticated)
        assertNull(sessionBridge.currentUserId)
    }
    
    @Test
    fun `initial isAuthenticated is false`() {
        assertFalse(sessionBridge.isAuthenticated)
    }
    
    @Test
    fun `initial currentUserId is null`() {
        assertNull(sessionBridge.currentUserId)
    }
    
    @Test
    fun `refresh updates state from current session`() = runTest {
        val testUserId = "refresh-user-id"
        val testEmail = "refresh@example.com"
        
        val mockUserInfo = mockk<UserInfo>(relaxed = true)
        every { mockUserInfo.id } returns testUserId
        every { mockUserInfo.email } returns testEmail
        
        val mockSession = mockk<UserSession>(relaxed = true)
        every { mockSession.user } returns mockUserInfo
        
        coEvery { mockAuth.currentSessionOrNull() } returns mockSession
        
        // Call refresh
        sessionBridge.refresh()
        
        // Verify state
        val state = sessionBridge.sessionState.value
        assertTrue(state is SessionState.Authenticated)
        assertEquals(testUserId, (state as SessionState.Authenticated).userId)
        assertTrue(sessionBridge.isAuthenticated)
    }
    
    @Test
    fun `refresh with no session sets unauthenticated`() = runTest {
        coEvery { mockAuth.currentSessionOrNull() } returns null
        
        // Call refresh
        sessionBridge.refresh()
        
        // Verify state
        assertEquals(SessionState.Unauthenticated, sessionBridge.sessionState.value)
        assertFalse(sessionBridge.isAuthenticated)
        assertNull(sessionBridge.currentUserId)
    }
}
