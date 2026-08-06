package com.spot.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.spot.android.core.analytics.DeepLinkOrigin
import com.spot.android.core.design.theme.SpotTheme
import com.spot.android.data.deeplink.DeepLinkCoordinator
import com.spot.android.data.notifications.SpotNotificationService
import com.spot.android.feature.auth.AuthViewModel
import com.spot.android.feature.launch.SpotAppRoot
import com.spot.android.navigation.OverlayHostViewModel
import com.spot.android.navigation.ProfileNavigationBus
import com.spot.android.navigation.ShellNavigationBus
import com.spot.android.navigation.SpotTab
import com.spot.android.navigation.TabReselectBus
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

/**
 * Main entry point for the Spot application.
 *
 * Hosts the launch gate and routes to auth gates or the 5-tab shell.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tabReselectBus: TabReselectBus

    @Inject
    lateinit var shellNavigationBus: ShellNavigationBus

    @Inject
    lateinit var profileNavigationBus: ProfileNavigationBus

    @Inject
    lateinit var deepLinkCoordinator: DeepLinkCoordinator

    private val authViewModel: AuthViewModel by viewModels()
    private val overlayViewModel: OverlayHostViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIncomingIntent(intent)

        setContent {
            SpotTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
                    LaunchedEffect(authState.isAuthenticated) {
                        if (authState.isAuthenticated) {
                            deepLinkCoordinator.processPending(overlayViewModel)
                        }
                    }

                    SpotAppRoot(
                        tabReselectBus = tabReselectBus,
                        shellNavigationBus = shellNavigationBus,
                        profileNavigationBus = profileNavigationBus,
                        authViewModel = authViewModel,
                        overlayViewModel = overlayViewModel,
                        deepLinkCoordinator = deepLinkCoordinator,
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        handleNotificationExtras(intent)
        val url = intent.data?.toString() ?: return
        if (url.contains("auth-callback")) {
            authViewModel.handleOAuthCallback(url)
            return
        }
        val origin = if (url.startsWith("http")) DeepLinkOrigin.AppLink else DeepLinkOrigin.CustomScheme
        lifecycleScope.launch {
            deepLinkCoordinator.handleIncomingUri(url, origin, overlayViewModel)
        }
    }

    private fun handleNotificationExtras(intent: Intent) {
        val type = intent.getStringExtra(SpotNotificationService.EXTRA_NOTIFICATION_TYPE) ?: return
        when (type) {
            SpotNotificationService.TYPE_FOLLOW_ACCEPTED -> {
                val userId = intent.getStringExtra(SpotNotificationService.EXTRA_USER_ID)
                shellNavigationBus.navigateToTab(SpotTab.Profile)
                if (!userId.isNullOrBlank()) {
                    profileNavigationBus.openProfile(userId)
                }
            }
        }
    }
}
