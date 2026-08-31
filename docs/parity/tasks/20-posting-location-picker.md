# Task 20 — Posting location picker redesign

**Size:** Small–Medium (2–4 h) • **Priority:** P2 • **Status:** Open
**iOS reference PR:** [Ewynman/spot-ios-app #70](https://github.com/Ewynman/spot-ios-app/pull/70)

## Goal

Rebuild the composer location step with clearer hierarchy, premium
place cards, a selected-Spot summary, and polished loading / empty
states. Replace the narrow category-only query with **broad POI
discovery** using the shared live location. Search within **3 km by
default**, sort by distance, and allow an explicit **8 km wider-area**
search. Typed queries debounce, and a **50 km local search** runs
before global fallback.

## Contract

### Layout

- **Selected-Spot summary card** at the top when a place is picked:
  name, address (single-line), coords, **Change** button.
- **Query field** below (large, cream, primary border — task 16 style).
- **Loading state:** premium skeleton cards (not a spinner).
- **Empty state:** copy `No places match.` + `Search a wider area`
  button which switches to the 8 km search.
- **Results:** list of place cards showing:
  - Icon (POI category glyph — food, shop, park, etc.).
  - Name.
  - Distance line: `123 m` or `1.2 km` (relative to current location).
  - Address (single-line).

### Discovery

- Uses the **shared live location** from `MapLocationTracker`
  (do not re-request location on the location step).
- Broad POI: use Places API `Nearby Search` with **all POI types**
  (not just the current narrow category filter).
- **Default radius:** 3 km.
- **Sort:** by ascending distance (Google Places returns rank-by
  prominence by default — override to `rankBy=distance`).
- **Explicit wider area:** user-facing button expands to 8 km.
- **Local search (typed queries):** 50 km radius; if empty, fall back
  to global text search.

### Debouncing

- Debounce text input at **300 ms** before firing a query.
- Cancel in-flight queries on new input (Coroutines `flow` +
  `debounce` + `distinctUntilChanged` + `flatMapLatest`).

### Custom name

- If none of the results fit, the user can enter a custom place name.
  Reuse the existing pattern from iOS `LocationSelectionView`.

## iOS reference

- PR 70 (see `../spot-ios-app/`):
  - `Views/PostFlow/LocationSelectionView.swift`
  - `Services/Spots/LocationSearchPolicy.swift`
- Constants: `LocationSearchPolicy.localSearchRadiusMeters = 50_000`;
  Places-API-equivalent radii per PR.

## Android target (files to touch)

Edit / create:
- `data/location/AndroidPlaceSearchProvider.kt` — broad POI,
  rank-by-distance, 3 km / 8 km, 50 km local.
- `data/location/LocationSearchPolicy.kt` — new file: pure logic
  (radius selection, distance labels, sort).
- `feature/post/PostComposerSteps.kt` — location step redesign.
- `feature/post/LocationPickerCards.kt` — new premium card composables.
- `app/src/test/.../data/location/LocationSearchPolicyTest.kt` —
  sort, radius selection, distance label formatting.

## Acceptance criteria

- [ ] Selected-Spot summary shows at the top when a place is picked;
      `Change` clears + re-focuses the query.
- [ ] Default radius: 3 km; wider-area button expands to 8 km.
- [ ] Results ranked by ascending distance from user location.
- [ ] Distance labels: `123 m` / `1.2 km` formatting.
- [ ] Loading shows skeleton cards; empty shows the wider-area button.
- [ ] Typed searches debounce at 300 ms and cancel prior in-flight
      queries.
- [ ] Local text search runs at 50 km before global fallback.
- [ ] Unit tests cover the policy (distance labels, sort, radius
      selection).
- [ ] `./gradlew testDebugUnitTest lintDebug assembleDebug` green.

## Test plan

- Manual (with location on): compose a spot → nearby POIs load ranked
  by distance.
- Manual: hit empty state → tap wider-area → results appear.
- Manual: type a specific query fast → only the final request results
  render (no flicker of stale results).

## Out of scope

- Any change to Google Places API key restrictions (ops).
- Rewriting the Places SDK integration below `AndroidPlaceSearchProvider`.

## Follow-ups

- Consider caching recent successful queries per session.
