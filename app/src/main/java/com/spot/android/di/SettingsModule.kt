package com.spot.android.di

import com.spot.android.data.settings.SettingsRepository
import com.spot.android.data.settings.SupabaseSettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt DI module for settings-related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsModule {

    @Binds
    abstract fun bindSettingsRepository(impl: SupabaseSettingsRepository): SettingsRepository
}
