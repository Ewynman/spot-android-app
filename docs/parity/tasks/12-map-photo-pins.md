# Task 12 — Map photo-preview pin markers

**Size:** Medium (3–8 h) • **Priority:** P1 • **Status:** Open
**iOS reference PR:** [Ewynman/spot-ios-app #92](https://github.com/Ewynman/spot-ios-app/pull/92)

## Goal

Replace the branded green teardrop map pins with **circular photo-preview
markers** that show the spot's primary image, matching iOS "Concept 3."
Fall back to the teardrop when no image is available.

## Why it matters

iOS shipped this as the flagship map redesign in PR #92. On Android
today, `feature/map/MapPinMarker.kt` renders green teardrops only.

## Contract

### Visual

- **Photo pin:** circular thumbnail (white border, subtle shadow) with a
  downward tail whose tip stays locked to the geographic coordinate
  across default / pressed / selected states.
- **Frame:** 44 × 56 dp (respect 48 dp accessibility minimum touch target
  on Android — bump frame slightly if needed).
- **Selected state:** scale the marker slightly (~1.10) and raise
  z-index so it renders above adjacent pins.
- **Fallback (no image or feature flag off):** existing green teardrop
  from `MapPinMarker.kt`.

### Behavior

- Only render photo pin when the spot has a `primary_public_url` or
  `primary_storage_path`. Rows without an image use the teardrop.
- Selection scaling is animated (~150 ms spring).
- Clustering behavior unchanged — photo pins participate in the same
  cluster identifier as the teardrops.

### Feature flag

- Add `MapMarkerFeatureFlags.photoPinMarkersEnabled` in `data/map/` (or
  reuse `FeedFlags` pattern). Default **on** in Debug and Release.
- QA can flip off at runtime via the debug settings screen.

### Image loading

- Dedicated bounded LRU cache separate from Coil's default (photo pins
  reload aggressively during pan/zoom).
  - Max entries: 128 (matches iOS constant).
  - Downsample to 88 × 88 px (2× 44 dp @ mdpi baseline; up to 176 for
    high-density displays via `Resources.displayMetrics.density`).
- In-flight request de-duplication: if two markers request the same URL
  concurrently, share the same job.
- On memory pressure (`onTrimMemory`), clear the cache.

### Analytics

- New event `map_marker_impression` — dedup'd per attach cycle
  (fire once per marker per map session).
- New event `map_marker_image_load` — includes success/failure and
  cache hit.
- Extend `map_marker_tapped` with `marker_type` (`photo_pin` or
  `teardrop`) and `zoom_level` (rounded to 0.5).

### Accessibility

- Photo pin `contentDescription` = `"Spot by {username}, {locationName}. Double-tap to preview."`
  Never surface literal `nil`; fall back to `""` for missing username
  or place.
- Marker remains tappable while image loads (placeholder or teardrop
  intermediary).

## Android target (files to touch)

Create:
- `data/map/MapMarkerFeatureFlags.kt` — flag holder.
- `feature/map/PhotoPinMarker.kt` — Compose or `Canvas` marker for
  Google Maps Compose.
- `core/media/MapMarkerImageCache.kt` — bounded LRU + in-flight dedupe.
- `app/src/test/.../feature/map/PhotoPinGeometryTest.kt` — tip-at-anchor,
  border overflow clamp, center-crop.
- `app/src/test/.../core/media/MapMarkerImageCacheTest.kt` — downsample,
  hit path, in-flight dedupe, cancel.

Edit:
- `feature/map/MapPinMarker.kt` — dispatch to photo pin or teardrop
  based on data + flag.
- `feature/map/MapScreen.kt` — wire impression event.
- `data/feed/HomeFeedRowDto` — verify `primary_public_url` reaches map
  hydrator (should already be there).
- `core/analytics/AnalyticsParams.kt` — add new event keys.

## Acceptance criteria

- [ ] Spots with a primary image render photo pins; without, teardrops.
- [ ] Tip stays anchored to the coordinate at every zoom level.
- [ ] Selection scales the pin and raises z-index above neighbors.
- [ ] Dense city zoom clusters, expands to photo pins on zoom-in.
- [ ] Rapid pan/zoom: no duplicate network requests for the same URL.
- [ ] Image loading is downsampled (verify via memory profiler on a
      100-pin map — no 4k images in memory).
- [ ] Falling back after image failure keeps the marker tappable.
- [ ] `MapMarkerFeatureFlags.photoPinMarkersEnabled = false` reverts
      every pin to the teardrop.
- [ ] `map_marker_impression`, `map_marker_image_load`, and updated
      `map_marker_tapped` events fire correctly.
- [ ] Unit tests cover geometry, cache, downsampling, dedupe.
- [ ] `./gradlew testDebugUnitTest lintDebug assembleDebug` green.

## Test plan

- Manual: NYC / Tokyo staging map; verify photo pins load, cluster,
  expand, and don't fan out network calls under rapid gestures.
- Manual: airplane mode → pins render as teardrops on cached rows.
- Instrumented: verify photo pin content description via UiAutomator.

## Out of scope

- Custom photo pin animations (only selection scale).
- Photo pin content on the Profile map (that's task 14).
- Rewriting the cluster count badge — reuse existing.

## Follow-ups

- Consider a very low-detail preview (blurhash) if network is slow.
