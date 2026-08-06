# Onboarding (Android)

## Purpose

Describe Welcome/auth gates and the first-run coach tour on Android.

## Audience

Product, UX, engineering, QA.

## Auth / Welcome

- **Welcome** (`WelcomeScreen`): cream canvas, tracked **SPOT** wordmark, serif headline *“Places hit different when they come from your people.”*, supporting line, editorial collage (chips / pin / avatars), terms checkbox gating all CTAs.
- **CTA order:** Get started → Log in → Continue with Google (Android OAuth swap for Apple).
- Terms / Privacy URLs: `https://spotapp.online/terms`, `https://spotapp.online/privacy`.

Post-auth: username setup (OAuth without username), terms update gate. See [PRD/05](../PRD/05-auth-onboarding.md).

## First-run coach

Active manager: `FirstRunOnboardingViewModel` + `SpotFirstRunOnboardingOverlay`, hosted from `SpotShell` via `FirstRunOnboardingHost`.

**Start conditions:** authenticated, empty likes + bookmarks, not completed/skipped; ~500 ms delay; skip in UI-test mode when set.

**Steps (order):**  
`welcome → spotCard → spotDetails → vibeTag → like → bookmark → creator → mapTab → userLocation → mapMarkers → markerPreview → finale`

**Behaviors:**

- `mapTab` navigates to Map via `ShellNavigationBus` and advances when Map is selected  
- `userLocation` may request Location pre-prompt  
- Finale or skip → after ~600 ms, Notification pre-prompt if still not determined  

**Persistence (DataStore `spot_first_run_onboarding`):**  
`spotFirstRunOnboarding.completed.v1`, `completedAt.v1`, `skipped.v1`, `lastStep.v1`, plus legacy `homeTourAccepted`.

Engineering detail: [first-run-onboarding.md](../engineering/first-run-onboarding.md).

## Related

- [ui-parity.md](ui-parity.md)
- [engineering/notifications.md](../engineering/notifications.md)
- iOS: `../spot-ios-app/docs/product/onboarding.md`
