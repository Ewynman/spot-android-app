# Task 01 — Pro Edit spot sheet

**Size:** Medium (3–8 h) • **Priority:** P1 • **Status:** Open

## Goal

Let a Pro user edit their own spot's photos, vibes, and location without
re-publishing. Reach parity with the iOS `EditSpotView`.

## Why it matters

Android currently ships Share and Delete on the spot overflow menu but
**no Edit action**. This is a documented parity gap
(`docs/README.md` → "Open follow-ups"). Pro users on Android cannot correct
mistakes on their own posts.

## Contract (inline — read this instead of the iOS Swift file)

### Trigger

- Entry point: **overflow menu on a spot the current user authored**
  (`feature/safety/SafetyFlowHost.kt` → spot overflow menu).
- The Edit row is visible only if:
  - The current user is the author (`spot.userId == session.userId`), **AND**
  - The current user is Pro (`UserSessionHolder.isPro == true`).
- If the user taps Edit while **not** Pro, open the paywall instead
  (existing `AppOverlay.Paywall` flow).

### UI

- Bottom sheet (modal), same visual language as the post composer.
- Three sections, all editable:
  1. **Photos** — current photos in order + add/remove (respects Pro limits:
     up to 5 images).
  2. **Vibes** — current vibe tags + add/remove (respects Pro limits: up to
     5 vibes, 2–30 chars each).
  3. **Location** — current location name + coords + option to re-pick
     via place search.
- Primary action: **Save** (disabled unless something changed).
- Secondary action: **Cancel** (confirms unsaved changes if dirty).
- States: idle, loading (fetching current spot), saving (progress in Save
  button), success (toast + dismiss), error (inline banner).

### Copy

| Slot | String |
|------|--------|
| Title | `Edit spot` |
| Photos section header | `Photos` |
| Vibes section header | `Vibes` |
| Location section header | `Location` |
| Save button | `Save` / `Saving…` |
| Cancel dialog title | `Discard changes?` |
| Cancel dialog body | `Your edits will be lost.` |
| Cancel dialog confirm | `Discard` |
| Cancel dialog dismiss | `Keep editing` |
| Success toast | `Spot updated!` |
| Error banner | `Couldn't save. Try again.` |

### RPC

- Save calls **`update_spot_editor_v1`** (see `data-contracts.md`).
- Any newly added photo must go through the moderation pipeline first:
  upload to `pending_images`, invoke `moderate-image`, and pass approved
  `media_asset_id`s in the RPC payload.
- Do **not** call `update_spot_metadata_v1` from Edit — that's the
  non-Pro metadata-only endpoint.

### Payload shape (verify against server)

Match what iOS `EditSpotEditorSupport.swift` sends. Confirm before shipping:
- `p_spot_id: uuid`
- `p_vibe_tag_ids: uuid[]` (final desired set, in order)
- `p_new_media_asset_ids: uuid[]` (approved new uploads)
- `p_kept_image_ids: uuid[]` (existing images to keep, in order)
- `p_location_name: text | null`
- `p_latitude: double precision`
- `p_longitude: double precision`

If any of the above field names differ on the live server, use the server
names. Don't guess.

### After save

- Optimistically update the spot in `HomeFeedViewModel` / `MapViewModel` /
  `ProfileViewModel` via a `LocalContentUpdateBus` (create it in
  `data/content/` alongside `LocalContentRemovalBus.kt`).
- Emit a `record_feed_event_v1` event of type `updated` (verify enum
  member exists) so the ranker sees the change.

## iOS reference (for reviewers)

- `../spot-ios-app/Spot/Views/PostFlow/EditSpotView.swift`
- `../spot-ios-app/Spot/ViewModels/EditSpotViewModel.swift`
- `../spot-ios-app/Spot/Services/Spots/EditSpotEditorSupport.swift`
- `../spot-ios-app/Spot/Services/Spots/EditSpotPersisting.swift`

## Android target (files to touch)

Create:
- `feature/post/EditSpotSheet.kt` — the bottom sheet composable.
- `feature/post/EditSpotViewModel.kt` — with `EditSpotUiState`.
- `data/post/SpotEditRepository.kt` — interface.
- `data/post/SupabaseSpotEditRepository.kt` — real impl.
- `app/src/test/.../data/post/FakeSpotEditRepository.kt`
- `app/src/test/.../feature/post/EditSpotViewModelTest.kt`
- `data/content/LocalContentUpdateBus.kt` — spot-mutation event bus.

Wire:
- Add `Edit spot` row to `feature/safety/SafetyFlowHost.kt` spot overflow
  menu (visible only when owner + Pro; else paywall trigger).
- Add `EditSpotRepository` binding in `di/PostModule.kt`.
- Subscribe `HomeFeedViewModel`, `MapViewModel`, `ProfileViewModel` to
  `LocalContentUpdateBus` and update local state.

Preserve:
- Do **not** duplicate the post composer's photo editor. Reuse
  `feature/post/PostComposerSteps.kt` composables where practical.
- Reuse `data/post/ImageProcessor.kt` for downscale.
- Reuse `SupabaseSpotPublishRepository.uploadAndModerate(...)` logic; if
  that helper isn't public, factor a shared helper out.

## Acceptance criteria

- [ ] Owner + Pro sees Edit spot in the overflow menu.
- [ ] Non-owner or non-Pro **cannot** see Edit spot (non-Pro owner sees
      paywall trigger instead).
- [ ] Photos section: reorder, add, remove; Pro cap of 5 respected.
- [ ] Vibes section: add / remove; 2–30 char validator; Pro cap of 5.
- [ ] Location section: re-pick via places search or keep current.
- [ ] Save disabled unless dirty; enabled after any change.
- [ ] Save shows progress, then `Spot updated!` toast, then dismiss.
- [ ] Cancel with unsaved changes prompts the discard dialog.
- [ ] Error path shows the error banner and retries in place.
- [ ] Feed, map, profile all reflect the update without a manual refresh.
- [ ] Test tag `editSpot.*` used on all interactive elements, matching iOS
      identifier strings.
- [ ] Unit tests: `EditSpotViewModelTest` covers idle / loading / saving /
      success / validation error / server error / discard-with-dirty.

## Test plan

- Run `./gradlew testDebugUnitTest` — new tests pass.
- Manual (staging): edit a Pro user's spot; add + remove a photo; add a
  new vibe; move the location; save; confirm home feed, map, and profile
  all reflect the change.
- Manual: as a non-Pro user, tap Edit → paywall opens; dismissing paywall
  does not open the edit sheet.

## Out of scope

- Editing another user's spot (owner-only).
- Batch edit across spots.
- Undo-save history.

## Follow-ups

- If the server rejects `p_kept_image_ids` when reordering, we may need a
  separate `reorder` step. Confirm on the first shipped edit.
- Consider adding a "keep original" checkbox for re-picked location.
