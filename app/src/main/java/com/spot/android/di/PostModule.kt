package com.spot.android.di

import com.spot.android.data.location.AndroidPlaceSearchProvider
import com.spot.android.data.location.PlaceSearchProvider
import com.spot.android.data.post.FilePostDraftRepository
import com.spot.android.data.post.PostDraftRepository
import com.spot.android.data.post.SpotPublishRepository
import com.spot.android.data.post.SupabaseSpotPublishRepository
import com.spot.android.data.post.SupabaseVibeTagRepository
import com.spot.android.data.post.VibeTagRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for post flow dependencies.
 *
 * Reference: PRD/08-post-flow.md
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class PostModule {

    @Binds
    @Singleton
    abstract fun bindPostDraftRepository(
        impl: FilePostDraftRepository,
    ): PostDraftRepository

    @Binds
    @Singleton
    abstract fun bindVibeTagRepository(
        impl: SupabaseVibeTagRepository,
    ): VibeTagRepository

    @Binds
    @Singleton
    abstract fun bindSpotPublishRepository(
        impl: SupabaseSpotPublishRepository,
    ): SpotPublishRepository

    @Binds
    @Singleton
    abstract fun bindPlaceSearchProvider(
        impl: AndroidPlaceSearchProvider,
    ): PlaceSearchProvider
}
