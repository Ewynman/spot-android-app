package com.spot.android.di

import com.spot.android.data.profile.FollowRepository
import com.spot.android.data.profile.ProfileRepository
import com.spot.android.data.profile.SupabaseFollowRepository
import com.spot.android.data.profile.SupabaseProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for profile and social dependencies.
 *
 * Reference: PRD/10-profile-social.md
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileModule {

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        impl: SupabaseProfileRepository,
    ): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindFollowRepository(
        impl: SupabaseFollowRepository,
    ): FollowRepository
}
