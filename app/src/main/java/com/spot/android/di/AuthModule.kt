package com.spot.android.di

import com.spot.android.data.auth.AuthRepository
import com.spot.android.data.auth.SupabaseAuthRepository
import com.spot.android.data.auth.SupabaseUserSessionRepository
import com.spot.android.data.auth.UserSessionRepository
import com.spot.android.data.terms.SupabaseTermsRepository
import com.spot.android.data.terms.TermsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for auth and session dependencies.
 *
 * Reference: PRD/05-auth-onboarding.md
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: SupabaseAuthRepository,
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserSessionRepository(
        impl: SupabaseUserSessionRepository,
    ): UserSessionRepository

    @Binds
    @Singleton
    abstract fun bindTermsRepository(
        impl: SupabaseTermsRepository,
    ): TermsRepository
}
