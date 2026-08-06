package com.spot.android.di

import com.spot.android.data.deeplink.SpotDetailRepository
import com.spot.android.data.deeplink.SupabaseSpotDetailRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DeepLinkModule {
    @Binds
    @Singleton
    abstract fun bindSpotDetailRepository(
        impl: SupabaseSpotDetailRepository,
    ): SpotDetailRepository
}
