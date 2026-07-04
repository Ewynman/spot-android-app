package com.spot.android.di

import com.spot.android.data.billing.BillingRepository
import com.spot.android.data.billing.PlayBillingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for billing dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class BillingModule {

    @Binds
    @Singleton
    abstract fun bindBillingRepository(
        impl: PlayBillingRepository
    ): BillingRepository
}
