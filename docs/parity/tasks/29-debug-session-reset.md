# Task 29 — Debug: one-shot session reset

**Size:** Tiny (< 1 h) • **Priority:** P3 • **Status:** Open
**iOS reference PR:** [Ewynman/spot-ios-app #60](https://github.com/Ewynman/spot-ios-app/pull/60)

## Goal

Debug builds should include a **Clear session on next launch** toggle
in Settings → Debug so QA can reset auth state without uninstalling.

## Contract

### Behavior

- Toggle in Settings → Debug → **Clear session on next launch**.
- Off by default.
- When on and the app cold-starts:
  1. Before initializing `SupabaseClientProvider` or `AuthViewModel`,
     the reset runs.
  2. Clears:
     - supabase-kt persisted session (encrypted prefs).
     - `AuthAccountHintStore` DataStore.
     - `PendingVerificationStore` DataStore.
     - `UserSessionHolder` in-memory state (implicitly, since the
       app restarts).
     - Signed-URL cache in `ImageUrlSigner`.
  3. Sets the toggle back to **off** (one-shot).
- Does **not** clear unrelated app data (drafts, permissions history,
  search history).

### Gating

- Only compiled into Debug (`BuildConfig.DEBUG`). Do not ship in
  Release APK.

## iOS reference

- PR 60 (see `../spot-ios-app/`):
  - `Utils/DebugKeychainReset.swift`
  - `scripts/DebugSettingsRoot.plist`

## Android target (files to touch)

Create:
- `data/auth/DebugSessionResetPreferences.kt` — DataStore flag.
- `data/auth/DebugSessionResetter.kt` — the reset routine.
- `app/src/test/.../data/auth/DebugSessionResetterTest.kt` — verify
  keys cleared, one-shot flag flipped.

Edit:
- `SpotApplication.onCreate(...)` — invoke resetter if flag is set,
  before Supabase init. Guard with `if (BuildConfig.DEBUG)`.
- `feature/settings/DebugLoggingScreen.kt` (or a new `DebugScreen.kt`)
  — add the toggle. Only visible in Debug.
- `feature/settings/SettingsNavigationHost.kt` — Debug section
  visibility (already Debug-only).

## Acceptance criteria

- [ ] Toggle visible in Debug builds only.
- [ ] Toggle default off.
- [ ] Turn on → force-quit → relaunch → user is signed out and
      account hint is gone; toggle is off.
- [ ] Turn on → cold start (not force-quit) → resets on next process
      creation.
- [ ] Drafts, search history, permission prompts history remain
      intact (verify by populating them then resetting).
- [ ] Release APK does not contain the debug reset class (verify
      via `./gradlew assembleRelease` + APK inspection or
      `dexdump | grep DebugSessionResetter` → absent).
- [ ] `./gradlew testDebugUnitTest lintDebug assembleDebug assembleRelease` green.

## Test plan

- Manual (Debug): populate a draft + search history → toggle reset →
  relaunch → signed out but drafts + history intact.
- APK inspection: confirm Release doesn't contain the resetter.

## Out of scope

- Any production reset flow.
- Clearing app-level user data (that's system Settings → Apps).

## Follow-ups

- Add a "Reset all app data" super-toggle that clears everything —
  Debug only, dangerous.
