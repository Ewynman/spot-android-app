# 18 — Logging

Spot uses **structured, category-based logging** with per-component and per-category toggles, an errors-only floor in release builds, and a "map-only" noise-reduction mode. This document specifies the contract to reproduce on Android so log output is consistent across platforms and safe for production.

Reference implementation (iOS): `Spot/Utils/SpotLogger.swift`, `Spot/Utils/LoggingConfig.swift`, and 60+ per-component log enums under `Spot/Models/Logs/`.

## Goals

1. **Structured** — every log has a stable component `tag`, a `level`, and a human-readable `message`, plus an optional key-value `details` block.
2. **Filterable** — enable/disable by **severity**, by **debug category**, or by **specific component**.
3. **Safe** — no PII, tokens, or raw image bytes; release builds log **errors only** by default.
4. **Low-noise** — a master switch and a special "map-only" mode to isolate map debugging.
5. **Consistent** — the same tags, categories, and level assignments as iOS so shared debugging vocabulary works.

## Log levels

`LogLevel`: **debug < info < error** (ordered for filtering).

| Level | When | Release default |
|-------|------|-----------------|
| `debug` | Verbose tracing; requires category/component enablement | suppressed |
| `info` | Important lifecycle/business events | suppressed unless raised |
| `error` | Failures; **always emitted** (even when the master switch is off) | emitted |

`minimumLevel` defaults to `info`; debug output additionally requires explicit category/component enablement. In non-debug (release) builds, `LoggingConfig` sets the floor to **errors only**.

## Debug categories

`DebugCategory` (raw string in parentheses), matching iOS exactly:

| Category | Raw | Use |
|----------|-----|-----|
| ui | `UI` | Taps, appears, UI interactions |
| navigation | `Navigation` | Navigation/route events |
| feed | `Feed` | Feed loading, ranking, diversity |
| network | `Network` | API/RPC/remote data operations |
| auth | `Auth` | Authentication events |
| image | `Image` | Image loading/caching |
| location | `Location` | Location services |
| performance | `Performance` | Performance metrics |
| deepLink | `DeepLink` | Deep linking |
| moderation | `Moderation` | Content moderation |
| privacy | `Privacy` | Privacy filtering |

Categories can be enabled individually, or all at once via a master switch.

## Enablement precedence (for a debug log to emit)

Evaluate in this order (mirror `shouldLogDebug`):
1. In debug builds, the master **"console logging enabled"** switch must be on (errors bypass this).
2. If the **enable-all-debug** master switch is on → emit.
3. Else if the **specific component** flag for the calling file is on → emit.
4. Else if the log's **category** is enabled → emit.
5. Otherwise → suppress.

`info` and `error` are not gated by category enablement (but the debug-build master switch still suppresses non-error output when off).

## Persisted toggles (DataStore keys)

Mirror these keys (from `Constants.UserDefaultsKeys`) so the debug settings screen behaves the same:

| Key | Meaning |
|-----|---------|
| `debugLoggingEnabled` | Master "console logging" switch (debug builds; default on in debug) |
| `logAllDebugCategories` | Treat all debug categories as enabled |
| `logSpotCard` | Component: spot card |
| `logPrivacy` | Component/category: privacy |
| `logFeedComponent` | Component: feed views/repository |
| `logPostFlow` | Component: post flow |
| `logAuth` | Component/category: auth |
| `logNetworkComponent` | Component: network |
| `logDeepLink` | Component/category: deep link |

These are surfaced in Settings → Debug → Console logging (debug builds only; see [11-settings.md](11-settings.md)).

## Component logging flags

In addition to categories, individual components can be toggled by file/component (mirror `ComponentLogging`): spot card, profile view, search view, feed view, author-privacy cache, feed repository, feed ranker, spot service, auth service, image service, deep-link router, auth view model, likes view model, bookmarks view model, post flow, location selection, photo selection. On Android, key these by a stable component identifier (e.g. the class/tag name) rather than a source file name.

## The `SpotLog` pattern (per-component enums)

Each component defines an enum of its log events, each case mapping to a `tag`, `level`, and `message`. This keeps log strings centralized, consistent, and reviewable. Kotlin equivalent:

