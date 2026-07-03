package com.spot.android

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for Spot.
 * 
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 * This will be the entry point for initializing application-wide services
 * and dependencies such as Supabase client, logging, and analytics.
 */
@HiltAndroidApp
class SpotApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // TODO: Initialize Firebase Analytics
        // TODO: Initialize Firebase Crashlytics
        // TODO: Initialize structured logger
        // TODO: Verify Supabase configuration on debug builds
    }
}
