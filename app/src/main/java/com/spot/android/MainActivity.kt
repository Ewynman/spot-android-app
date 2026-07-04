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
import androidx.compose.ui.Modifier
import com.spot.android.core.design.theme.SpotTheme
import com.spot.android.feature.auth.AuthViewModel
import com.spot.android.feature.launch.SpotAppRoot
import com.spot.android.navigation.ShellNavigationBus
import com.spot.android.navigation.TabReselectBus
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

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

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleAuthCallback(intent)

        setContent {
            SpotTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    SpotAppRoot(
                        tabReselectBus = tabReselectBus,
                        shellNavigationBus = shellNavigationBus,
                        authViewModel = authViewModel,
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAuthCallback(intent)
    }

    private fun handleAuthCallback(intent: Intent?) {
        val url = intent?.data?.toString() ?: return
        if (url.contains("auth-callback")) {
            authViewModel.handleOAuthCallback(url)
        }
    }
}
