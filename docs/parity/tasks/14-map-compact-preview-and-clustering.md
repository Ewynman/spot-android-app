# Task 14 — Map compact preview + clustering audit

**Size:** Medium (3–8 h; likely partial gap) • **Priority:** P1
**Status:** Open
**iOS reference PR:** [Ewynman/spot-ios-app #88](https://github.com/Ewynman/spot-ios-app/pull/88)

## Goal

Bring Android's Map tab in line with iOS PR #88: unified `MapExperience`
shell for Global + Profile maps, **compact floating preview card** on
first tap (Like/Save always visible), a **secondary detail sheet** with
a pinned action bar, and MapKit-style **clustering with count badges**.

## Contract

### Preview / detail model

- **Compact preview:** small floating card (~120–140 dp tall) shown on
  first pin tap. Layout: thumbnail, username, location name, Like,
  Save. **No scrolling required** to reach Like/Save.
- **Detail sheet:** appears when the user taps the card body or swipes
  it up. Bottom sheet with a persistent action bar (Like, Save, more).
  Uses a `.fraction(0.82)` initial detent equivalent — Android:
  `SheetState` with `PartiallyExpanded`.
- **Empty map tap:** dismisses preview; **does not restore** the
  camera to a previous location.
- **Tap another pin:** preview swaps instantly; camera pans to include
  both preview and new pin if needed.

### Clustering

- Use Google Maps Compose's built-in clustering
  (`com.google.maps.android.compose.Clustering`) with a **branded count
  badge**:
  - Deep-green circle, cream count text.
  - Badge diameter: 36 dp; count typography: labelMedium bold.
- **Coincident stack:** pins at identical (or ~1m apart) coords collapse
  into a small horizontal carousel inside the preview when tapped
  (existing `MapOverlapResolver` logic — verify parity).
- Tap cluster: zoom-in to expand.

### Filter pills

- Inactive filter pill: cream fill, primary text.
- Active filter pill: **primary (deep-green) fill, cream text**.
- Filter change preserves camera state (no jump).
- Pro filters (Followed, Liked): visible only for Pro users, else
  paywall on tap.

### Profile map

- Reuse the same `MapExperience` composable — no re-implementation.
- **No large header** on profile map — floating pill: `‹ [avatar]
  @username · N spots`.
- Same preview / detail behavior as Global map.
- Back returns to the Spots tab, not the parent screen.

### Analytics

- Firebase events (already in `MapAnalytics.kt` — verify present):
  - `map_pin_selected`
  - `map_cluster_tapped`
  - `map_preview_opened`
  - `map_detail_opened`
  - `map_preview_dismissed`
  - `map_filter_changed`

## iOS reference (files worth cross-checking)

- `../spot-ios-app/Spot/Views/Components/Map/MapExperience.swift`
- `../spot-ios-app/Spot/Views/Components/Map/MapSpotPreviewCard.swift`
- `../spot-ios-app/Spot/Views/Components/Map/SpotDetailSheet.swift`
- `../spot-ios-app/Spot/Views/Components/Map/MapClusterBadge.swift`

## Android target (files to touch)

**Audit first** — much of this may already exist. Confirm each item
below in `feature/map/`, then fix only what's missing:

- Compact preview (`MapSpotDrawer.kt` — verify it has a **compact
  mode** distinct from the expanded sheet).
- Clustering with branded badge (`MapPinLayout.kt` — verify branded
  badge, not the default Maps-Compose blue circle).
- Filter pill styling (`MapFilterPillsRow.kt` — verify inactive vs
  active color mapping).
- Profile map reuse (`feature/map/ProfileMapView.kt` — verify it uses
  the same `MapExperience` root, not a fork).
- Analytics coverage (`MapAnalytics.kt` — verify all events above fire).

If any of these are missing, ship them. Otherwise, file a smaller
follow-up.

## Acceptance criteria

- [ ] Tap pin → compact preview (~120–140 dp), Like/Save visible
      without scrolling.
- [ ] Tap card body or swipe → detail sheet with persistent action
      bar.
- [ ] Tap empty map → preview dismisses; camera stays where it is.
- [ ] Tap another pin → preview swaps instantly.
- [ ] Dense zoom → branded count badges; tap cluster zooms in.
- [ ] Coincident pins → horizontal carousel inside preview.
- [ ] Inactive filter pill light, active pill primary-fill; filter
      change does not jump the camera.
- [ ] Profile map: floating pill header, no large chrome; back returns
      to Spots tab.
- [ ] All Firebase map events fire correctly.
- [ ] `./gradlew testDebugUnitTest lintDebug assembleDebug` green.

## Test plan

- Manual: NYC / SF staging map to exercise clustering.
- Manual: post two spots at the exact same coordinates → coincident
  carousel.
- Manual: sign in as Pro and non-Pro → filter pills gate correctly.

## Out of scope

- Photo pins (task 12).
- Home place-first flip card (task 13).
- Any change to `get_map_spots_v1` payload.

## Follow-ups

- Extract `MapExperience` to `core/design/component/` if the profile map
  reuse pattern justifies it.
