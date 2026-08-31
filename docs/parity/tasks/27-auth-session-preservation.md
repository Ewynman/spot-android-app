# Task 27 — Auth session preservation across reinstalls

**Size:** Medium (3–8 h) • **Priority:** P1 • **Status:** Open
**iOS reference PR:** [Ewynman/spot-ios-app #53](https://github.com/Ewynman/spot-ios-app/pull/53)

## Goal

Match iOS PR #53's auth guarantees:

1. **Preserve device-local Supabase sessions** after reinstall (do not
   intentionally sign users out).
2. **Local-scope logout** — signing out on this device does not revoke
   other device sessions.
3. Store a **removable device-local account hint** (email + last
   username, for the Welcome Back screen). Never persist passwords,
   OTPs, tokens.
4. Persist minimal **email-verification recovery state** so OTP setup
   can resume after relaunch.
5. **Deterministic launch session refresh** — tear down prior-user
   caches when refresh fails.
6. **Email-only login** — remove any anonymous-username-to-email
   resolution path.
7. Distinguish **username-availability backend failures** from
   genuinely taken usernames.

## Contract

### Session storage

- supabase-kt on Android uses `EncryptedSharedPreferences` by default
  for session persistence — **verify** the current
  `SupabaseClientProvider.kt` init keeps this enabled.
- On reinstall (fresh install after uninstall): sessions are
  intentionally cleared — that's OS behavior. But **on app upgrade**
  or clear-data of another app, our session should remain.
- Do **not** call `signOut(SignOutScope.GLOBAL)` at launch — that's
  the "intentional sign out" iOS just removed.

### Local-scope logout

- Settings → Sign out uses `SignOutScope.LOCAL` (supabase-kt) so other
  devices signed in as the same user are unaffected.
- Confirm the sign-out flow does not revoke the refresh token
  server-wide.

### Account hint

- After successful auth, persist `{ email, lastUsername }` to a
  dedicated DataStore file (not the encrypted session store).
- On launch when unauthenticated: show `WelcomeBackScreen` prefilled
  with the hint's email (`auth.welcomeBack.emailField` prefilled).
- Add a `Sign in with a different account` link to clear the hint.

### Verification recovery

- If the user starts sign-up (OTP pending) and force-quits the app,
  relaunching resumes on `ConfirmEmailScreen` with the same email.
- Store `{ email, expiresAt }` in `PendingVerificationStore`
  (already exists — verify TTL handling).

### Launch refresh determinism

- On launch, refresh the session **before** loading user-scoped state.
- If refresh fails:
  1. Clear `UserSessionHolder` (liked / bookmarked / blocked / Pro
     sets).
  2. Clear signed-URL cache.
  3. Route to Welcome (via account hint if present, else fresh
     welcome).
- Add a `launch.sessionRefreshFailed` structured log with the error
  category (no PII).

### Email-only login

- The login screen accepts email only. Do not attempt to resolve a
  typed username to an email via any RPC.
- If needed for UX, add a hint: `Sign in with the email you used to
  create your account.`

### Username availability

- `is_username_available` RPC can return three outcomes on the client:
  - `available: true`
  - `available: false, reason: "taken"` → show "That username is
    taken."
  - RPC error / network error → show "Couldn't check right now. Try
    again."
- Never conflate an RPC error with "taken."

## iOS reference

- PR 53 (see `../spot-ios-app/`):
  - `Services/Auth/AuthService.swift`
  - `Services/Auth/AuthViewModel.swift`
  - `Services/Auth/AuthCredentialStores.swift`
  - `Views/Auth/WelcomeBackView.swift`
- Nonce binding to Apple Sign-In — iOS-specific; Android's Google
  Sign-In gets the equivalent from supabase-kt's PKCE flow.

## Android target (files to touch)

Verify + edit:
- `core/supabase/SupabaseClientProvider.kt` — encrypted session
  storage on; no launch-time global sign-out.
- `data/auth/SupabaseAuthRepository.kt` — sign-out uses
  `SignOutScope.LOCAL`.
- `data/auth/PendingVerificationStore.kt` — verify + resume flow.
- `data/auth/AuthAccountHintStore.kt` (create if not present) — hint
  DataStore.
- `feature/launch/LaunchGateResolver.kt` — refresh session first;
  route on failure.
- `feature/auth/AuthViewModel.kt` — distinguish username availability
  errors from "taken".
- `feature/auth/LoginScreen.kt` — remove any username-to-email
  resolution path.
- `feature/auth/WelcomeScreen.kt` — Welcome Back sub-route when hint
  exists.
- `data/auth/UserSessionHolder.kt` — clear method invoked on refresh
  failure.

Tests:
- `AuthViewModelTest` — sign-out scope, username availability three
  outcomes.
- `LaunchGateResolverTest` — refresh failure clears state and routes.
- `AuthAccountHintStoreTest` — persist + read + clear.

## Acceptance criteria

- [ ] After successful login → sign out → session cleared locally;
      a second device signed in as the same user remains signed in.
- [ ] After successful login → force quit → relaunch → still signed
      in.
- [ ] After successful login → uninstall → reinstall → signed out
      (OS-level); WelcomeBack prefilled with email hint (hint
      survives if it's stored in Cloud Backup DataStore; else fresh
      welcome — acceptable).
- [ ] Refresh failure clears `UserSessionHolder` + signed-URL cache
      and routes to Welcome.
- [ ] Login refuses non-email input with a clear message.
- [ ] Username field distinguishes "taken" from "check failed."
- [ ] OTP pending state persists across force quit.
- [ ] `./gradlew testDebugUnitTest lintDebug assembleDebug` green.

## Test plan

- Manual test matrix above.
- Manual: force offline during launch → refresh fails → Welcome shows.

## Out of scope

- Apple Sign-In (iOS-specific).
- Passkeys / passwordless links (future PRD).

## Follow-ups

- Auto-signout on refresh token revoked server-side (server-driven).
