# Task 13 — Home Spot card: place-first + map flip

**Size:** Medium (3–8 h) • **Priority:** P1 • **Status:** Open
**iOS reference PR:** [Ewynman/spot-ios-app #90](https://github.com/Ewynman/spot-ios-app/pull/90)

## Goal

Rebuild the Home feed card as a **place-first flippable card**: front
face shows title/vibe/photo/author + Like/Save; back face shows a map
snapshot with the canonical branded marker. Add **Open in Map** routing
into the Map tab with the spot pre-focused, and a one-shot Home return
scroll.

## Contract

### Layout

**Front (photo side):**

- Location name (bold) at top of chrome.
- Vibe chip immediately below the location.
- Photo (the primary image; existing carousel behavior preserved).
- "Shared by @username" below photo.
- Interaction bar: Like, Save, **map-flip toggle** (green marker icon).

**Back (map side):**

- Static map snapshot centered on the coordinates with the branded
  green marker at the tip.
- "Open in Map" pill button overlaid at the bottom right.
- Tapping the map-flip toggle again flips back to the photo.

### Behavior

- **Flip animation:** rotate around Y-axis, 400 ms. Crossfade on
  Reduce Motion (respect `AccessibilityManager.isReduceMotionEnabled`).
- **Invalid coordinates** (`lat==0 && lng==0`, or `null`): disable the
  flip toggle; make it non-interactive.
- **Open in Map:**
  1. Switch to Map tab.
  2. Move the camera to the spot's coordinates.
  3. Select the spot (open the compact preview).
  4. Persist a one-shot `pendingHomeReturnScroll = spotId` so that
     when the user returns to Home, the feed scrolls to that spot.
- **Flip alone does not** trigger the Home return scroll — only
  Open in Map does.
- Interaction bar buttons (Like, Save, overflow) behave identically on
  both faces.

### Map snapshot

- Reuse Google Maps Compose static-map path or a small `MapView` with
  interaction disabled.
- Cache the snapshot per `(spotId, size)` for reuse when the card is
  re-created after scroll.

### First-run onboarding

- Update the first-run coach steps in `feature/onboarding/` to
  highlight:
  - Location name + vibe chip prominence.
  - The map-flip control.
- Do **not** touch the coach step count if possible — replace copy /
  target selectors only.

## iOS reference

- PR 90 files (from `../spot-ios-app/`):
  - `Views/Home/HomepageView.swift`
  - `Views/Components/SpotCard.swift`
  - `Views/Components/HomeSpotMapPreview.swift`
  - `Services/Map/MapFocusCoordinator.swift`

## Android target (files to touch)

Create:
- `feature/home/HomeSpotMapPreview.kt` — the back face composable.
- `data/map/MapFocusCoordinator.kt` — shared cross-tab focus dispatcher
  (already exists on iOS; may already exist as a bus on Android — reuse
  `TabReselectBus` pattern if so).
- `app/src/test/.../feature/home/HomeSpotCardModelTest.kt` — model +
  place-formatting logic.
- `app/src/test/.../data/map/MapFocusCoordinatorTest.kt`.

Edit:
- `core/design/component/SpotCard.kt` — front-face layout reorder
  (place first, vibe below), interaction bar map-flip toggle.
- `feature/home/HomeScreen.kt` — track and consume one-shot
  `pendingHomeReturnScroll`.
- `feature/map/MapScreen.kt` / `MapViewModel.kt` — accept an
  incoming focus request and center + select.
- `feature/onboarding/SpotFirstRunOnboardingOverlay.kt` — updated coach
  copy/targets.

## Acceptance criteria

- [ ] Home feed card renders in place-first order: title, vibe, photo,
      shared-by, interaction bar.
- [ ] Map-flip toggle animates the flip (rotate on default, crossfade
      with Reduce Motion).
- [ ] Invalid coordinates disable the flip toggle.
- [ ] Open in Map switches to Map tab, camera moves, spot is selected
      and preview is visible.
- [ ] Returning to Home after Open in Map scrolls back to that spot.
- [ ] Flip alone does **not** trigger the scroll behavior.
- [ ] Like / Save / collections / overflow work identically on both
      faces.
- [ ] First-run coach targets the new chrome and the map-flip control.
- [ ] Profile / Search / deep-link surfaces still use the shared
      `SpotCard` — front-face changes propagate; back-face flip stays
      opt-in via prop (default off for those surfaces).
- [ ] Unit tests cover the model + focus coordinator.
- [ ] `./gradlew testDebugUnitTest lintDebug assembleDebug` green.

## Test plan

- Manual: post a spot at known coords, verify all four states (flip,
  open in map, return scroll, invalid-coords disable).
- Manual: Reduce Motion on → crossfade instead of rotate.
- Manual: run through the coach on first launch after clearing
  DataStore → new targets highlight correctly.

## Out of scope

- Changes to the Map preview drawer (see task 14).
- Any change to Search or Profile grid cards.
- Any change to the shared `SpotCard` API that breaks other callers.

## Follow-ups

- If snapshot caching gets memory-heavy, add a size limit.
