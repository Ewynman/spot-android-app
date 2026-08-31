# Task 02 — Collection picker on Search + Profile bookmark paths

**Size:** Small (1–3 h) • **Priority:** P1 • **Status:** Open

## Goal

When a Pro user bookmarks a spot from the Search grid or the Profile
grid, present the same **collection picker sheet** that Home and Map
already use. Non-Pro users continue to see the paywall on the 51st save
as today.

## Why it matters

Home and Map already show the collection picker on bookmark (see
`feature/home/HomeScreen.kt` + `feature/map/MapScreen.kt`). Search and
Profile do not, so Pro users have inconsistent behavior between tabs.
This is a documented parity gap (`docs/README.md` → "Open follow-ups").

## Contract

### Behavior

On bookmark tap (star / bookmark icon) from Search grid, Search expanded
spot, Search user-profile grid, or Profile spot grid:

1. If user is not signed in → auth gate flow (existing).
2. If not Pro:
   - If free bookmark cap not reached: bookmark and show `Saved` toast
     (existing).
   - If cap reached: open paywall (existing).
3. If Pro: open `CollectionPickerSheet` seeded with the current bookmark
   state.

Removing a bookmark still runs `remove_saved_spot_v1` and does **not**
open the picker.

### UI

- Reuse `feature/collections/CollectionPickerSheet.kt` — no visual
  changes.
- The sheet already handles: pick collections, create collection inline,
  save without adding to a collection, remove, error toast.

### RPCs

- Bookmark toggles go through `EngagementRepository`:
  - Add: insert into `spot_bookmarks` via `SupabaseEngagementRepository`.
  - Remove: `remove_saved_spot_v1`.
- Collection membership goes through `CollectionsRepository`.

No new RPCs. This is a wiring task.

## iOS reference (for reviewers)

- `../spot-ios-app/Spot/Views/Components/SpotCard.swift` — bookmark tap
  path (Pro branch opens `CollectionManagerSheet`).
- `../spot-ios-app/Spot/Views/Profile/BookmarksCollectionsScreen.swift`
- `../spot-ios-app/Spot/Views/Search/SearchView.swift` — bookmark handler
  on grid cells.

## Android target (files to touch)

- `feature/search/SearchScreen.kt` and/or `feature/search/SearchComponents.kt` —
  wire bookmark taps to the picker when Pro.
- `feature/profile/ProfileScreen.kt` and/or
  `feature/profile/ProfileComponents.kt` — same wiring on the spot grid
  and expanded spot.
- Reuse the picker host pattern already in `HomeScreen.kt` / `MapScreen.kt`.
  Extract the wiring helper (e.g. `BookmarkFlow.rememberBookmarkFlow(...)`)
  under `feature/collections/` if it isn't already shared.
- `app/src/test/.../feature/search/SearchViewModelTest.kt` — add cases
  for `onBookmarkTapped` when Pro / non-Pro / at cap.
- Same for `ProfileViewModelTest.kt`.

## Acceptance criteria

- [ ] Pro user tapping bookmark on Search grid opens the collection picker.
- [ ] Pro user tapping bookmark on Search expanded spot opens the picker.
- [ ] Pro user tapping bookmark on a Search-viewed user profile's grid
      opens the picker.
- [ ] Pro user tapping bookmark on their own or another user's Profile
      spot grid opens the picker.
- [ ] Free user at cap gets the paywall from all four entry points.
- [ ] Free user under cap gets the classic `Saved` toast (no picker).
- [ ] Removing a bookmark from any of these entry points still uses
      `remove_saved_spot_v1` and does not open the picker.
- [ ] Test tags mirror Home/Map picker tags (`collections.pickerSheet`, …).
- [ ] Unit tests cover the branching in Search and Profile ViewModels.

## Test plan

- `./gradlew testDebugUnitTest`
- Manual: sign in as Pro, bookmark from all four surfaces → picker opens.
- Manual: sign in as free at 50 bookmarks, tap bookmark → paywall.
- Manual: sign in as free under cap, tap bookmark → toast, no picker.

## Out of scope

- Adding a Manage collections action on Search/Profile overflow menus
  (that lives on the spot detail overlay).
- Any changes to bookmark caps or collection limits.
- Any UI changes to the picker itself.

## Follow-ups

- If the shared helper is extracted, consider migrating Home + Map to it
  in the same PR for consistency — but only if the diff stays small.
