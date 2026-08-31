# Task 25 — User location marker: initials + silhouette fallback

**Size:** Tiny (< 1 h) • **Priority:** P3 • **Status:** Open
**iOS reference PR:** [Ewynman/spot-ios-app #51](https://github.com/Ewynman/spot-ios-app/pull/51)

## Goal

The current-user marker on the map must always be visible, even
without a profile photo. Show **account initials** if available, else
a **branded person silhouette**. Marker sits above overlapping Spot
pins.

## Contract

### Precedence

1. If profile image is loaded successfully → show avatar as marker.
2. Else if `username` is present → show up to **two initials**
   (from `username.first(1)` if username, or first letters of first
   two words if a display name). Uppercase.
3. Else → branded person silhouette icon.

While the profile image is loading or after a load failure, **retain
the fallback** (initials or silhouette). Do not flash between empty
and loaded.

### Style

- Circle, diameter 44 dp, cream fill, primary-green border 2 dp.
- Initials: labelMedium (12 sp) bold, primary-green color.
- Silhouette: outlined person icon, primary-green.
- z-index above spot pin markers (`markerZIndex = 1f` on user vs
  default `0f`).

## iOS reference

- PR 51 (see `../spot-ios-app/`):
  - `Views/Components/Map/UserLocationAnnotationView.swift`
  - `Utils/UserInitials.swift` (name → initials).

## Android target (files to touch)

Create:
- `feature/map/UserMarkerFallback.kt` — the three-state marker
  composable.
- `core/util/UserInitials.kt` — pure function `initials(username, displayName)`.
- `app/src/test/.../core/util/UserInitialsTest.kt` — edge cases:
  empty, single word, multi-word, non-ASCII.

Edit:
- `feature/map/MapUserLocationMarker.kt` — dispatch to the fallback
  composable based on state.

## Acceptance criteria

- [ ] User with avatar: marker shows avatar.
- [ ] User with no avatar but with username: shows initials.
- [ ] User with neither: shows silhouette.
- [ ] Loading state: fallback holds; no flicker.
- [ ] Marker renders above spot pins (verify by placing a spot pin at
      the user's exact coord).
- [ ] Unit test covers initials extraction.
- [ ] `./gradlew testDebugUnitTest lintDebug assembleDebug` green.

## Test plan

- Manual: sign in as a user with an avatar → avatar visible.
- Manual: sign in as a user without an avatar → initials.
- Manual: change username to a non-ASCII string → renders correctly.

## Out of scope

- Any change to the map camera or pin placement.
- Any change to profile image upload.

## Follow-ups

- Consider the same fallback on the profile header (task 24 covers
  most of this indirectly).
