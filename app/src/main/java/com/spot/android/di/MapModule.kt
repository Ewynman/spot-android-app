package com.spot.android.di

import com.spot.android.data.location.AndroidMapLocationTracker
import com.spot.android.data.location.MapLocationTracker
import com.spot.android.data.map.FollowingIdsRepository
import com.spot.android.data.map.MapRepository
import com.spot.android.data.map.SupabaseFollowingIdsRepository
import com.spot.android.data.map.SupabaseMapRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for map dependencies.
 *
 * Reference: PRD/07-map.md
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class MapModule {

    @Binds
    @Singleton
    abstract fun bindMapRepository(
        impl: SupabaseMapRepository,
    ): MapRepository

    @Binds
    @Singleton
    abstract fun bindFollowingIdsRepository(
        impl: SupabaseFollowingIdsRepository,
    ): FollowingIdsRepository

    @Binds
    @Singleton
    abstract fun bindMapLocationTracker(
        impl: AndroidMapLocationTracker,
    ): MapLocationTracker
}
