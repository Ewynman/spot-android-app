# Task 21 — Map recenter behavior + fresh permission checks

**Size:** Small (1–3 h) • **Priority:** P2 • **Status:** Open
**iOS reference PRs:** [#69](https://github.com/Ewynman/spot-ios-app/pull/69) and [#33](https://github.com/Ewynman/spot-ios-app/pull/33)

## Goal

Two related fixes:

1. **Recenter re-arm.** When the user taps recenter while location is
   authorized but no fix has arrived yet, arm a flag so the fix that
   arrives moments later actually centers the camera (today it may be
   ignored as a routine update).
2. **Fresh permission status** for the recenter button. Refresh
   permission from the system on tap; don't rely on stale cached
   status. The recenter button must never show a permission prompt
   after the user has already granted permission.

## Contract

### Recenter behavior

- Tapping recenter:
  1. Refresh permission status via
     `PermissionsRepository.getPermissionState(LOCATION)` (fresh check).
  2. If `NotDetermined`: launch the permission pre-prompt.
  3. If `Granted`:
     - If we already have a `userLocation` fix: center the camera on
       it immediately.
     - If not: set `pendingRecenter = true`. When the next
       `FusedLocationProvider` fix arrives, center on it if
       `pendingRecenter` is true, then clear the flag.
  4. If `Denied` / `PermanentlyDenied`: center on cached last-known
     location if any; else show a small toast: `Location unavailable`.

### Camera intent hardening

- `SharedSpotMap` (Compose Map wrapper) should ignore camera intents
  that equal the last-applied intent (no-op re-emission).
- After a programmatic move completes, clear the pending intent so a
  repeat recenter is a *new* move (not deduplicated to the previous
  identical intent).

### Recenter control visibility

- Show the recenter control when any of:
  - `NotDetermined`
  - `Granted` (authorized when-in-use / always)
  - `Denied` / `Restricted` **with** a cached location
- Hide when denied AND no cached location.

## iOS reference

- PR 69 (see `../spot-ios-app/`):
  - `Managers/LocationManager.swift` (main-actor init — this is iOS
    specific, Android's `FusedLocationProviderClient` doesn't share
    the CLLocationManager threading issue).
  - `Views/Home/MapView.swift` — `hasCenteredOnUser` re-arm.
  - `Views/Components/Map/SharedSpotMap.swift` — camera intent
    deduplication.
- PR 33:
  - `Views/Home/MapView.swift` — use fresh `permissionManager.locationStatus`
    on recenter, not the manager's cached status.

## Android target (files to touch)

Edit:
- `feature/map/MapViewModel.kt` — `pendingRecenter` state, camera intent
  dedupe, permission freshness on recenter.
- `feature/map/MapScreen.kt` — call `permissionsRepository.getPermissionState(...)`
  fresh on recenter tap.
- `data/location/AndroidMapLocationTracker.kt` — verify fixes are
  emitted post-authorization change.
- `data/permissions/AndroidPermissionsRepository.kt` — verify
  `getPermissionState` reads the current system state (not a cached
  `Flow` value).

Tests:
- `MapViewModelTest` — recenter branches; camera intent dedupe;
  pending-recenter flow.

## Acceptance criteria

- [ ] Tapping recenter with permission already granted centers
      immediately (no permission sheet).
- [ ] Tapping recenter with `NotDetermined` triggers the pre-prompt
      once.
- [ ] Tapping recenter before a fix arrives → next fix centers.
- [ ] Repeat recenter tap on the same coord re-emits (not deduplicated
      as a no-op).
- [ ] Denied + no cached location → recenter hidden.
- [ ] `./gradlew testDebugUnitTest lintDebug assembleDebug` green.

## Test plan

- Manual (grant permission): tap recenter → no prompt, centers.
- Manual (fresh install): tap recenter → pre-prompt → grant → next fix
  centers.
- Manual (denied): recenter hidden until you visit a location with a
  cached fix.

## Out of scope

- Any rewrite of `FusedLocationProviderClient` usage.
- Background location.

## Follow-ups

- Consider showing "Waiting for GPS fix…" microcopy while
  `pendingRecenter` is true.
