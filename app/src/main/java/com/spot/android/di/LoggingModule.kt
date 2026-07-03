package com.spot.android.di

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.spot.android.core.analytics.AnalyticsBundleFactory
import com.spot.android.core.analytics.AnalyticsTracker
import com.spot.android.core.analytics.AndroidAnalyticsBundleFactory
import com.spot.android.core.analytics.FirebaseAnalyticsTracker
import com.spot.android.core.logging.AndroidLogWriter
import com.spot.android.core.logging.DataStoreLogPreferencesRepository
import com.spot.android.core.logging.LogPreferencesRepository
import com.spot.android.core.logging.LogWriter
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for structured logging and analytics dependencies.
 *
 * Reference: PRD/17-non-functional-testing.md, PRD/11-settings.md
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class LoggingModule {

    @Binds
    @Singleton
    abstract fun bindLogPreferencesRepository(
        impl: DataStoreLogPreferencesRepository,
    ): LogPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindLogWriter(
        impl: AndroidLogWriter,
    ): LogWriter

    @Binds
    @Singleton
    abstract fun bindAnalyticsTracker(
        impl: FirebaseAnalyticsTracker,
    ): AnalyticsTracker

    @Binds
    @Singleton
    abstract fun bindAnalyticsBundleFactory(
        impl: AndroidAnalyticsBundleFactory,
    ): AnalyticsBundleFactory

    companion object {
        @Provides
        @Singleton
        fun provideFirebaseAnalytics(
            @ApplicationContext context: Context,
        ): FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

        @Provides
        @Singleton
        fun provideFirebaseCrashlytics(): FirebaseCrashlytics =
            FirebaseCrashlytics.getInstance()
    }
}
