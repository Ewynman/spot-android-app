# 01 — Android Architecture & Tech Stack

This document maps the iOS architecture (SwiftUI + ViewModel + Service/Repository, Supabase-backed) onto a modern Android stack, and lists the platform adaptations.

## Recommended stack

| Concern | iOS (reference) | Android (recommended) |
|---------|-----------------|-----------------------|
| Language | Swift | **Kotlin** |
| UI | SwiftUI | **Jetpack Compose** (Material 3, custom theme) |
| State | `ObservableObject` + `@StateObject`/`@EnvironmentObject` | **ViewModel + `StateFlow`/`UiState`** (MVVM/MVI) |
| DI | Manual init / singletons | **Hilt** (or Koin) |
| Navigation | `NavigationStack` + custom tab shell + overlay flags | **Navigation-Compose** with a bottom `NavigationBar`; overlays as top-level composables driven by a shared state holder |
| Async | Swift Concurrency (`async/await`, actors) | **Kotlin Coroutines + Flow** |
| Backend SDK | `supabase-swift` | **`supabase-kt`** (`gotrue-kt`, `postgrest-kt`, `storage-kt`, `functions-kt`, `realtime-kt`) |
| Images | Custom signed-URL loader + caching | **Coil** with an auth-aware fetcher (attach session for signed URLs) |
| Maps | MapKit | **Google Maps Compose** (`maps-compose`) or MapLibre. See map notes below. |
| Location | CoreLocation | **FusedLocationProviderClient** (Play Services Location) |
| Billing | StoreKit 2 | **Google Play Billing Library v6+** |
| Push | APNs (local notifications today) | **FCM** + local notifications (`NotificationManagerCompat`) |
| Deep links | Universal Links + custom scheme | **Android App Links** (verified `https`) + custom scheme intent filters |
| Analytics/Crash | Firebase Analytics + Crashlytics | Firebase (same) or your choice |
| Local persistence | `UserDefaults`, JSON files (drafts), Keychain (session) | **DataStore** (prefs), files/Room for drafts, **EncryptedSharedPreferences**/Android Keystore for session (supabase-kt handles session storage) |
| Local search history | JSON in prefs | DataStore/Room |

## Layered architecture

Mirror the iOS separation:

```
UI (Compose screens & components)
  → ViewModel (StateFlow<UiState>, one per screen/feature)
     → Repository / Service (typed, mockable interfaces)
        → Supabase client (Postgrest RPC/table, Storage, Functions, Auth)
           → Postgres + RLS + Storage
```

Rules:
- **Typed repositories** with interfaces so they can be faked in tests (mirror `FeedRepository`, `SpotSupabaseRepository`, `AuthService`, `SearchService`, etc.).
- ViewModels expose immutable `UiState` and one-shot effects (snackbars, navigation) via `Channel`/`SharedFlow`.
- No business logic in composables.
- Every user-facing flow handles **loading / empty / error / unauthenticated** states explicitly.

## Suggested module / package layout

```
app/
  di/                      # Hilt modules
  core/
    supabase/              # SupabaseClientProvider, session storage
    design/                # Theme, colors, typography, spacing tokens, reusable components
    model/                 # Domain models (Spot, User, VibeTag, ...) + DTOs
    util/                  # Constants, validators, geohash, logging
  data/
    auth/                  # AuthRepository, session bridge
    feed/                  # FeedRepository, FeedApi (DTOs), FeedEventService, ranker (optional)
    spots/                 # SpotRepository (publish, edit, delete), PublishCoordinator, drafts
    map/                   # MapViewportLoader
    search/                # SearchRepository, SearchHistory
    profile/               # ProfileRepository, FollowRepository
    moderation/            # ModerationService, TermsService
    subscription/          # BillingRepository, entitlement resolver
    media/                 # Upload + moderation client, signed-URL resolver
  feature/
    launch/  auth/  onboarding/  home/  map/  post/  search/
    profile/  settings/  paywall/  permissions/  notifications/
  navigation/              # NavGraph, routes, tab shell, overlay host
```

## Backend configuration

Android connects to the **same Supabase project** as iOS.

