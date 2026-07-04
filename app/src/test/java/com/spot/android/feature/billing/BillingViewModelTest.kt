package com.spot.android.feature.billing

import app.cash.turbine.test
import com.spot.android.core.logging.SpotLogger
import com.spot.android.core.supabase.SessionBridge
import com.spot.android.data.auth.UserSessionHolder
import com.spot.android.data.billing.BillingRepository
import com.spot.android.data.billing.BillingResult
import com.spot.android.data.billing.BillingState
import com.spot.android.data.billing.ProOnboardingPreferences
import com.spot.android.data.billing.ProProductDetails
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for BillingViewModel.
 *
 * Reference: PRD/12-pro-subscription.md, PRD/17-non-functional-testing.md
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BillingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var billingRepository: BillingRepository
    private lateinit var sessionBridge: SessionBridge
    private lateinit var userSessionHolder: UserSessionHolder
    private lateinit var proOnboardingPreferences: ProOnboardingPreferences
    private lateinit var logger: SpotLogger
    private lateinit var viewModel: BillingViewModel

    private val testUserId = "test-user-id"
    private val testProductDetails = ProProductDetails(
        productId = "spot_pro_yearly",
        title = "Spot Pro",
        description = "Annual subscription",
        formattedPrice = "$49.99",
        priceAmountMicros = 49990000L,
        priceCurrencyCode = "USD",
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        billingRepository = mockk(relaxed = true)
        sessionBridge = mockk(relaxed = true)
        userSessionHolder = mockk(relaxed = true)
        proOnboardingPreferences = mockk(relaxed = true)
        logger = mockk(relaxed = true)

        every { billingRepository.state } returns MutableStateFlow(BillingState.IDLE)
        every { billingRepository.productDetails } returns MutableStateFlow(null)
        every { billingRepository.errorMessage } returns MutableStateFlow(null)
        every { userSessionHolder.isPro } returns MutableStateFlow(false)
        every { sessionBridge.currentUserId } returns testUserId

        coEvery { billingRepository.initialize() } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): BillingViewModel {
        return BillingViewModel(
            billingRepository = billingRepository,
            sessionBridge = sessionBridge,
            userSessionHolder = userSessionHolder,
            proOnboardingPreferences = proOnboardingPreferences,
            logger = logger,
        )
    }

    @Test
    fun `init initializes billing repository`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        coVerify { billingRepository.initialize() }
    }

    @Test
    fun `purchasePro successful shows onboarding for first purchase`() = runTest {
        val activity = mockk<android.app.Activity>(relaxed = true)
        
        every { billingRepository.state } returns MutableStateFlow(BillingState.READY)
        every { billingRepository.productDetails } returns MutableStateFlow(testProductDetails)
        coEvery { billingRepository.purchasePro(activity, testUserId) } returns BillingResult.Success
        coEvery { proOnboardingPreferences.hasSeenOnboarding(testUserId) } returns flowOf(false)
        coEvery { proOnboardingPreferences.setOnboardingSeen(testUserId) } returns Unit

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.purchasePro(activity, "test_entry")
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is BillingEffect.ShowProOnboarding)
        }

        coVerify { proOnboardingPreferences.setOnboardingSeen(testUserId) }
    }

    @Test
    fun `purchasePro successful shows success for repeat purchase`() = runTest {
        val activity = mockk<android.app.Activity>(relaxed = true)
        
        every { billingRepository.state } returns MutableStateFlow(BillingState.READY)
        every { billingRepository.productDetails } returns MutableStateFlow(testProductDetails)
        coEvery { billingRepository.purchasePro(activity, testUserId) } returns BillingResult.Success
        coEvery { proOnboardingPreferences.hasSeenOnboarding(testUserId) } returns flowOf(true)

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.purchasePro(activity, "test_entry")
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is BillingEffect.ShowProSuccess)
        }
    }

    @Test
    fun `purchasePro canceled does not show effects`() = runTest {
        val activity = mockk<android.app.Activity>(relaxed = true)
        
        every { billingRepository.state } returns MutableStateFlow(BillingState.READY)
        coEvery { billingRepository.purchasePro(activity, testUserId) } returns BillingResult.Canceled

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.purchasePro(activity, "test_entry")
            advanceUntilIdle()

            expectNoEvents()
        }
    }

    @Test
    fun `purchasePro error sets error message`() = runTest {
        val activity = mockk<android.app.Activity>(relaxed = true)
        val errorMsg = "Purchase failed"
        
        every { billingRepository.state } returns MutableStateFlow(BillingState.READY)
        coEvery { billingRepository.purchasePro(activity, testUserId) } returns BillingResult.Error(errorMsg)

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.purchasePro(activity, "test_entry")
        advanceUntilIdle()

        assertEquals(errorMsg, viewModel.uiState.value.userErrorMessage)
    }

    @Test
    fun `restorePurchases successful shows success effect`() = runTest {
        every { billingRepository.state } returns MutableStateFlow(BillingState.READY)
        coEvery { billingRepository.restorePurchases(testUserId) } returns BillingResult.Success

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.restorePurchases()
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is BillingEffect.ShowRestoreSuccess)
        }
    }

    @Test
    fun `restorePurchases error sets error message`() = runTest {
        val errorMsg = "No purchases found"
        
        every { billingRepository.state } returns MutableStateFlow(BillingState.READY)
        coEvery { billingRepository.restorePurchases(testUserId) } returns BillingResult.Error(errorMsg)

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.restorePurchases()
        advanceUntilIdle()

        assertEquals(errorMsg, viewModel.uiState.value.userErrorMessage)
    }

    @Test
    fun `retryLoadProducts calls initialize`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        coVerify(exactly = 1) { billingRepository.initialize() }

        viewModel.retryLoadProducts()
        advanceUntilIdle()

        coVerify(exactly = 2) { billingRepository.initialize() }
    }

    // Note: onCleared() is protected and cannot be tested directly
    // It's called automatically by the Android framework when the ViewModel is destroyed
}
