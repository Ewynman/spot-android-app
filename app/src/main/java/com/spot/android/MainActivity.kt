package com.spot.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.spot.android.core.design.DesignSystemPreviewScreen
import com.spot.android.core.design.theme.SpotTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main entry point for the Spot application.
 * 
 * This activity will host the navigation graph and manage the app's lifecycle.
 * Currently displays the Design System Preview Screen for validation.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            SpotTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // TODO: Replace with navigation shell once task 1.6 is complete
                    DesignSystemPreviewScreen()
                }
            }
        }
    }
}

