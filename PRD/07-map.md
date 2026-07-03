# 07 — Map

Tab index 1, test tag `navigation.mapTab`. Viewport-based discovery: branded pins, a user-location avatar marker, a bottom **spot drawer**, and (Pro) client-side filters.

## Data source

- **`get_map_spots_v1(p_min_lat, p_min_lng, p_max_lat, p_max_lng, p_center_lat, p_center_lng, p_limit=250)`** — returns spots within the visible bounding box, with `distance_meters` from center for sorting.
- Fetch is **debounced ~250ms** as the camera moves.
- Results are **merged with existing pins** and trimmed to a cap (nearest-first). There is no "load all" — always viewport-driven.

## Pins & rendering

- **Individual branded pins** (soft clusters are disabled in the current build; `allowSoftClusters = false`). Pin body `#1D2C24`, inner dot `#F5F3EF`, stroke `#0F1A14`.
- **User-location avatar marker**: the user's avatar with a colored ring — **gold `#C9A24A` for Pro**, green `#1D2C24` otherwise; halo while location updates. Suppress the default system blue dot.
- **Overlap handling**: pins within ~5m (bucket `0.00005°`) get a radial offset of **12m** so co-located spots are tappable.
- Selected pin gets a glow ring and a slight scale-up; entry animation staggers pins in.

## Tuning constants (`Constants.MapDesign`) — match for identical feel

| Constant | Value |
|----------|-------|
| initialRadiusMeters | 4,000 |
| initialNeighborhoodRadiusMeters | 3,200 (first center on real fix) |
| localSpan / citySpan | 0.04 / 0.30 (density thresholds) |
| visibleSpotsCap | 250 |
| farZoomPinCap | 60 |
| pinSize | 22 |
| pinSelectedScale / pinPressedScale | 1.28 / 0.92 |
| avatarMarkerSize / avatarRingWidth | 38 / 3 |
| pinEntryDuration | 0.28s |
| pinStaggerStep / pinStaggerCap | 0.012 / 0.25 |
| selectSpringResponse / selectSpringDamping | 0.32 / 0.82 |
| regionDebounce fast/slow | 180ms / 380ms |
| selectedPinCameraLift | 90 (keep selected pin above the drawer) |
| overlapBucketSize / overlapOffsetMeters | 0.00005 / 12 |
| panelMaxScreenFraction / panelMinHeight | 0.65 / 280 |
| mapDrawerTopCornerRadius | 22 |
| mapDrawerGapBelowFilterPills | 5 |
| VM viewport fetch debounce | ~250ms |

## Spot drawer (bottom sheet)

- Detents: **peek** and **expanded**. Contains a `SpotCard` for the selected spot.
- Height clamped so it never exceeds `panelMaxScreenFraction` (0.65) and stays below the filter pill row (gap 5).
- Selecting a pin lifts the camera by `selectedPinCameraLift` (90pt) so the pin stays visible above the drawer.

### Drawer dismiss policy (must match — covered by iOS policy tests)

Dismiss reasons (mirror `MapDrawerDismissReason`): `closeButton`, `mapMoved` (panned/zoomed away from the selected pin sufficiently), `emptyMapTap`, `filterChanged` (active filter hides the selected spot), `selectedSpotNoLongerVisible`, `spotSwitch` (tapping a different pin replaces selection), `tabLeft`, `tabReselected`.

Key rules:
- Tapping a **different** pin **replaces** the selection and drawer content.
- Panning/zooming **away** from the selected pin dismisses the drawer.
- Reselecting the Map tab closes the vibe picker + dismisses the drawer.

## Location behavior

- On first real GPS fix, center on the user at neighborhood zoom (3,200m). Subsequent GPS updates do **not** re-zoom once the user has manually moved the map (`userHasMovedMap`).
- **Recenter** control re-centers on the user; if permission is `notDetermined`, show the Location pre-prompt first.
- **Denied** location → fall back to a wide default region (continental-US style). Discovery still works by panning.

## Pro map filters (client-side)

- A **filter pill row** is shown **only to Pro users** (hidden entirely for free users — this is a hidden feature, not a paywall trigger).
- Filter dimensions (`SpotMapFilter`): **vibe**, **saved**, **liked**, **following**. Combined as AND across dimensions, OR within selected vibes.
- Filters apply **client-side** to already-fetched pins (they do not change the RPC query).
- **Vibe filter sheet** lists the 18 default vibe tags (+ any custom). Gate: available to Pro only.

## Onboarding coach anchors

The first-run tour spotlights map elements: user-location control, markers, and marker preview (drawer). Provide measurable anchor points for these (see [05-auth-onboarding.md](05-auth-onboarding.md)).

## States

- **Loading**: viewport fetch in progress (subtle; keep prior pins).
- **Empty**: no spots in viewport → keep map usable; optional subtle hint.
- **Error**: viewport fetch failed → log + keep prior pins; optional retry.

## Navigation

- From the drawer / card, tap creator → profile route.

## Android implementation notes

- Use **Google Maps Compose** (`GoogleMap` + `rememberCameraPositionState`). Observe camera idle to compute the visible bounds and center, debounce ~250ms, then call `get_map_spots_v1`.
- Render pins as custom marker composables (`MarkerComposable`) using the branded styling; keep a stable id → marker map and cap at 250.
- Implement the drawer as a `BottomSheetScaffold` or a custom sheet with peek/expanded states; wire the dismiss policy to camera movement and selection changes.
- Provide a `ProfileMapView` variant (same map, a single user's spots) reused on profiles — see [10-profile-social.md](10-profile-social.md).
