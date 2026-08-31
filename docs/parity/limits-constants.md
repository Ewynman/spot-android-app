# Limits & constants

Every value here must match iOS exactly. **Android source of truth is
`core/util/Constants.kt`.** iOS source is
`../spot-ios-app/Spot/Utils/Constants.swift` +
`.../Spot/Services/Feed/FeedFlags.swift` + selected view/service files.

Never inline a magic number in feature code — always reference the constant.

## Pagination & lists

| Constant | Value | Android source | iOS source |
|----------|-------|----------------|------------|
| Feed page size | **24** | `Constants.Pagination.defaultPageSize` | `FeedFlags.pageSize` |
| Bookmarks page size | 24 | Same | `BookmarksViewModel.swift:19` |
| Likes page size | 24 | Same | `LikesViewModel.swift:19` |
| Map viewport cap | **250** | `Constants.MapDesign.visibleSpotsCap` | `MapViewModel.swift:31` |
| Map RPC server cap | 500 | (server clamps) | `FeedAPI.swift:630` |

## Post limits

| Constant | Free | Pro | Android source | iOS source |
|----------|------|-----|----------------|------------|
| Images per post | **1** | **5** | `Constants.PostLimits.maxFreePostImages` / `maxProPostImages` | `Constants.PostLimits` |
| Vibes per post | **1** | **5** | `Constants.PostLimits.maxFreePostVibes` / `maxProPostVibes` | `Constants.PostLimits` |

## Validation

| Concern | Value | Android source |
|---------|-------|----------------|
| Vibe tag length | **2–30 chars** | `VibeTagValidator.kt` |
| Username length | 3–20 chars | `UsernameValidator.kt` |
| Username charset | `[a-zA-Z0-9_.]` | `UsernameValidator.kt` |
| Password min length | 8 | (server + client) |
| Password requirements | upper + lower + digit + symbol | Client hint only; server authoritative |
| Report details max | **500 chars** | Enforce in `feature/safety/ReportSheet.kt` |
| Blocked terms list | `app/src/main/assets/blocked_terms.json` (mirror iOS `Spot/Resources/BlockedTerms.json`) | Vibe + username validators |

## Auth / OTP

| Constant | Value | Android source |
|----------|-------|----------------|
| OTP digits | **6** | `AuthViewModel.kt` |
| OTP resend cooldown | **30 s** | `AuthViewModel.kt` |
| Session storage | Encrypted (supabase-kt default on Android) | supabase-kt |
| OAuth callback URI | `spotapp://auth-callback` | `AndroidManifest.xml` + `SupabaseClientProvider.kt` |

## Publish pipeline

| Constant | Value | Android source |
|----------|-------|----------------|
| Publish timeout | **90 s** | `SpotPublishCoordinator.kt` |
| Image long edge (import) | **1600 px** | `ImageProcessor.kt` |
| Editor preview long edge | 900 px | `ImageProcessor.kt` (if present) |
| JPEG quality (publish) | **~0.80** | `ImageProcessor.kt` |
| JPEG quality (avatar) | 0.70 | `ProfilePictureUploader` (add if missing) |
| Signed URL TTL | **7 days** (604800 s) | `ImageUrlSigner.kt` |
| Signed URL re-sign threshold | 6 hours before expiry | `ImageUrlSigner.kt` |

## Feed events

| Constant | Value | Android source |
|----------|-------|----------------|
| Impression debounce | **30 s** | `FeedEventService.kt` |
| Visible threshold | 2 s | `FeedEventService.kt` |
| Long dwell threshold | 8 s | `FeedEventService.kt` |
| Quick skip threshold | 1 s | `FeedEventService.kt` |
| Persistent seen TTL | 7 days | `FeedEventService.kt` |

## Map

| Constant | Value | Android source |
|----------|-------|----------------|
| Region debounce | **250 ms** | `MapViewModel.kt` (iOS uses 180/380; 250 is a reasonable Android choice) |
| Initial radius | 4000 m | `Constants.MapDesign` |
| Neighborhood radius | 3200 m | `Constants.MapDesign` |
| Local search radius | 50_000 m | `AndroidPlaceSearchProvider.kt` |
| Marker cluster distance | (see `MapPinLayout.kt`) | — |

## Deep links

| Constant | Value | Android source |
|----------|-------|----------------|
| Spot ID max length | **50** | `DeepLinkRouter.kt` |
| Spot ID charset | `[a-zA-Z0-9_-]` | `DeepLinkRouter.kt` |
| Deep link debounce | 1 s | `DeepLinkCoordinator.kt` |
| Custom scheme | `spotapp://` | `AndroidManifest.xml` |
| App Links host | `spotapp.online` (+ `www.spotapp.online`) | `AndroidManifest.xml` |

## Launch

| Constant | Value | Android source |
|----------|-------|----------------|
| Splash minimum duration | 1500 ms (production) | `LaunchGateResolver.kt` |
| Splash UI-test duration | 50 ms | Same, when `UI_TEST_MODE` |

## Subscription

| Constant | Value | Android source |
|----------|-------|----------------|
| Product ID | `spot_pro_yearly` | `local.properties` → `billing.productId.proYearly` |
| Pro price (yearly) | US$19.99 | Google Play console |
| Effective Pro rule | `pro_until > now` ELSE `is_pro` | Server-side + `UserSessionHolder` |
| App account token | User UUID | `PlayBillingRepository.kt` |
| Restore path | `BillingClient.queryPurchasesAsync` | `PlayBillingRepository.kt` |

## Bookmarks / collections

| Constant | Value | Notes |
|----------|-------|-------|
| Free bookmark cap | **50** | Not hardcoded on iOS client; enforced server-side. Client shows paywall at cap. |
| Collection name max | 50 chars | Verify with server RLS/trigger |

## Content moderation

| Constant | Value | Android source |
|----------|-------|----------------|
| Image moderation approval field | `approved: boolean` | `SupabaseSpotPublishRepository.kt` |
| Moderation edge function | `moderate-image` | Same |
| Text filtering source list | `app/src/main/assets/blocked_terms.json` | Add if not present; mirror iOS |
| Server text filter RPC | `text_content_filter_v1` | — |

## Image cache

| Constant | Value | Android source |
|----------|-------|----------------|
| Disk cache | Coil default | `ImageModule` |
| Memory cache | Coil default | `ImageModule` |
| Signed URL cache | 6h/7d as above | `ImageUrlSigner.kt` |

## Debug / build

| Constant | Value | Notes |
|----------|-------|-------|
| Supabase project ref | `aeurigbbohyxvtsfiyul` | Staging + shared with iOS staging |
| Anon key | injected via `local.properties` | Never commit real keys |
| Google Maps API key | injected via `local.properties` | Restricted to app package + SHA-1 |
| Firebase App ID (Android) | See real `google-services.json` (task 06) | Placeholder currently |

## Do not introduce new limits without

1. Updating this file **and** `core/util/Constants.kt`.
2. Cross-checking with iOS `Constants.swift`.
3. Filing a design note in the PR description if the number is Android-specific
   (e.g., the map debounce differs from iOS on purpose).
