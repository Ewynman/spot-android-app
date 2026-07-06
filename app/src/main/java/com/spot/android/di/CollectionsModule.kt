package com.spot.android.di

import com.spot.android.data.collections.CollectionsRepository
import com.spot.android.data.collections.SupabaseCollectionsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for Pro collections feature.
 *
 * Reference: PRD/10-profile-social.md, PRD/12-pro-subscription.md
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class CollectionsModule {

    @Binds
    @Singleton
    abstract fun bindCollectionsRepository(
        impl: SupabaseCollectionsRepository,
    ): CollectionsRepository
}
