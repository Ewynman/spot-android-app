# Task 15 — Pro vibe-to-photo mapping (`PHOTO_SYNCED`)

**Size:** Medium (3–8 h) • **Priority:** P1 • **Status:** Open
**iOS reference PR:** [Ewynman/spot-ios-app #89](https://github.com/Ewynman/spot-ios-app/pull/89)

## Goal

Add the **Pro-only "Match Vibes to Photos"** feature: 1:1 mapping
between images and vibe tags, with auto-pair + swap in the composer and
photo-synced display in the viewer. Also rebuild the broken Vibe Tags
bottom sheet.

## Contract

### Composer (post + edit)

- Available when: **Pro** AND `images.count == vibes.count` AND
  `count >= 2`.
- UI: a toggle labeled **`Match Vibes to Photos`** in the vibe step of
  the composer.
- Turning on:
  - Auto-pair vibe `i` ↔ image `i` in order.
  - Swapping a photo swaps its paired vibe.
  - Swapping a vibe swaps its paired photo.
- Turning off: falls back to the default `ROTATING` display mode.
- If image or vibe count diverges (edit-time), show a small inline
  status: `Uneven counts — synced mapping paused.` and fall back to
  rotating.
- If only one photo or only one vibe: hide the mapping control
  entirely.

### Persistence

- `spots.vibe_display_mode`: `"rotating"` (default) or `"photo_synced"`.
- `spot_images.vibe_tag_id`: nullable uuid pointing to the vibe paired
  with that image (only populated when `photo_synced`).
- Both must be sent through `publish_spot_with_approved_media_assets_v1`
  and `update_spot_editor_v1`. **Verify the RPC signature accepts these
  params** — if not, this is a backend task; stop and note.

### Viewer

- When `vibe_display_mode == photo_synced`:
  - Vibe pill on the spot card syncs to the committed carousel page.
  - No auto-rotation of vibes while the vibe sheet is open.
  - Pausing gallery swipe and vibe rotation while the vibe sheet is
    open.
- When `rotating`: existing rotating chip behavior in
  `RotatingVibeTags.kt`.

### Vibe Tags bottom sheet redesign

Broken today per iOS PR #89 comment ("full-width wrapping-chip sheet
with active-vibe highlight"). Verify the current Android sheet:

- Full-width (not compressed).
- Wrapping chips with proper spacing (`Row` with `FlowRow` or wrap).
- Active vibe (the currently displayed one) highlighted with
  primary-fill background.
- Sheet title not compressed at any device width.

## iOS reference

- `../spot-ios-app/Spot/Views/PostFlow/VibeSelectionView.swift`
- `../spot-ios-app/Spot/Views/PostFlow/VibePhotoMappingSection.swift`
- `../spot-ios-app/Spot/Views/Components/VibeTagsSheet.swift`
- `../spot-ios-app/Spot/Models/VibeDisplayMode.swift` (`rotating` |
  `photo_synced`)
- `../spot-ios-app/Spot/Utils/VibePhotoMappingPolicy.swift`

## Android target (files to touch)

Verify / add:
- `data/model/enums/VibeDisplayMode.kt` — enum with `Rotating` and
  `PhotoSynced`; probably already exists (inventory mentions it).
- DTOs: `SpotImageRowDto.vibe_tag_id` (verify present), `SpotRowDto.vibe_display_mode`.

Create:
- `feature/post/VibePhotoMappingSection.kt` — the composer section.
- `data/post/VibePhotoMappingPolicy.kt` — auto-pair, swap-photo,
  swap-vibe pure logic.
- `feature/post/VibeTagsSheet.kt` — redesigned full-width sheet.
- `app/src/test/.../data/post/VibePhotoMappingPolicyTest.kt`
- `app/src/test/.../feature/post/PostViewModelTest.kt` — add cases for
  turning on / off, count divergence, non-Pro.

Edit:
- `feature/post/PostViewModel.kt` — add `vibeDisplayMode`, `imageVibeMap`
  state.
- `data/post/SupabaseSpotPublishRepository.kt` — send new params.
- `core/design/component/SpotCard.kt` — on viewer, if
  `photo_synced`, subscribe vibe to page index; suspend auto-rotate.

## Acceptance criteria

- [ ] Pro user creating a post with 3 photos + 3 vibes sees the
      **Match Vibes to Photos** toggle; enabling it auto-pairs.
- [ ] Swapping a photo swaps its paired vibe; swapping a vibe swaps its
      photo.
- [ ] Non-Pro user does not see the toggle (or sees the paywall on tap).
- [ ] Publishing a `photo_synced` spot: `spots.vibe_display_mode = 'photo_synced'`
      and `spot_images.vibe_tag_id` populated correctly.
- [ ] Viewer with `photo_synced`: vibe pill matches the committed
      carousel page; no auto-rotate.
- [ ] Edit flow allows toggling on / off; uneven counts fall back with
      the inline status.
- [ ] Single photo or single vibe: mapping control hidden.
- [ ] Vibe Tags sheet: full-width, wrapping chips, active highlight,
      title not compressed.
- [ ] Unit tests cover the mapping policy edge cases.
- [ ] `./gradlew testDebugUnitTest lintDebug assembleDebug` green.

## Test plan

- Manual (Pro): compose 3+3 → enable mapping → publish → view.
- Manual (Pro): edit an existing `rotating` spot → enable mapping →
  save → view.
- Manual (free): confirm no toggle.
- Manual: 3 photos + 2 vibes → toggle hidden or disabled.

## Out of scope

- Custom vibe creation flow (already handled).
- Backend changes to RPC signatures.

## Follow-ups

- If the mapping policy grows more rules (e.g., drag-to-remap), extract
  a dedicated Compose subtree.
