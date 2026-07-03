package com.spot.android

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.spot.android.core.analytics.AnalyticsTracker
import com.spot.android.core.logging.LogCategory
import com.spot.android.core.logging.SpotLogger
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

    @Inject
    lateinit var spotLogger: SpotLogger

    @Inject
    lateinit var analyticsTracker: AnalyticsTracker

    @Inject
    lateinit var crashlytics: FirebaseCrashlytics
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onCreate() {
        super.onCreate()
        
        initializeSessionBridge()
        initializeLoggingAndAnalytics()
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
                spotLogger.d(LogCategory.Auth, TAG, "SessionBridge initialized successfully")
            } catch (e: Exception) {
                spotLogger.e(LogCategory.Auth, TAG, "Failed to initialize SessionBridge", e)
                crashlytics.recordException(e)
            }
        }
    }

    private fun initializeLoggingAndAnalytics() {
        crashlytics.setCrashlyticsCollectionEnabled(true)
        spotLogger.d(LogCategory.Network, TAG, "Structured logger and analytics initialized")
        analyticsTracker.logEvent("app_start")
    }
    
    companion object {
        private const val TAG = "SpotApplication"
    }
}
