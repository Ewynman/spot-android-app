package com.spot.android.di

import com.spot.android.data.permissions.AndroidPermissionsRepository
import com.spot.android.data.permissions.PermissionsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for permissions framework dependencies.
 *
 * Reference: PRD/05-auth-onboarding.md
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class PermissionsModule {

    @Binds
    @Singleton
    abstract fun bindPermissionsRepository(
        impl: AndroidPermissionsRepository,
    ): PermissionsRepository
}
