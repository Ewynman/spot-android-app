package com.spot.android

import android.app.Application
import android.util.Log
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
 * logging, and analytics.
 */
@HiltAndroidApp
class SpotApplication : Application() {
    
    @Inject
    lateinit var sessionBridge: SessionBridge
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Supabase session observation
        initializeSessionBridge()
        
        // TODO: Initialize Firebase Analytics
        // TODO: Initialize Firebase Crashlytics
        // TODO: Initialize structured logger
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
