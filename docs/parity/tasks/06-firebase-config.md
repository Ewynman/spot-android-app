# Task 06 — Real Firebase config + App Check init

**Size:** Small (1–3 h; mostly config) • **Priority:** P2 • **Status:** Open

## Goal

Replace the placeholder `app/google-services.json` with a real Firebase
project config for the Android app, and initialize **Firebase App Check**
in `SpotApplication` so Analytics, Crashlytics, and FCM (task 03) can
run for real.

## Why it matters

Today:

- `app/google-services.json` contains placeholder values (`_comment`
  fields and `REPLACE_WITH_...` API keys).
- The `firebase-appcheck` dependency isn't declared / isn't initialized.
- Analytics + Crashlytics silently no-op on Release builds without the
  right config.
- FCM (task 03) cannot register tokens without a real config + App Check.

## Contract

### Firebase project selection

- **Use the same Firebase project as the iOS app** if there is one. iOS
  uses `Spot/GoogleService-Info.plist`; ask whoever owns that project to
  register an Android app under the same project so Analytics dashboards
  aggregate across platforms.
- Package name: `com.spot.android`.
- Add debug + release SHA-1 and SHA-256 fingerprints (same values you
  add to `assetlinks.json` in task 04, plus SHA-1 for OAuth).

### `google-services.json`

- Replace `app/google-services.json` entirely with the file downloaded
  from the Firebase console for the newly-registered Android app.
- Keep the file **out of source control if it contains real API keys**.
  Options:
  - Commit the file (Google's API key restrictions + Play integrity make
    this generally safe for Firebase's Android SDK config), OR
  - Add it to `.gitignore` and inject it via a build-time script from a
    CI secret. **Recommended: commit it**, matching Firebase's docs.
- Ensure `app/build.gradle.kts` applies `com.google.gms.google-services`
  (it already does).

### App Check

- Add dependencies to `libs.versions.toml`:
  - `firebase-appcheck-playintegrity`
- Initialize in `SpotApplication.onCreate(...)` **before** any other
  Firebase call:

```kotlin
FirebaseApp.initializeApp(this)
FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
    PlayIntegrityAppCheckProviderFactory.getInstance()
)
```

- Register the app in the Firebase console under App Check → Play
  Integrity, and enforce for Analytics + Crashlytics + Firestore (n/a) +
  FCM.
- For **debug builds**, use the `DebugAppCheckProviderFactory` and
  register the debug token printed in Logcat.

### Analytics collection

- **Debug builds:** Analytics collection off (matches iOS
  `AppDelegate.swift`).
- **Release builds:** Analytics + Crashlytics on.
- Toggle via `AnalyticsConfig` (create if missing) or the
  `firebase_analytics_collection_enabled` manifest meta-data.

## iOS reference (for reviewers)

- `../spot-ios-app/Spot/AppDelegate.swift` — Firebase init +
  Crashlytics + Analytics setup.
- `../spot-ios-app/Spot/GoogleService-Info.plist` — the equivalent iOS
  config (for the same Firebase project, if unified).

## Android target (files to touch)

- `app/google-services.json` — replace placeholder with real.
- `SpotApplication.kt` — Firebase init + App Check init in `onCreate`.
- `AndroidManifest.xml` — `firebase_analytics_collection_enabled` meta-data.
- `gradle/libs.versions.toml` — add `firebase-appcheck-playintegrity`.
- `app/build.gradle.kts` — add the dependency.
- `core/analytics/FirebaseAnalyticsTracker.kt` — wire debug-off if not
  already.
- `app/src/test/.../core/analytics/FirebaseAnalyticsTrackerTest.kt` —
  update if the debug-off behavior changes.

## Acceptance criteria

- [ ] `app/google-services.json` contains real values (no `_comment`
      fields).
- [ ] `FirebaseApp.initializeApp(this)` runs first in
      `SpotApplication.onCreate`.
- [ ] `FirebaseAppCheck` installs `PlayIntegrityAppCheckProviderFactory`
      on Release and `DebugAppCheckProviderFactory` on Debug.
- [ ] Debug build: Analytics collection **disabled**; Crashlytics enabled.
- [ ] Release build: Analytics + Crashlytics both enabled.
- [ ] `./gradlew assembleDebug lintDebug testDebugUnitTest` green.
- [ ] Instrumented smoke: Debug build launches without Firebase config
      errors in Logcat.

## Test plan

- Debug: verify a `spot_posted` analytics event **does not** hit the
  Firebase debug view (collection disabled).
- Release (Play internal track): verify a `spot_posted` event **does**
  appear in the Firebase debug view when built with
  `-Pfirebase.analytics.debug=true`.
- Verify Crashlytics test crash appears in the Crashlytics dashboard for
  the release build.

## Out of scope

- FCM token registration (task 03).
- Adding new analytics events (they exist already).
- iOS Firebase project migration.

## Follow-ups

- Add a CI check that fails if `google-services.json` reverts to a
  placeholder.
- Enable App Check enforcement on the Supabase side too (server function
  that verifies the App Check header on sensitive RPCs).
