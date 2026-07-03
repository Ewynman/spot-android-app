# Spot — Android

**Spot** is a social place-discovery app where people save and share spots centered on **vibe** (how a place feels) rather than rigid categories. This is the **Android port** of the Spot app, built to match the iOS implementation feature-for-feature while sharing the same Supabase backend.

## What is Spot?

Spot emphasizes:
- **Vibe-based discovery** — every spot has vibe tags like "Hidden Gem," "Cozy Corner," "Great For Photos"
- **Premium, minimal UI** — cream background, deep forest-green primary, clean design
- **Map exploration** — discover spots near you with beautiful custom pins
- **Social features** — follow users, like and bookmark spots, private accounts
- **Pro subscription** — unlock advanced features like multi-image posts, custom vibes, and collections
- **Safety first** — built-in moderation, reporting, blocking, and content filtering

## Tech Stack

This Android app is built with modern best practices:

| Concern | Technology |
|---------|-----------|
| **Language** | Kotlin |
| **UI** | Jetpack Compose + Material 3 |
| **Architecture** | MVVM/MVI with ViewModel + StateFlow |
| **Dependency Injection** | Hilt |
| **Navigation** | Navigation Compose + custom bottom bar |
| **Async** | Kotlin Coroutines + Flow |
| **Backend** | Supabase (GoTrue, PostgREST, Storage, Functions, Realtime) via `supabase-kt` |
| **Images** | Coil with auth-aware signed URL fetcher |
| **Maps** | Google Maps Compose |
| **Location** | FusedLocationProviderClient (Play Services) |
| **Billing** | Google Play Billing v6+ |
| **Push** | Firebase Cloud Messaging (FCM) |
| **Analytics** | Firebase Analytics + Crashlytics |
| **Persistence** | DataStore (preferences), Room (drafts), encrypted session storage |

## Project Structure

```
app/src/main/java/com/spot/android/
├── core/
│   ├── supabase/          # Supabase client, session management
│   ├── design/            # Theme, colors, typography, dimensions
│   ├── model/             # Domain models, DTOs, enums
│   └── util/              # Constants, validators, helpers
├── data/
│   ├── auth/              # Authentication repository
│   ├── feed/              # Home feed repository
│   ├── spots/             # Spot publishing, editing
│   ├── map/               # Map data loading
│   ├── search/            # Search repository
│   ├── profile/           # Profile & social repository
│   ├── moderation/        # Safety & moderation services
│   ├── subscription/      # Billing & Pro entitlement
│   └── media/             # Image upload & moderation
├── feature/
│   ├── launch/            # Splash & launch gates
│   ├── auth/              # Sign in, sign up, OTP
│   ├── onboarding/        # Post-auth username & terms gates
│   ├── home/              # Home feed
│   ├── map/               # Map discovery
│   ├── post/              # Spot composer
│   ├── search/            # Search users, locations, vibes
│   ├── profile/           # Profile & social features
│   ├── settings/          # Account, privacy, subscription
│   ├── paywall/           # Pro upsell
│   ├── permissions/       # Permission pre-prompts
│   └── notifications/     # Local notifications
├── navigation/            # NavGraph, routes, overlay host
└── di/                    # Hilt modules
```

## Getting Started

### Prerequisites

- **Android Studio Ladybug (2024.2.1)** or later
- **JDK 17**
- **Android SDK** with min API 26, target API 35
- **Supabase account** with access to project `aeurigbbohyxvtsfiyul`
- **Google Maps API key**
- **Firebase project** configured for Android

### Setup Instructions

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd spot-android
   ```

2. **Configure local.properties**
   
   Copy the template and fill in your credentials:
   ```bash
   cp local.properties.template local.properties
   ```
   
   Edit `local.properties` with your actual values:
   ```properties
   sdk.dir=/path/to/Android/sdk
   
   # Supabase (get from https://supabase.com/dashboard/project/aeurigbbohyxvtsfiyul/settings/api)
   supabase.url=https://aeurigbbohyxvtsfiyul.supabase.co
   supabase.anonKey=YOUR_ACTUAL_ANON_KEY
   
   # Google Maps (get from https://console.cloud.google.com/google/maps-apis/credentials)
   google.maps.apiKey=YOUR_ACTUAL_MAPS_KEY
   
   # Share URL
   share.baseUrl=https://spotapp.online
   
   # Play Billing Product IDs
   billing.productId.proYearly=spot_pro_yearly
   ```

3. **Configure Firebase**
   
   Download your `google-services.json` from Firebase Console and replace the placeholder:
   ```
   Firebase Console → Project Settings → General → Your apps → Download google-services.json
   ```
   
   Place it at `app/google-services.json`

4. **Sync and Build**
   ```bash
   ./gradlew build
   ```

5. **Run the app**
   
   Open the project in Android Studio and run on a device or emulator (API 26+).

## Backend Configuration

This app connects to the **same Supabase project** as the iOS app:

- **Project ref:** `aeurigbbohyxvtsfiyul`
- **Region:** us-west-2
- **Postgres:** 17
- **Base URL:** `https://aeurigbbohyxvtsfiyul.supabase.co`

### Important Rules

1. **Never fork the schema** — Android and iOS share the same database
2. **RLS is authoritative** — never rely on client-only authorization
3. **Every uploaded image must pass moderation** via the `moderate-image` edge function
4. **Only embed the anon key** — never the service-role key
5. **Match iOS behavior exactly** for limits, pagination, and gating

## Development Workflow

### Building

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Install on device
./gradlew installDebug
```

### Testing

```bash
# Run unit tests
./gradlew testDebugUnitTest

# Run instrumented tests
./gradlew connectedAndroidTest

