package com.spot.android.di

import android.content.Context
import com.spot.android.core.supabase.SessionBridge
import com.spot.android.core.supabase.SupabaseClientProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

/**
 * Hilt module providing Supabase-related dependencies.
 * 
 * Provides:
 * - SupabaseClientProvider (singleton)
 * - SupabaseClient (via provider)
 * - SessionBridge (singleton)
 */
@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {
    
    @Provides
    @Singleton
    fun provideSupabaseClientProvider(
        @ApplicationContext context: Context
    ): SupabaseClientProvider {
        return SupabaseClientProvider(context)
    }
    
    @Provides
    @Singleton
    fun provideSupabaseClient(
        provider: SupabaseClientProvider
    ): SupabaseClient {
        return provider.client
    }
    
    @Provides
    @Singleton
    fun provideSessionBridge(
        provider: SupabaseClientProvider
    ): SessionBridge {
        return SessionBridge(provider)
    }
}
