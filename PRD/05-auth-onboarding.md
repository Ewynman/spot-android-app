# 05 — Auth & Onboarding

Covers the launch gate, welcome, sign up/in, email OTP, OAuth, post-auth username setup, terms gate, the first-run product tour, and permission prompts.

## Launch gate (ordered)

The root decides which screen to show based on auth/session state. Order matters:

| Condition | Screen |
|-----------|--------|
| Session still loading | Launch splash |
| Awaiting email verification | Confirm-email (OTP) |
| Not authenticated | Welcome |
| Authenticated + email verified + OAuth account missing username | Post-auth setup (username) |
| Authenticated + needs terms re-acceptance | Terms update gate (**blocking**) |
| Authenticated + email verified | Main tab shell |
| Authenticated but email unverified | Confirm-email |

Splash timing on iOS: ~1.5s show + 0.5s fade (0.05/0.01 in UI-test). Use a short branded splash on Android.

On authenticated entry the app also (a) refreshes Pro entitlement from the store, (b) checks terms acceptance, (c) checks the post-auth username requirement. These also re-run on foreground.

## Welcome screen

- **Actions:** Sign in with Apple/Google [ANDROID DECISION: Google primary], **Get Started** → Sign up, **Log In** → Login.
- **Terms gate:** an **unchecked "I agree to Terms & Privacy" checkbox** must be ticked before Get Started / Log in / social sign-in are enabled. Cold launch always re-presents this. Links open Terms (`https://spotapp.online/terms`) and Privacy (`https://spotapp.online/privacy`). This is a **Play/App-store safety requirement** — do not skip.
- **No permission prompts** on welcome.
- Test tag: `welcome.screen`.
- Pre-auth agreement is stored locally (mirror `PreAuthTermsAgreementStore`) and auto-recorded server-side after login via `record_terms_acceptance_v1`.

## Sign up (email)

- Fields: **email, username, password, confirm password**.
- **Private account** checkbox (`is_private`).
- Terms checkbox (opens legal URLs).
- Username availability checked via `is_username_available`.
- On success → begin email-verification-pending state → Confirm-email screen.

## Log in

- Fields: **email or username**, password.
- If identifier has no `@`, resolve via `resolve_login_email(username)` then sign in.
- **Forgot password**: requires an email (contains `@`); triggers GoTrue reset email.
- Error copy: network error, username not found, generic incorrect credentials (do not leak which field was wrong).

## Confirm email (OTP)

- **6 numeric digit** entry, auto-advancing focus fields.
- Verify → GoTrue OTP verification (validates exactly 6 digits).
- **Resend code** with a **30-second cooldown**.
- Masked email display: first 2 chars + `****` + domain.
- Back clears the pending state and returns to Welcome.

## OAuth sign-in [ANDROID DECISION]

- iOS: Sign in with Apple → Supabase `signInWithIdToken(provider:.apple)`; persists full name on first consent; then `sync_current_user_v1`.
- Android: use **Google Sign-In** via supabase-kt OAuth (and optionally Apple for cross-platform continuity). Same post-step: `sync_current_user_v1`.
- OAuth accounts are created without a username → **post-auth username gate** appears.

## Post-auth username setup

- Shown when an OAuth account **lacks a username** (email signups already have one).
- Collects **username** (photo optional / legacy PFP-only mode). Loads existing `users.username` / `profile_image_url`; small retry (~350ms) to avoid a flash.
- The **terms agreement checkbox reappears here** and gates "Continue" so OAuth registrations cannot complete without explicit agreement.

## Terms update gate

- Loads active `terms_versions` row + checks `has_accepted_active_terms`.
- If not accepted → **blocking** gate until accepted (records via `record_terms_acceptance_v1`).
- **Fail-open** on network error (don't lock users out).

## First-run product tour

Two managers on iOS; the current one is the **`SpotFirstRunOnboardingManager`** (longer coached tour). A legacy `HomeTourManager` (username/location/vibe/likeSave) is migrated into a single "tour accepted" flag.

**Steps (in order):**
```
welcome → spotCard → spotDetails → vibeTag → like → bookmark → creator
→ mapTab → userLocation → mapMarkers → markerPreview → finale
```

**Start conditions:**
- Authenticated, first session after signup.
- `likedSpots` and `bookmarkedSpots` both empty (fresh user heuristic).
- Not already completed/skipped.
- Skipped entirely in automated UI-test mode.

**Coach targets** (spotlight anchors): spot card, spot details, a vibe tag, like button, bookmark button, creator row, map tab, map user-location control, map markers, marker preview.

**Behaviors:**
- Welcome step stays on Home, ~500ms delay then starts.
- The `mapTab` step switches to the Map tab (index 1) and auto-advances when the map tab is selected.
- `userLocation` step shows the **Location pre-prompt** if permission is `notDetermined`.
- On finale or skip → after ~600ms, show the **Notification permission** prompt if `notDetermined`.

**Persistence keys (mirror with DataStore):**
- `spotFirstRunOnboarding.completed.v1`, `...completedAt.v1`, `...skipped.v1`, `...lastStep.v1`.
- Legacy `homeTourAccepted`.

## Permissions (contextual, never blocking)

Permissions are requested at the moment of need with a **neutral "Continue" pre-prompt** screen before the OS dialog. Denial never blocks app access.

| Permission | Pre-prompt | Native trigger | Android API |
|------------|-----------|----------------|-------------|
| Location | Location pre-prompt | Map recenter / tour user-location step | `ACCESS_FINE/COARSE_LOCATION`, FusedLocation |
| Camera | Camera pre-prompt | Post → Take Photo | `CAMERA` |
| Photos | Photo pre-prompt | Post → Choose from Gallery | Photo Picker (no permission on 13+) / `READ_MEDIA_IMAGES` |
| Notifications | Notification pre-prompt | After first-run tour | `POST_NOTIFICATIONS` (Android 13+) |

**Prompt-state keys (mirror):** `locationPermissionRequested`, `notificationsRequested`, `photoPermissionRequested`, `cameraPermissionRequested`. Also `firstRun` and a legacy `promptPermsOnNextLogin` (bulk prompt deprecated — do NOT bulk-request on launch).

Location denied → the map falls back to a wide default region (continental-US style fallback). See [07-map.md](07-map.md).

## Session state exposed to UI (mirror `AuthViewModel`)

The auth view model publishes: `isAuthenticated`, `isLoading`, `userId`, `isEmailVerified`, `awaitingEmailVerification`, `likedSpots`, `bookmarkedSpots`, `blockedUsers`, `isPro`, `proUntil`, `customVibeTags`, `currentUserProfileImageURL`, `currentUserUsername`. Screens observe these for gating (e.g. like/bookmark state, Pro gates, private-account behavior).

## States to handle

- **Loading**: splash / spinners during session restore and OTP verify.
- **Error**: network, invalid credentials, OTP invalid/expired, username taken, resend cooldown.
- **Empty/first-run**: tour overlay.
- **Unauthenticated**: welcome; pending deep links stored until login (see [15-deep-links.md](15-deep-links.md)).

## Numeric limits

- OTP: **6 digits**; resend cooldown **30s**.
- Post-auth username retry delay ~**350ms**; tour start delay ~**500ms**; notification prompt delay ~**600ms** after finale/skip.
