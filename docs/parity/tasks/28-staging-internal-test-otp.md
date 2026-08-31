# Task 28 — Staging internal-test OTP for Debug builds

**Size:** Small (1–3 h) • **Priority:** P3 • **Status:** Open
**iOS reference PR:** [Ewynman/spot-ios-app #76](https://github.com/Ewynman/spot-ios-app/pull/76)

## Goal

Debug builds should let internal testers sign up with any fake email
and verify with a fixed OTP (`UT1234`, case-insensitive) via the
existing `staging-verify-email` edge function.

Release / Play internal-track builds must **not** include the UI or
client path.

## Why it matters

Speeds up internal testing — you don't need to hit a real inbox for
every signup on staging.

## Contract

### Build gating

- Only enabled when:
  - `BuildConfig.DEBUG == true`, **AND**
  - Environment is **staging** (verify via `BuildConfig.SUPABASE_URL`
    contains `aeurigbbohyxvtsfiyul`).
- Release builds compile the class out with an `if (BuildConfig.DEBUG)`
  guard around the composable and repository call.

### UI

- On `ConfirmEmailScreen`, add a small link below the OTP row:
  `Use internal test code`.
- Tapping it fills `UT1234` into the six cells and submits.
- Also accepts case-insensitive typed variants (`ut1234`, `UT1234`).

### Client flow

- Instead of Supabase's normal OTP verify, call the
  `staging-verify-email` edge function with the pending email.
- Edge function returns a `token_hash` and `type`; client calls
  `verifyOTP(tokenHash:type:)` (or supabase-kt Android equivalent —
  probably `auth.verifyEmailOtp(...)`).

### Rate limiting

- The server-side edge function enforces rate limits via a
  `staging_test_auth_attempts` table. Client just handles the error
  response (429 or similar) with a toast: `Too many test attempts.
  Try again in a moment.`

## iOS reference

- PR 76 (see `../spot-ios-app/`):
  - `Services/Auth/StagingTestEmailVerification.swift` — client.
  - `Views/Auth/ConfirmEmailView.swift` — UI toggle.
  - `supabase/functions/staging-verify-email/` — edge function.

## Android target (files to touch)

Create:
- `data/auth/StagingTestEmailVerification.kt` (Debug source set).
- `app/src/debug/java/com/spot/android/data/auth/...` if using source
  sets; alternatively guard everything with `BuildConfig.DEBUG`.

Edit:
- `feature/auth/ConfirmEmailScreen.kt` — conditional link.
- `feature/auth/AuthViewModel.kt` — dispatch to the staging path when
  the link is used.
- `app/build.gradle.kts` — add a `staging` product flavor if there
  isn't one, OR read `SUPABASE_URL` at runtime to decide.

Tests:
- Unit test: link is shown only in Debug + staging.
- Unit test: verify code accepted case-insensitively.

## Acceptance criteria

- [ ] Debug build (staging Supabase): link appears; tapping it
      verifies and signs the user in.
- [ ] Release build: link **absent**; even if the string is present
      in the source, no code path invokes the staging RPC.
- [ ] Real 6-digit emailed OTP still works in Debug.
- [ ] Rate-limit error surfaces a clear toast.
- [ ] `./gradlew testDebugUnitTest lintDebug assembleDebug assembleRelease` green.

## Test plan

- Manual (Debug + staging): sign up with a made-up email → tap
  internal test → in-app authenticated.
- Manual (Debug + staging): send a real OTP flow → still works.
- Manual (Release): reflect the APK to confirm the link isn't rendered.

## Out of scope

- The edge function itself (already deployed on staging).
- Production behavior.

## Follow-ups

- Add a matching hidden gesture (e.g., long-press SPOT wordmark) so
  QA doesn't need to hunt for the link.