```kotlin
interface SpotLog {
    val tag: String        // originating component name, shown as the log tag
    val level: LogLevel
    val message: String
}

enum class FeedRepositoryLog(
    override val level: LogLevel,
    override val message: String
) : SpotLog {
    LOAD_INITIAL(LogLevel.DEBUG, "Feed loadInitial"),
    LOAD_INITIAL_FAILED(LogLevel.ERROR, "Feed loadInitial failed"),
    LOAD_MORE(LogLevel.DEBUG, "Feed loadMore"),
    LOAD_MORE_FAILED(LogLevel.ERROR, "Feed loadMore failed"),
    DIVERSITY_PASS_APPLIED(LogLevel.INFO, "Home feed diversity pass applied"),
    FEED_ENRICH_FAILED(LogLevel.DEBUG, "Feed spot enrichment failed");

    override val tag: String get() = "FeedRepository"
}
```

Emit with a central logger, optionally attaching a details map:

```kotlin
SpotLogger.log(FeedRepositoryLog.LOAD_INITIAL_FAILED, details = mapOf("error" to e.message))
```

Reproduce a per-component log enum for each major screen/service (iOS ships ~62 of them, e.g. `AuthServiceLogs`, `FeedRepositoryLogs`, `MapViewModelLogs`, `SpotPublishCoordinatorLogs`, `DeepLinkRouterLogs`, `ModerationServiceLogs`, `PaywallViewLogs`, …). You don't need all 62 up front — add one per component as you build it, keeping tags identical to iOS.

## Output format

Structured entry format (match iOS for readability):

```
SpotLogger: <tag>
<message>
[
     key: value
     key2: value2
]
```

- Header line `SpotLogger: <tag>`, then the message, then a sorted key-value details block in `[ ]` (omit the block when there are no details).
- The underlying line written to the platform log includes level + file/line + function context, e.g. `[SpotLogger][ERROR] FeedRepository.kt:42 | loadInitial | <body>`.
- **Sort detail keys** for stable, diffable output. Render dates/arrays readably.

## Platform sink [ANDROID DECISION]

- iOS writes through Apple's unified logging (`os.Logger`) with per-level types so Xcode's type filter works, and deliberately avoids `print()` fallbacks.
- **Android:** write through `android.util.Log` (or `Timber`) mapping `debug→Log.d`, `info→Log.i`, `error→Log.e`, tagged with the component tag. Do **not** add a `println` fallback that bypasses level metadata.
- Forward `error`-level logs to **Crashlytics** (`recordException` / `log`) so production failures are captured, while keeping debug/info local-only.

## Map-only mode

Provide an equivalent of `mapOnlyLoggingEnabled`: when on, suppress all output **except** map-related components (tags starting with `Map`, `LocationManager`, and the map viewport RPC `get_map_spots_v1`). Useful for isolating map debugging without unrelated noise. This does not affect OS/system map framework logs.

## Release behavior (must-haves)

- Non-debug builds: **errors only** floor; debug/info suppressed.
- The debug console-logging screen and its toggles are **debug-build only**.
- **Never** log: access/refresh tokens, passwords, OTP codes, raw image bytes, full email addresses (mask like the OTP screen does), or precise auth secrets.
- Prefer stable identifiers (spotId, userId) over free-text PII in details.

## Convenience: logging a Spot payload

iOS offers helpers that merge a `Spot` into the details block (spotId, userId, username, likes, createdAt, imageURL, thumbnailURL, locationName). Provide the same convenience on Android so spot-related logs are uniform.

## Checklist (Android)

- [ ] `LogLevel` (debug/info/error) with ordering.
- [ ] `DebugCategory` with the 11 categories + raw strings above.
- [ ] `SpotLog` interface + per-component enums (tags identical to iOS).
- [ ] Central `SpotLogger` with the enablement precedence and output format.
- [ ] DataStore-backed toggles with the keys above + debug settings screen.
- [ ] Component-level flags.
- [ ] Map-only mode.
- [ ] Errors-only floor in release; errors forwarded to Crashlytics.
- [ ] No PII/secrets in logs.
