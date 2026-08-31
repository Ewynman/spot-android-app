# Task 16 — Post + feed UI polish batch

**Size:** Small (1–3 h) • **Priority:** P2 • **Status:** Open
**iOS reference PR:** [Ewynman/spot-ios-app #82](https://github.com/Ewynman/spot-ios-app/pull/82)

## Goal

Ship the small UX + visual polish batch iOS shipped in PR #82: extend
map behind top safe area, larger cream + Spot-green composer/search
fields, visible photo reorder controls, crop grid fitted to the
displayed image, labeled Save Draft action, determinate publish
progress, restore current-user identity on optimistic feed cards, and
anchor card menus to the tapped button.

## Contract (item by item)

### 1. Map canvas behind top status bar

- Google Map fills the top of the screen (behind the status bar area).
- Any overlay UI (recenter, filter pills) still respects the status
  bar inset.

### 2. Cream surfaces stay light + Spot-green composer/search fields

- Composer text fields and Search query field use:
  - Background: `SpotColors.background` (cream, `#F5F3EF`).
  - Border / focused ring: `SpotColors.primary` (deep green).
  - Text size: bump one step up (bodyLarge → 18sp).
- No dark mode / system tint bleed-through on cream surfaces.

### 3. Visible photo reorder controls

- In the composer photos step, each photo card shows a small drag
  handle (three horizontal lines icon) at the top-left. Long-press or
  drag-handle press → drag to reorder.
- Reorder is committed on drop; hint toast `Reordered` for the first
  reorder in a session.

### 4. Crop grid fits displayed image

- The 3×3 crop grid overlay resizes with the image container (not the
  original image size). When aspect ratio changes, the grid stays
  aligned with the visible image.

### 5. Save Draft action (replace "…" options control)

- Final composer step: replace the ambiguous options icon with a
  labeled **`Save Draft`** button (secondary style: cream fill, primary
  border).
- Tapping shows the existing save-draft confirmation.

### 6. Determinate publish progress bar

- The publish banner in Home shows a determinate progress bar with
  three stages (mirror iOS):
  - **Uploading** (0–50%)
  - **Moderating** (50–75%)
  - **Finalizing** (75–100%)
- Report progress from `SpotPublishCoordinator` via a
  `StateFlow<PublishProgress>` (create `PublishStage` enum + %).

### 7. Restore current-user identity on optimistic feed cards

- When the user publishes, the optimistic feed insert must carry the
  current user's `username` and `profileImageUrl` from
  `UserSessionHolder`, not blank.
- Verify: no more empty author row on the top card after posting.

### 8. Anchor card menus to the tapped button

- Overflow menu on `SpotCard`: use Material's built-in anchor
  (`DropdownMenu` with the trigger as anchor); do **not** overlay
  from the top-left of the card.
- Verify on cards near the bottom of the screen — menu opens upward
  when there isn't room below.

## iOS reference

- PR 82 files (see `../spot-ios-app/`):
  - `Views/PostFlow/PostFlowView.swift` — Save Draft button.
  - `Views/PostFlow/PhotoSelectionView.swift` — reorder handles.
  - `Views/PostFlow/SpotPhotoEditorView.swift` — crop grid.
  - `Services/Spots/SpotPublishCoordinator.swift` — stages.
  - `ViewModels/FeedViewModel.swift` — optimistic author fill.
  - `Views/Components/SpotCard.swift` — anchor preference.

## Android target (files to touch)

Edit:
- `feature/map/MapScreen.kt` — allow the map behind the status bar.
- `feature/post/PostComposerSteps.kt` — composer/search field style;
  drag handles; Save Draft button.
- `feature/search/SearchComponents.kt` — search field style.
- `feature/post/PhotoEditor.kt` (if present) or the crop composable —
  fit grid to displayed image.
- `data/post/SpotPublishCoordinator.kt` — expose stage + %.
- `feature/home/HomeScreen.kt` — determinate banner.
- `feature/home/HomeFeedViewModel.kt` — optimistic insert with author
  fields from `UserSessionHolder`.
- `core/design/component/SpotCard.kt` — verify overflow menu anchor.

Tests:
- Add / extend where the polish is testable (progress stage % math,
  optimistic author fields).

## Acceptance criteria

- [ ] Map canvas extends behind status bar; overlays still respect
      inset.
- [ ] Composer + search fields: cream fill, primary border, larger
      text.
- [ ] Drag handles visible on photo cards; drag reorders.
- [ ] Crop grid fits the displayed image at all aspect ratios.
- [ ] Final composer step shows labeled `Save Draft` button.
- [ ] Publish banner shows determinate progress with three stages.
- [ ] Optimistic feed insert shows current user's username + avatar,
      never blank.
- [ ] Card overflow menu opens adjacent to the tapped ⋮ button,
      auto-flipping upward near the bottom.
- [ ] `./gradlew testDebugUnitTest lintDebug assembleDebug` green.

## Test plan

- Manual: publish a spot → progress bar advances through 3 stages →
  optimistic card shows author → banner disappears.
- Manual: open overflow on the last visible card → menu flips upward.
- Manual: crop with a portrait image, then a square → grid resizes.

## Out of scope

- Any change to composer step flow.
- Any change to search algorithm.

## Follow-ups

- Consider a shared `SpotTextField` composable if the styling recurs.