# Run lint
./gradlew lintDebug
```

### Code Style

- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use the `official` Kotlin code style (configured in `gradle.properties`)
- No business logic in composables
- All repositories must be interfaces with fakes for testing
- Use test tags matching iOS identifiers (e.g., `navigation.homeTab`)

## Build Order & Task Graph

The PRD includes a detailed **dependency-ordered task graph** in `PRD/18-build-order.md`. Features are organized into phases:

- **Phase 0:** Project scaffold ✅ (this PR)
- **Phase 1:** Foundation (design system, Supabase core, models, image loader, shared components, navigation, logging)
- **Phase 2:** Auth & safety spine (auth flows, session management, terms gating, report/block flows)
- **Phase 3:** Core content surfaces (feed, post, map, search, profile)
- **Phase 4:** Monetization & settings (Pro/billing, collections, settings, deep links, notifications)
- **Phase 5:** Hardening & release (tests, performance, accessibility, release checklist)

Each task is sized for one autonomous agent and can run in parallel with its siblings (within a phase) once dependencies are met.

## Key Constants

These values **must match iOS** to ensure consistent behavior:

| Constant | Value | Usage |
|----------|-------|-------|
| Feed page size | 24 | Pagination |
| Free post limits | 1 image, 1 vibe | Content limits |
| Pro post limits | 5 images, 5 vibes | Content limits |
| Free bookmark cap | 50 | Then paywall |
| Vibe tag length | 2–30 chars | Validation |
| Publish timeout | 90 seconds | Background queue |
| OTP length | 6 digits | Email verification |
| OTP resend cooldown | 30 seconds | Rate limiting |
| Map visible spots cap | 250 | Performance |
| Search debounce | 300ms | UX optimization |

## Documentation

Comprehensive product requirements are in the `PRD/` folder:

- `PRD/00-overview.md` — Vision, personas, terminology
- `PRD/01-architecture-android.md` — Stack, platform adaptations
- `PRD/02-design-system.md` — Colors, typography, tokens, components
- `PRD/03-data-model.md` — Full Supabase schema
- `PRD/04-backend-api.md` — RPC catalog, storage, edge functions
- `PRD/05-auth-onboarding.md` — Auth flows, OTP, terms gates
- `PRD/06-home-feed.md` — Feed screen, actions, pagination
- `PRD/07-map.md` — Map discovery, pins, drawer
- `PRD/08-post-flow.md` — Composer, moderation pipeline
- `PRD/09-search.md` — Search users, locations, vibes
- `PRD/10-profile-social.md` — Profile, follows, privacy
- `PRD/11-settings.md` — Account, security, subscription
- `PRD/12-pro-subscription.md` — Pro entitlements, billing
- `PRD/13-moderation-safety.md` — Moderation, reports, blocks
- `PRD/14-notifications.md` — Local + push notifications
- `PRD/15-deep-links.md` — App Links, custom scheme
- `PRD/16-feed-ranking-algorithm.md` — Server ranking logic
- `PRD/17-non-functional-testing.md` — Performance, logging, testing
- `PRD/18-build-order.md` — Execution layer, task graph

**Always read the relevant PRD doc before implementing a feature.**

## Design System

Spot uses a **light-theme only** design (no dark mode in v1 for parity):

### Colors

- **Background:** `#F5F3EF` (cream)
- **Primary:** `#1D2C24` (deep forest green)
- **Button Text:** `#F5F3EF` (cream on dark backgrounds)
- **Accent:** `#DEE6D8` (soft green, vibe tags ONLY)
- **Pro Gold:** `#C9A24A` (Pro user indicators)

### Layout Tokens

- **Horizontal padding:** 32dp
- **Spacing:** small (8dp), medium (12dp), large (16dp), xl (24dp)
- **Radius:** small (10dp), medium (12dp), large (20dp)

### Typography

Uses system font (Roboto) with:
- **Display:** "SPOT" wordmark (32sp, bold, uppercase)
- **Title:** Screen headers
- **Body:** Captions, usernames, general text
- **Label:** Vibe chips, buttons

## Safety & Security

Safety features are **non-negotiable** and ship in v1:

- ✅ Terms agreement before registration
- ✅ Image + text moderation (Azure Content Safety)
- ✅ Report content (spots, profiles)
- ✅ Report + block users
- ✅ Content filtering based on blocks and suspensions
- ✅ Private accounts
- ✅ Account deletion with re-auth

### Security Best Practices

- Only the **anon key** is embedded in the client
- All authorization enforced via **RLS** on Supabase
- Images downscaled to ≤1600px before upload
- Signed URLs cached until near 7-day expiry
- No PII logged (structured logger with debug toggles)
- Treat 401/403 as expected when unauthenticated

## CI/CD

GitHub Actions workflow runs on every push and PR:

```yaml
- Build debug APK
- Run lint checks
- Run unit tests
```

The workflow creates a placeholder `local.properties` for CI builds.

## Contributing

### Definition of Done

Every feature task must:

1. Match the relevant PRD contract (states, copy, constants)
2. Implement loading/empty/error/unauthenticated states
3. Include unit tests for ViewModel logic using fakes
4. Use only the anon key (never service-role key)
5. Compile with `./gradlew assembleDebug`
6. Pass lint checks
7. No hardcoded secrets
8. No new PII in logs
9. Use test tags matching iOS vocabulary where practical

### Git Workflow

- Create feature branches from `main`
- Follow branch naming: `cursor/<descriptive-name>-cbb1`
- Keep commits atomic and well-described
- Ensure CI passes before merging
- Each PR should cover one task from `PRD/18-build-order.md`

## License

[Add appropriate license]

## Contact

[Add contact information or links to issue tracker]

---

**Note:** This is the scaffold phase (Phase 0.1). The app currently displays a placeholder screen. Features will be implemented according to the dependency order in `PRD/18-build-order.md`.
