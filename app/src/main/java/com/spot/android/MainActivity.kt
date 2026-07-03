package com.spot.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.spot.android.core.design.theme.SpotTheme
import com.spot.android.navigation.OverlayHostViewModel
import com.spot.android.navigation.SpotShell
import com.spot.android.navigation.TabReselectBus
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Main entry point for the Spot application.
 *
 * Hosts the 5-tab navigation shell and top-level overlay layer.
 * Launch gate / auth routing will replace direct shell access in Phase 2.2.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tabReselectBus: TabReselectBus

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SpotTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val overlayViewModel: OverlayHostViewModel = hiltViewModel()
                    SpotShell(
                        tabReselectBus = tabReselectBus,
                        overlayViewModel = overlayViewModel,
                    )
                }
            }
        }
    }
}
