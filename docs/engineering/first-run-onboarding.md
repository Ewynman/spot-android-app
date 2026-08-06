# First-run coach tour (Android)

## Purpose

Engineering reference for the first-run onboarding coach.

## Audience

Engineers and agents extending coach steps or permission handoffs.

## Components

| Type | File |
| --- | --- |
| Prefs | `data/onboarding/FirstRunOnboardingPreferences.kt` |
| State / steps | `feature/onboarding/FirstRunUiState.kt` |
| VM | `feature/onboarding/FirstRunOnboardingViewModel.kt` |
| UI | `feature/onboarding/SpotFirstRunOnboardingOverlay.kt` |
| Host | `feature/onboarding/FirstRunOnboardingHost.kt` (in `SpotShell`) |

## Step machine

Enum `FirstRunStep` matches iOS titles/bodies. Full-screen cards for `WELCOME` / `FINALE`; guided steps use scrim + bottom instruction card.

Primary CTA: welcome → `startTour()` (jump to spotCard); mid steps → `next()`; finale → `finish()`.

## Side effects

| Step | Side effect |
| --- | --- |
| `MAP_TAB` | `ShellNavigationBus.navigateToTab(Map)`; auto-advance on Map selection |
| `USER_LOCATION` | Map tab + `FirstRunEffect.RequestLocationPermission` if not determined |
| `MAP_MARKERS` / `MARKER_PREVIEW` | Ensure Map tab |
| Complete / skip | Persist; after 600 ms `RequestNotificationPermission` if needed |

Host routes effects into shared `PermissionsViewModel` (same instance as `PermissionRequestHost`).

## Start gate

```
authenticated
&& likedSpots.isEmpty()
&& bookmarkedSpots.isEmpty()
&& !hasCompletedOrSkipped
&& !uiTestMode
```

Then delay 500 ms and present.

## Limitations vs iOS

- Spotlight cutouts / measured frames are simplified (no full `CoachFrames` geometry yet)  
- Set `setUiTestMode(true)` from instrumentation to skip the tour  

## Related

- [product/onboarding.md](../product/onboarding.md)
- [diagrams/first-run-coach-flow.md](../diagrams/first-run-coach-flow.md)
- [PRD/05-auth-onboarding.md](../PRD/05-auth-onboarding.md)
