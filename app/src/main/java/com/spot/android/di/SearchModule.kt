package com.spot.android.di

import com.spot.android.data.search.SearchRepository
import com.spot.android.data.search.SupabaseSearchRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for search dependencies.
 *
 * Reference: PRD/09-search.md
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SearchModule {

    @Binds
    @Singleton
    abstract fun bindSearchRepository(
        impl: SupabaseSearchRepository,
    ): SearchRepository
}
