# Task 08 — Settings → Collections navigation

**Size:** Tiny (< 1 h) • **Priority:** P2 • **Status:** Open

## Goal

Wire the "Collections" row in Settings so it actually navigates to the
Collections list screen. Today it's a TODO.

## Why it matters

`feature/settings/SettingsNavigationHost.kt:71` contains
`onNavigateToCollections = { /* TODO: Navigate to collections */ }`. iOS
routes here from Settings → Manage collections. The screen exists
(`feature/collections/CollectionsListScreen.kt`) but the entry point is
dead.

## Contract

### Behavior

- Row visibility: **Pro users only** (matches iOS — non-Pro users don't
  see the entry).
- Tap: navigates to `CollectionsListScreen`. Back returns to Settings.
- If the current user turns non-Pro while on the screen (rare), pop back
  to Settings and show a `Pro required` toast.

### Copy

- Row label: `Manage collections` (match iOS `SettingsView.swift`
  wording).

## iOS reference (for reviewers)

- `../spot-ios-app/Spot/Views/Profile/SettingsView.swift` — Pro row that
  pushes `CollectionsView`.
- `../spot-ios-app/Spot/Views/Profile/CollectionsView.swift`

## Android target (files to touch)

Edit:
- `feature/settings/SettingsNavigationHost.kt` — replace the TODO with
  a real navigation call. Depending on how the host is wired, either
  add a `Route.Collections` and register the destination in the nav
  graph, or push through the existing `ShellNavigationBus` /
  `ProfileNavigationBus`.
- `feature/settings/SettingsScreen.kt` — hide/show the row based on
  `session.isPro`.

Verify:
- No new module needed — `feature/collections/` already exposes the
  screen and its ViewModel via Hilt.
- The Collections list `back` handler pops to Settings, not to Profile.

## Acceptance criteria

- [ ] Pro user sees the "Manage collections" row in Settings.
- [ ] Non-Pro user does not see the row.
- [ ] Tap opens `CollectionsListScreen`.
- [ ] Back returns to Settings.
- [ ] No TODO comment remains at that location.
- [ ] Test tag `settings.manageCollectionsRow` (mirror iOS if a
      different string exists — check `screen-map.md` and update if you
      find the iOS identifier).
- [ ] `./gradlew testDebugUnitTest lintDebug` green.

## Test plan

- Manual: sign in as Pro, open Settings → Manage collections. Sign in
  as free, verify the row is hidden.

## Out of scope

- Any changes to `CollectionsListScreen` itself.
- Adding a Collections shortcut anywhere else.

## Follow-ups

- If Pro-only rows in Settings share a pattern, extract a
  `ProGatedRow` composable — but only if 2+ rows benefit.
