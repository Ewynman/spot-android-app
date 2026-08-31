# Task 24 — Profile Spots empty states

**Size:** Small (1–2 h) • **Priority:** P2 • **Status:** Open
**iOS reference PR:** [Ewynman/spot-ios-app #52](https://github.com/Ewynman/spot-ios-app/pull/52)

## Goal

Ship clear, distinct empty states for the Profile Spots area,
depending on who's viewing whose profile.

## Contract

### Three states

| Viewer | Profile owner | State |
|--------|---------------|-------|
| Self  | Self  | **Own empty:** call-to-action + button `Post your first Spot` → routes to Post tab. |
| Self  | Other (public, no spots) | **Other empty:** neutral message, no CTA. |
| Self  | Other (private, not following) | **Private:** locked-lock icon + `This account is private. Follow to see their Spots.` |

### Copy

| State | Title | Body | CTA |
|-------|-------|------|-----|
| Own | `No spots yet` | `Share a place you love — it's how Spot works.` | `Post your first Spot` |
| Other empty | `No spots yet` | `This user hasn't posted anything.` | — |
| Private | `Private account` | `Follow to see their Spots.` | (Follow button appears in profile header, not here) |

### Behavior

- Own empty tap → switches to Post tab (via
  `ShellNavigationBus.navigateToTab(POST)`).
- Private state is only shown when `profile.isPrivate` AND the viewer
  is not following AND is not the owner.

## iOS reference

- PR 52 (see `../spot-ios-app/`):
  - `Views/Profile/ProfileView.swift` — `ProfileSpotsEmptyState`
    resolver.
  - `Services/Profile/ProfileSpotsEmptyStateResolver.swift` (if
    present).

## Android target (files to touch)

Create:
- `feature/profile/ProfileSpotsEmptyState.kt` — three composables +
  resolver.
- `app/src/test/.../feature/profile/ProfileSpotsEmptyStateResolverTest.kt` —
  pure resolver logic tests.

Edit:
- `feature/profile/ProfileScreen.kt` / `ProfileComponents.kt` — swap
  the current empty state for the resolver output.
- `feature/profile/ProfileViewModel.kt` — expose the derived state.

Test tags (mirror iOS):
- `profile.spotsEmptyState`
- `profile.postFirstSpotButton`

## Acceptance criteria

- [ ] Own empty: shows CTA; tap routes to Post tab.
- [ ] Other (public, empty): shows neutral message; no CTA.
- [ ] Other (private, not following): shows private state.
- [ ] Other (private, following): shows normal spots list.
- [ ] Test tags match iOS strings.
- [ ] Unit test covers the resolver for all four combinations.
- [ ] `./gradlew testDebugUnitTest lintDebug assembleDebug` green.

## Test plan

- Manual: sign in as User A with no spots → own empty CTA works.
- Manual: view User B (public, no spots) → other empty message.
- Manual: view User C (private, not following) → private message.
- Manual: follow User C → see spots.

## Out of scope

- Any change to the follow-request flow.
- Empty states on other Profile tabs (Map, Likes, Bookmarks).

## Follow-ups

- Extend the resolver to Likes/Bookmarks tabs if empty states drift.
