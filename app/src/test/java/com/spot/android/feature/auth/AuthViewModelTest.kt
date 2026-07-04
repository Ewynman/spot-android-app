package com.spot.android.feature.auth

import com.spot.android.core.logging.FakeLogWriter
import com.spot.android.core.logging.SpotLogger
import com.spot.android.core.supabase.SessionBridge
import com.spot.android.core.supabase.SupabaseClientProvider
import com.spot.android.data.auth.AuthError
import com.spot.android.data.auth.AuthException
import com.spot.android.data.auth.FakeAuthRepository
import com.spot.android.data.auth.FakeUserSessionRepository
import com.spot.android.data.auth.UserSessionHolder
import com.spot.android.data.auth.UserSessionSnapshot
import com.spot.android.data.terms.FakeTermsRepository
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.user.UserInfo
import io.github.jan.supabase.gotrue.user.UserSession
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var fakeUserSessionRepository: FakeUserSessionRepository
    private lateinit var fakeTermsRepository: FakeTermsRepository
    private lateinit var userSessionHolder: UserSessionHolder
    private lateinit var mockAuth: Auth
    private lateinit var sessionBridge: SessionBridge
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        fakeAuthRepository = FakeAuthRepository()
        fakeUserSessionRepository = FakeUserSessionRepository()
        fakeTermsRepository = FakeTermsRepository()
        userSessionHolder = UserSessionHolder()

        mockAuth = mockk(relaxed = true)
        coEvery { mockAuth.currentSessionOrNull() } returns null

        val mockProvider = mockk<SupabaseClientProvider>(relaxed = true)
        every { mockProvider.auth } returns mockAuth

        sessionBridge = SessionBridge(mockProvider)

        val logger = SpotLogger(
            preferencesRepository = com.spot.android.core.logging.FakeLogPreferencesRepository(),
            logWriter = FakeLogWriter(),
        )

        viewModel = AuthViewModel(
            authRepository = fakeAuthRepository,
            userSessionRepository = fakeUserSessionRepository,
            termsRepository = fakeTermsRepository,
            sessionBridge = sessionBridge,
            userSessionHolder = userSessionHolder,
            logger = logger,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loading session shows loading state`() = runTest {
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.isResolvingLaunchGates)
        assertFalse(viewModel.uiState.value.isAuthenticated)
    }

    @Test
    fun `unauthenticated session routes to welcome state`() = runTest {
        coEvery { mockAuth.currentSessionOrNull() } returns null
        sessionBridge.refresh()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isResolvingLaunchGates)
        assertFalse(state.isAuthenticated)
        assertNull(state.userId)
    }

    @Test
    fun `pending verification shows confirm email gate`() = runTest {
        fakeAuthRepository.setPendingEmail("pending@example.com")
        coEvery { mockAuth.currentSessionOrNull() } returns null
        sessionBridge.refresh()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.awaitingEmailVerification)
        assertEquals("pending@example.com", state.pendingVerificationEmail)
    }

    @Test
    fun `authenticated verified user loads session snapshot`() = runTest {
        setAuthenticatedSession(userId = "user-123", email = "verified@example.com")
        fakeAuthRepository.emailVerified = true
        sessionBridge.refresh()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isAuthenticated)
        assertEquals("user-123", state.userId)
        assertTrue(state.isEmailVerified)
        assertFalse(state.isResolvingLaunchGates)
        assertEquals("testuser", state.currentUserUsername)
        assertEquals(setOf("spot-1"), state.likedSpots)
        assertEquals(setOf("spot-2"), state.bookmarkedSpots)
        assertEquals(setOf("user-blocked"), state.blockedUsers)
        assertEquals(1, fakeUserSessionRepository.loadCalls)
        assertEquals(1, fakeTermsRepository.checkCalls)
    }

    @Test
    fun `authenticated unverified user stays on confirm email gate`() = runTest {
        setAuthenticatedSession(userId = "user-456", email = "unverified@example.com")
        fakeAuthRepository.emailVerified = false
        sessionBridge.refresh()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isAuthenticated)
        assertTrue(state.awaitingEmailVerification)
        assertFalse(state.isEmailVerified)
        assertEquals(0, fakeUserSessionRepository.loadCalls)
    }

    @Test
    fun `oauth username gate detected from session snapshot`() = runTest {
        fakeUserSessionRepository.snapshot = Result.success(
            UserSessionSnapshot(
                username = "",
                profileImageURL = null,
                isPro = false,
                proUntil = null,
                emailVerified = true,
                likedSpots = emptySet(),
                bookmarkedSpots = emptySet(),
                blockedUsers = emptySet(),
                customVibeTags = emptyList(),
                needsUsernameSetup = true,
            ),
        )
        setAuthenticatedSession(userId = "oauth-user", email = "oauth@example.com")
        fakeAuthRepository.emailVerified = true
        sessionBridge.refresh()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.needsUsernameSetup)
    }

    @Test
    fun `sign up requiring verification sets awaiting email state`() = runTest {
        coEvery { mockAuth.currentSessionOrNull() } returns null
        sessionBridge.refresh()
        advanceUntilIdle()

        viewModel.signUpWithEmail(
            email = "new@example.com",
            password = "password123",
            username = "newuser",
            isPrivate = false,
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.awaitingEmailVerification)
        assertEquals("test@example.com", state.pendingVerificationEmail)
        assertEquals(1, fakeAuthRepository.signUpCalls)
    }

    @Test
    fun `sign in failure surfaces auth error`() = runTest {
        fakeAuthRepository.signInResult = Result.failure(
            AuthException(AuthError.InvalidCredentials),
        )
        coEvery { mockAuth.currentSessionOrNull() } returns null
        sessionBridge.refresh()
        advanceUntilIdle()

        viewModel.signInWithEmailOrUsername("user", "wrong")
        advanceUntilIdle()

        assertEquals(AuthError.InvalidCredentials, viewModel.uiState.value.authError)
    }

    @Test
    fun `terms not accepted sets needs terms acceptance`() = runTest {
        fakeTermsRepository.hasAcceptedResult = Result.success(false)
        setAuthenticatedSession(userId = "user-terms", email = "terms@example.com")
        fakeAuthRepository.emailVerified = true
        sessionBridge.refresh()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.needsTermsAcceptance)
    }

    @Test
    fun `terms check failure fails open`() = runTest {
        fakeTermsRepository.hasAcceptedResult = Result.failure(RuntimeException("network"))
        setAuthenticatedSession(userId = "user-failopen", email = "fail@example.com")
        fakeAuthRepository.emailVerified = true
        sessionBridge.refresh()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.needsTermsAcceptance)
    }

    @Test
    fun `sign out clears session state`() = runTest {
        setAuthenticatedSession(userId = "user-789", email = "out@example.com")
        fakeAuthRepository.emailVerified = true
        sessionBridge.refresh()
        advanceUntilIdle()

        viewModel.signOut()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isAuthenticated)
        assertNull(state.userId)
        assertEquals(1, fakeAuthRepository.signOutCalls)
        assertTrue(userSessionHolder.likedSpots.value.isEmpty())
    }

    private fun setAuthenticatedSession(userId: String, email: String) {
        val mockUser = mockk<UserInfo>(relaxed = true)
        every { mockUser.id } returns userId
        every { mockUser.email } returns email

        val mockSession = mockk<UserSession>(relaxed = true)
        every { mockSession.user } returns mockUser

        coEvery { mockAuth.currentSessionOrNull() } returns mockSession
    }
}
