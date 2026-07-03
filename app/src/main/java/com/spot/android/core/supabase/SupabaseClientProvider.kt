package com.spot.android.core.supabase

import android.content.Context
import com.spot.android.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.FlowType
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.ktor.client.engine.android.Android
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides configured Supabase client with all required modules.
 * 
 * This is the single source of truth for Supabase configuration in the app.
 * Connects to the same project as iOS (aeurigbbohyxvtsfiyul).
 * 
 * Modules enabled:
 * - Auth (GoTrue): Email/password, OAuth, OTP
 * - Postgrest: RPCs and direct table access
 * - Storage: Image uploads and signed URLs
 * - Functions: Edge function calls (moderate-image)
 * - Realtime: Future use for live updates
 * 
 * Reference: PRD/01-architecture-android.md, PRD/04-backend-api.md
 */
@Singleton
class SupabaseClientProvider @Inject constructor(
    private val context: Context
) {
    
    /**
     * Configured Supabase client singleton.
     * 
     * Session is automatically persisted in encrypted app-private storage
     * and refreshed by supabase-kt.
     */
    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            // HTTP engine
            httpEngine = Android.create()
            
            // Auth module (GoTrue)
            install(Auth) {
                // Use implicit flow (PKCE) for OAuth
                flowType = FlowType.PKCE
                
                // Scheme for OAuth redirects (matches AndroidManifest deep link)
                scheme = "spotapp"
                host = "auth-callback"
                
                // Session storage is encrypted by default in Android
                // Stored in app-private SharedPreferences
            }
            
            // Postgrest for RPCs and table access
            install(Postgrest) {
                // Default serialization handles snake_case
            }
            
            // Storage for image uploads and signed URLs
            install(Storage)
            
            // Functions for edge function calls
            install(Functions)
            
            // Realtime for future live updates
            install(Realtime)
        }
    }
    
    /**
     * Quick access to auth module.
     */
    val auth: Auth
        get() = client.auth
}
