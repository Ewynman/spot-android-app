package com.spot.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.spot.android.core.design.theme.SpotTheme
import com.spot.android.feature.launch.SpotAppRoot
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SpotTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    SpotAppRoot(tabReselectBus = tabReselectBus)
                }
            }
        }
    }
}
