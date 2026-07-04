package com.spot.android.di

import com.spot.android.data.safety.SafetyRepository
import com.spot.android.data.safety.SupabaseSafetyRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for safety / moderation dependencies.
 *
 * Reference: PRD/13-moderation-safety.md
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SafetyModule {

    @Binds
    @Singleton
    abstract fun bindSafetyRepository(
        impl: SupabaseSafetyRepository,
    ): SafetyRepository
}
