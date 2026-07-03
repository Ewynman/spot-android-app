package com.spot.android

import android.app.Application
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.spot.android.core.supabase.SessionBridge
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Application class for Spot.
 * 
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 * Initializes application-wide services: Supabase session management,
 * logging, analytics, and Coil image loading.
 */
@HiltAndroidApp
class SpotApplication : Application(), ImageLoaderFactory {
    
    @Inject
    lateinit var sessionBridge: SessionBridge
    
    @Inject
    lateinit var imageLoader: ImageLoader
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Supabase session observation
        initializeSessionBridge()
        
        // TODO: Initialize Firebase Analytics
        // TODO: Initialize Firebase Crashlytics
        // TODO: Initialize structured logger
    }
    
    /**
     * Provides the singleton ImageLoader with Spot-specific configuration.
     * This is called by Coil to get the ImageLoader instance.
     */
    override fun newImageLoader(): ImageLoader {
        return imageLoader
    }
    
    private fun initializeSessionBridge() {
        applicationScope.launch {
            try {
                sessionBridge.initialize()
                Log.d(TAG, "SessionBridge initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize SessionBridge", e)
                // Non-fatal: app can continue, auth will fail gracefully
            }
        }
    }
    
    companion object {
        private const val TAG = "SpotApplication"
    }
}
