# Task 18 — Feed + profile + post behavior fixes

**Size:** Medium (3–8 h) • **Priority:** P2 • **Status:** Open
**iOS reference PR:** [Ewynman/spot-ios-app #73](https://github.com/Ewynman/spot-ios-app/pull/73)

## Goal

Ship the batch of behavior fixes iOS shipped in PR #73: keep newly
published spots pinned at top of the feed without replacing it, isolate
pagination loading UI, retain profile view model across tab switches,
hide profile chrome while viewing a spot, use one branded full-screen
delete confirmation, preserve venue names until the map pin
meaningfully moves, reject stale reverse-geocode callbacks, and
parallelize profile hydration queries.

## Contract (per item)

### 1. New spot pinned at top; don't refresh the whole feed

- On publish success, insert the synthetic spot at index 0 of the feed
  list. **Do not** trigger a full `refresh()` that replaces the list —
  users lose scroll position.
- The next natural refresh (pull-to-refresh or app cold start) will
  reconcile with the server.

### 2. Isolate pagination loading UI

- When paginating (loading page N+1), the loading indicator lives at
  the bottom of the list — **not** as a full-screen loader that hides
  the top of the feed.
- Initial-load and refresh still use the top-of-feed skeleton.

### 3. Retain profile ViewModel across tab switches

- If the user opens their profile then switches tabs and back, the
  profile does **not** re-load from scratch — state is preserved.
- Implementation: scope `ProfileViewModel` to the profile navigation
  entry (Hilt `@ViewModelScoped` per `NavBackStackEntry`).

### 4. Hide profile chrome while viewing a Spot overlay

- When a spot detail overlay is open above the profile (e.g., from
  tapping a grid cell), the profile header + tabs are hidden behind the
  overlay so they don't peek through.

### 5. One branded full-screen delete confirmation

- Delete confirmation is a full-screen branded overlay (matching iOS
  `SpotConfirmationOverlay`), not a Material AlertDialog.
- Copy: `Delete this spot?` + explanatory body + destructive `Delete`
  button + cancel.
- No gray scrim.

### 6. Preserve selected venue name until pin meaningfully moves

- In the post composer location step: when the user selects a nearby
  place, keep that place name until the map pin moves > 50 m (config
  constant).
- Trivial pan jitter must not clear the name.

### 7. Reject stale reverse-geocode callbacks

- The location step performs reverse-geocode on pin move. If the user
  moves the pin quickly, older callbacks arriving late must be
  discarded — key each request by a monotonic sequence number and drop
  results whose sequence is less than the most-recent request.

### 8. Parallelize profile hydration queries

- `ProfileViewModel.load()` currently may run header + spots +
  bookmarks queries serially. Run independent queries in parallel via
  `coroutineScope { launch { ... }; launch { ... } }` or `async`.
- Preserve the loading state until the header is loaded; other tabs
  can populate lazily.

## iOS reference

- PR 73 (see `../spot-ios-app/`):
  - `ViewModels/FeedViewModel.swift`
  - `ViewModels/ProfileViewModel.swift`
  - `Views/PostFlow/LocationSelectionView.swift`
  - `Services/Spots/LocationSelectionPolicy.swift`
  - `Views/Components/SpotConfirmationOverlay.swift`

## Android target (files to touch)

Edit:
- `feature/home/HomeFeedViewModel.kt` — top-pin insert; pagination
  loader isolated.
- `feature/home/HomeScreen.kt` — pagination footer vs top skeleton.
- `feature/profile/ProfileScreen.kt` / `ProfileViewModel.kt` — retain
  VM across tab switches (verify `@HiltViewModel` scoping).
- `feature/profile/ProfileComponents.kt` — hide chrome under overlay.
- Delete confirmation: replace whichever `AlertDialog` fires the delete
  with the branded overlay (use / create
  `core/design/component/SpotConfirmationOverlay.kt`).
- `data/location/AndroidPlaceSearchProvider.kt` — sequence-numbered
  reverse-geocode.
- `feature/post/PostComposerSteps.kt` — meaningful-move policy on the
  location step.
- `feature/profile/ProfileViewModel.kt` — parallelize `async` hydration.

Tests:
- `HomeFeedViewModelTest` — pagination loader case; publish inserts
  at 0.
- `ProfileViewModelTest` — parallel hydration.
- Location policy: unit tests for meaningful-move threshold + stale
  callback rejection.

## Acceptance criteria

- [ ] After publish, feed keeps scroll position; new spot at index 0.
- [ ] Pagination loader is a footer, not full-screen.
- [ ] Switching Profile tab away and back preserves state.
- [ ] Spot overlay on Profile hides the profile header + tabs.
- [ ] Delete confirmation is a branded full-screen overlay, no gray
      scrim.
- [ ] Selected venue name persists across sub-50-m pin jitter.
- [ ] Stale reverse-geocodes are discarded.
- [ ] Profile loads header + spots + bookmarks concurrently.
- [ ] Unit tests cover the location policy and VM changes.
- [ ] `./gradlew testDebugUnitTest lintDebug assembleDebug` green.

## Test plan

- Manual: post a spot with 100 items scrolled → feed doesn't jump.
- Manual: paginate a long feed → footer only.
- Manual: profile tab → other tab → profile → no re-load.
- Manual: on location step, tap a place, then pan slightly → name
  stays.
- Manual: on location step, drag pin quickly across the city → no
  stale name flash.

## Out of scope

- Any RPC changes.
- Redesigning the Profile screen.

## Follow-ups

- If `SpotConfirmationOverlay` becomes shared, use it for block
  confirmation (task 22).
