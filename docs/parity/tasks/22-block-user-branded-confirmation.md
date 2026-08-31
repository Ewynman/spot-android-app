# Task 22 — Branded confirmation before blocking a user

**Size:** Tiny (< 1 h) • **Priority:** P2 • **Status:** Open
**iOS reference PR:** [Ewynman/spot-ios-app #91](https://github.com/Ewynman/spot-ios-app/pull/91)

## Goal

Block from a profile ellipsis or a Spot card ellipsis must **not fire
on the first tap**. Show the branded confirmation overlay (matching
delete's overlay) that names the account before writing.

## Contract

### Copy

- Title (with username): `Block @{username}?`
- Title (missing username fallback): `Block this user?`
- Body: `Their spots, likes, and profile will be hidden from you. They won't be notified.`
- Confirm (destructive style): `Block`
- Cancel: `Cancel`

### Behavior

- Spot card ⋮ → Block → **overlay first**, no immediate write.
- Profile ⋮ → Block → same overlay, replaces any system alert.
- Confirm calls `block_user_v1` (already wired in
  `SupabaseSafetyRepository`).
- Cancel dismisses the overlay; no state change.

### Component

- Reuse the branded confirmation overlay from task 18 (delete
  confirmation) — `core/design/component/SpotConfirmationOverlay.kt`.
  If not yet extracted, extract it in this PR.

## iOS reference

- PR 91 (see `../spot-ios-app/`):
  - `Views/Components/SpotCard.swift` — block entry from card.
  - `Views/Profile/ProfileView.swift` — block entry from profile.
  - `Views/Components/SpotConfirmationOverlay.swift` — the overlay.

## Android target (files to touch)

Edit:
- `feature/safety/SafetyFlowHost.kt` and `feature/safety/BlockUserDialog.kt`
  — replace `AlertDialog` with the branded overlay; produce the
  correct copy (named username or fallback).
- `feature/safety/SafetyViewModel.kt` — verify no write happens on
  first tap; write only after `onConfirm`.

Test:
- `SafetyViewModelTest` — block confirmation state, and cancel does
  not call the repository.
- Unit test the copy helper (like iOS `BlockUserConfirmationCopy`):
  named title, fallback title, body invariance.

## Acceptance criteria

- [ ] First tap on Block (card or profile) opens the overlay; does
      not call the repository.
- [ ] Overlay title uses `Block @{username}?` when a username is
      known; else `Block this user?`.
- [ ] Confirm calls `block_user_v1` once.
- [ ] Cancel dismisses; no server call.
- [ ] Unit tests cover the confirmation state machine and copy.
- [ ] `./gradlew testDebugUnitTest lintDebug assembleDebug` green.

## Test plan

- Manual: from a spot card ⋮ → Block → overlay with `@username`; cancel → nothing.
- Manual: from a profile ⋮ → Block → same overlay; confirm → user blocked, feed filters immediately.

## Out of scope

- Any change to the underlying `block_user_v1` payload.
- Any change to the report flow.

## Follow-ups

- Consider a matching confirmation for **unblock** (currently one-tap
  in some paths).
