# Task 30 — Structured logging profiles parity (0–4)

**Size:** Small (1–3 h) • **Priority:** P3 • **Status:** Open
**iOS reference PR:** [Ewynman/spot-ios-app #64](https://github.com/Ewynman/spot-ios-app/pull/64)

## Goal

Bring Android's `SpotLogger` in line with iOS's unified logging: single
**0–4 profile** setting, standardized output format, privacy-safe
detail formatting, and high-frequency debug events suppressed outside
profile 4.

## Contract

### Profile levels

| Profile | Behavior |
|---------|----------|
| 0 | Errors only |
| 1 | **Default in Release** — errors + warnings + selected structured info |
| 2 | + info for user flows (auth, publish, feed) |
| 3 | + debug for non-hot-path (VM state changes) |
| 4 | + high-frequency debug (feed events, image loads, map render, deep-link router, analytics) |

Noisy tags suppressed unless profile 4 (mirror iOS):
`Analytics`, `DeepLinkRouter`, `FeedEventService`, `ImageService`,
`MapMarker`, `SearchHistory`.

### Output format

- One line per log:
  `{Tag} {Message}` followed by a sorted details dictionary on the
  same line (`key=value, key=value`), sorted alphabetically by key.
- No trailing newlines besides the log framework's default.

Example:

```
FeedRepository home_feed_load status=ok, count=24, hasMore=true
```

### Privacy-safe details

- `String` values redacted to `<redacted:len=N>` when they match any of:
  - Email regex.
  - UUID regex.
  - JWT-shaped tokens.
- Numeric IDs (spot IDs from public rows) may be logged unredacted at
  profile 4.
- Explicit "safe" markers: fields prefixed with `_safe_` (e.g.
  `_safe_count`) skip redaction — use sparingly.

### Debug artifacts

- Rotating log files for detached Debug builds (Xcode-attached iOS
  runs use os_log; Android's equivalent is Logcat when attached,
  file rotation when not).
- Rotation: 5 files × 512 KB each in
  `context.getExternalFilesDir("logs")` (accessible via ADB pull).
- Only in Debug builds.

### Settings

- Settings → Debug → Logging profile: shows the 5 options with
  descriptions.
- Change is applied immediately (no restart required).

## iOS reference

- PR 64 (see `../spot-ios-app/`):
  - `Utils/SpotLogger.swift`
  - `Config/LoggingDefaults.plist` (default profile 1).
  - `Models/Logs/*.swift` (per-domain log enums).

## Android target (files to touch)

Verify + edit:
- `core/logging/SpotLogger.kt` — profile enum 0–4; noisy-tag suppression;
  standardized format.
- `core/logging/LogPreferences.kt` — `profile: Int` (0..4). Default 1.
- `core/logging/LogWriter.kt` — rotating files in Debug when Logcat
  isn't attached (detect via `Debug.isDebuggerConnected()` — if false
  in Debug build, write to file).
- `core/logging/Redaction.kt` (new) — pure redaction utility with
  tests.
- `feature/settings/DebugLoggingScreen.kt` — 5 profile options + copy.
- `app/src/test/.../core/logging/RedactionTest.kt`,
  `SpotLoggerFormatTest.kt`.

## Acceptance criteria

- [ ] Log line format matches: `Tag Message key=value, key=value`.
- [ ] Keys always sorted alphabetically.
- [ ] Emails, UUIDs, JWTs redacted at all profiles.
- [ ] Noisy tags silent outside profile 4.
- [ ] Debug builds without a debugger write rotating log files.
- [ ] Settings → Debug → Logging profile lists 5 options and applies
      immediately.
- [ ] Unit tests cover redaction, format, and profile suppression.
- [ ] `./gradlew testDebugUnitTest lintDebug assembleDebug assembleRelease` green.

## Test plan

- Manual: install Debug build, run `adb logcat -s Spot`, cycle through
  profiles 0–4 and verify volume changes.
- Manual: pull rotating log file from `getExternalFilesDir("logs")`
  and inspect format + redactions.

## Out of scope

- Any change to Firebase Analytics events.
- Log ingestion pipelines.

## Follow-ups

- Consider auto-uploading Debug logs to Firebase Crashlytics as
  breadcrumbs.
