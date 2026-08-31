# Task 31 — Private account test suite

**Size:** Medium (3–5 h) • **Priority:** P3 • **Status:** Open
**iOS reference PRs:** [#38](https://github.com/Ewynman/spot-ios-app/pull/38) and [#36](https://github.com/Ewynman/spot-ios-app/pull/36)

## Goal

Match the iOS private-account test-suite parity so Android's private
flows are as well-guarded as iOS.

## Contract

Cover these scenarios with unit + instrumented tests. Where a
scenario needs the network, use the existing repository fakes.

### Follow requests

- **Requester sends** a follow request → the target user's
  `follow_requests` row is created with status `pending`.
- **Target accepts** → `follows` row created; `follow_requests` row
  removed; local `FOLLOW_ACCEPTED` notification fires.
- **Target declines** → `follow_requests` row removed; no notification.
- **Requester cancels** → row removed.

### Private profile viewing

- Non-follower sees the private state on Profile Spots (task 24).
- Non-follower cannot open a spot detail (via deep link) posted by a
  private account they don't follow — overlay shows
  `SpotUnavailableOverlay`.
- Follower sees spots as normal.

### Feed filtering

- Private accounts' spots do not appear in the non-follower's home
  feed (server-side RLS).
- Confirm empty result surfaces the correct empty state, not an
  error.

### Search

- Users search returns private accounts with the "private" indicator.
- Tapping a private user in search opens their profile with the
  private state.

### Block interactions

- Blocking a user removes their pending follow request.
- Blocking a user makes their profile 404-equivalent from the blocker's
  side (403 / empty result).
- Unblock does not auto-restore any follow relationship.

## iOS reference

- PRs 38 / 36 (see `../spot-ios-app/`):
  - `SpotTests/PrivateAccount*Tests.swift`
  - `SpotUITests/PrivateAccount*Tests.swift`

## Android target (files to touch)

Create:
- `app/src/test/.../feature/profile/FollowRequestsViewModelPrivateAccountTest.kt`
- `app/src/test/.../feature/profile/ProfileViewModelPrivateAccountTest.kt`
- `app/src/test/.../feature/home/HomeFeedViewModelPrivateAccountFilterTest.kt`
- `app/src/test/.../feature/search/SearchViewModelPrivateAccountTest.kt`
- `app/src/test/.../feature/safety/SafetyViewModelPrivateBlockTest.kt`

Instrumented (androidTest):
- `feature/overlay/SpotDetailOverlayPrivateAccountTest.kt` — deep-link
  overlay routes to `SpotUnavailableOverlay` for a private
  non-followed spot.

Fakes:
- Extend fakes with a `configureAsPrivate(userId: String)` helper so
  each scenario sets up cleanly.

## Acceptance criteria

- [ ] All scenarios above have unit or instrumented tests.
- [ ] Tests pass consistently (no flakes across 3 runs).
- [ ] `./gradlew testDebugUnitTest connectedAndroidTest` green.

## Test plan

- Run the suite 3× locally; no flakes.
- Ensure fakes covered do not leak state between tests (JUnit `@Before`
  reset).

## Out of scope

- Any code changes to feature behavior.
- Backend / RLS testing.

## Follow-ups

- If flaky, extract a shared `PrivateAccountFixtures` helper.
