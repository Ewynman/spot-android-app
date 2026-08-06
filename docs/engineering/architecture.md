# Architecture (Android)

## Purpose

Describe layering, DI, shared session state, and shell overlays for the Android port.

## Audience

Engineers and Cursor agents.

## Layering

```
Compose UI  →  ViewModel (StateFlow + Channel effects)  →  Repository interface  →  Supabase / Play / FCM
```

- No business logic in composables  
- No direct Supabase from ViewModels — always repository interfaces + fakes for tests  
- Hilt for DI (`di/*Module.kt`)  

## Shared session

`UserSessionHolder` keeps liked / bookmarked / blocked / Pro / username / avatar so feed, map, search, and profile stay consistent after optimistic updates.

`SessionBridge` wraps supabase-kt auth session (`currentUserId`).

## Shell

`SpotShell` (`navigation/SpotNavHost.kt`):

- 5-tab `NavHost` + `SpotBottomBar`  
- `PermissionRequestHost`  
- `SafetyFlowHost`  
- `OverlayHostLayer` (paywall, spot detail, Pro success)  
- `FirstRunOnboardingHost`  
- Profile overlay via `ProfileNavigationBus`  

Cross-tab navigation: `ShellNavigationBus`, `TabReselectBus`, `ProfileNavigationBus`.

## Data plane

Supabase only for users/spots/media/social. Firebase = analytics / Crashlytics / App Check only. See [PRD/01](../PRD/01-architecture-android.md) and project golden rules.

## Key packages added for parity

| Package | Role |
| --- | --- |
| `data/deeplink/` | Router, pending store, spot fetch, coordinator |
| `data/onboarding/` | First-run DataStore prefs |
| `data/notifications/` | Channels + local FOLLOW_ACCEPTED |
| `feature/onboarding/` | Coach overlay + ViewModel (also username/terms screens) |
| `core/design/component/RotatingVibeTags.kt` | SpotCard vibe rotation |
| `core/util/LocationFormat.kt` | city/state label helper |

## Related

- [deep-links.md](deep-links.md)
- [first-run-onboarding.md](first-run-onboarding.md)
- [design-system.md](design-system.md)
- [PRD/01-architecture-android.md](../PRD/01-architecture-android.md)
