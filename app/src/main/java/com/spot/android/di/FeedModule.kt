package com.spot.android.di

import com.spot.android.data.feed.EngagementRepository
import com.spot.android.data.feed.FeedRepository
import com.spot.android.data.feed.SupabaseEngagementRepository
import com.spot.android.data.feed.SupabaseFeedRepository
import com.spot.android.data.location.AndroidViewerLocationProvider
import com.spot.android.data.location.ViewerLocationProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for home feed dependencies.
 *
 * Reference: PRD/06-home-feed.md
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class FeedModule {

    @Binds
    @Singleton
    abstract fun bindFeedRepository(
        impl: SupabaseFeedRepository,
    ): FeedRepository

    @Binds
    @Singleton
    abstract fun bindEngagementRepository(
        impl: SupabaseEngagementRepository,
    ): EngagementRepository

    @Binds
    @Singleton
    abstract fun bindViewerLocationProvider(
        impl: AndroidViewerLocationProvider,
    ): ViewerLocationProvider
}