| Key | Value |
|-----|-------|
| Project ref | `aeurigbbohyxvtsfiyul` |
| Base URL | `https://aeurigbbohyxvtsfiyul.supabase.co` |
| Anon/publishable key | Fetch from Supabase dashboard → Project Settings → API. Safe to embed in the client (RLS enforces access). Do **not** embed the service-role key. |
| Storage buckets | `spots`, `pending_images`, `avatars` (see [04-backend-api.md](04-backend-api.md)) |
| Edge function | `moderate-image` (`verify_jwt = true`) |

The iOS app reads `url` + `anonKey` from `Info.plist` (`Supabase` dict). On Android, store these in `local.properties`/`BuildConfig` or a bundled config (not the service key).

## Platform adaptations — [ANDROID DECISION] items

1. **Sign in with Apple → primary social login on Android.**
   - iOS offers Sign in with Apple + email/password. On Android, Apple Sign In is possible via Supabase OAuth (web flow) but is unusual. **Recommendation:** keep **email/password + email OTP** as the primary flow (already supported by the backend), and add **Google Sign-In** via Supabase OAuth as the social option. If you want cross-platform account continuity for Apple users, keep Apple OAuth available too.
   - The backend `sync_current_user_v1` and username-collection gate work regardless of provider. The "post-auth username setup" gate applies to any provider where the account was created without a username (i.e. OAuth).

2. **StoreKit → Google Play Billing.**
   - iOS product id: `spotPro` (yearly auto-renewing). Create an equivalent **subscription product in Google Play Console** and map entitlement to `users.is_pro` / `users.pro_until`. See [12-pro-subscription.md](12-pro-subscription.md). Verify purchases server-side (Play Developer API) or via a Supabase edge function before flipping Pro.

3. **Universal Links → App Links.**
   - Host `assetlinks.json` at `https://spotapp.online/.well-known/assetlinks.json` (Android's analog of AASA). Intent filters for `https://spotapp.online/s/*` and `https://www.spotapp.online/s/*`, plus custom scheme `spotapp://`. See [15-deep-links.md](15-deep-links.md).

4. **APNs → FCM.**
   - Follow-request/accept notifications are **local** today, so a local `NotificationManagerCompat` implementation is enough for parity. For the future server-driven "follow request received" push, use FCM tokens stored server-side. See [14-notifications.md](14-notifications.md).

5. **Maps provider.**
   - MapKit clustering/soft-density is custom on iOS. On Android use **Google Maps Compose**. Reproduce: individual branded pins, a user-location avatar marker (gold ring for Pro / green otherwise), viewport-debounced fetch via `get_map_spots_v1`, a bottom drawer, and the drawer dismiss policy. Numeric tuning constants are in [07-map.md](07-map.md).

6. **Image processing.**
   - iOS downsizes to max 1600px and JPEG-encodes before upload. Match this on Android (decode + downscale to ≤1600px longest edge, JPEG quality ~0.8) to keep upload sizes and moderation behavior consistent.

## Cross-cutting concerns to port

- **Logging:** iOS uses `SpotLogger` with per-area categories toggled via prefs. Provide a structured logger with debug categories (feed, auth, network, post, map, deep-link, privacy) toggleable in a debug settings screen. Never log PII or raw image bytes.
- **Fresh-install detection:** iOS clears stale Keychain sessions on reinstall. On Android, supabase-kt session storage in app-private storage is cleared on uninstall automatically; still handle "signed-out but stale prefs" gracefully.
- **Session bridge:** a small holder exposing the current user id/session synchronously for gates (deep links, publish) mirrors `SpotAuthBridge`.

## Key shared constants (must match iOS)

See [02-design-system.md](02-design-system.md) and feature files for full lists. Highlights:

- Feed page size: **24**.
- Post limits: free **1 image / 1 vibe**, Pro **5 images / 5 vibes**.
- Bookmark cap (free): **50** (then paywall).
- Vibe tag length: **2–30** chars.
- Publish timeout: **90 s**.
- Email OTP: **6 digits**, resend cooldown **30 s**.
- Search debounce **0.3 s**, history max **20**/segment, grid page target **24**.
- Follow requests page size **24**, badge poll every **8 s**.
- Map visible-spots cap **250**.
