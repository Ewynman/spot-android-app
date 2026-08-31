# Task 23 — Delete confirmation dimming fix

**Size:** Tiny (< 1 h) • **Priority:** P3 • **Status:** Open
**iOS reference PR:** [Ewynman/spot-ios-app #87](https://github.com/Ewynman/spot-ios-app/pull/87) (client half)

## Goal

Remove the gray dim scrim behind the delete confirmation overlay so the
feed / profile stays visible behind the confirm dialog, matching iOS.

## Contract

- The branded delete confirmation overlay renders on top of the feed
  / profile but **does not** dim the background.
- Tap-outside still dismisses (verify).
- Cancel button still dismisses.
- Confirm still deletes.

**Note:** iOS PR #87 also shipped a **backend migration** to make the
feed-event trigger no-op on already-deleted spots
(`_record_feed_event_for_user_v1` / `record_feed_event_v1`). That's a
server task; not part of this Android task. If Android sees the same
FK error, escalate to backend.

## iOS reference

- PR 87 (see `../spot-ios-app/`):
  - `Views/Components/SpotConfirmationOverlay.swift` — no scrim.

## Android target (files to touch)

Edit:
- `core/design/component/SpotConfirmationOverlay.kt` (create if
  extraction hasn't happened yet — see task 18) — no scrim behind
  content. Use `Popup` with `PopupProperties(dismissOnClickOutside = true)`
  and no scrim, **not** `Dialog` which paints a default scrim.

Verify:
- `feature/profile/ProfileComponents.kt` delete dialog uses the
  branded overlay.

## Acceptance criteria

- [ ] Delete confirmation shows without a gray scrim.
- [ ] Tap outside or Cancel dismisses.
- [ ] Confirm still deletes.
- [ ] `./gradlew testDebugUnitTest lintDebug assembleDebug` green.

## Test plan

- Manual: from own-profile grid or spot card ⋮ → Delete → confirm
  visible; background not dimmed.

## Out of scope

- Backend feed-event FK fix (server task).

## Follow-ups

- If tap-to-dismiss on `Popup` clashes with any other overlay, gate
  the outside-tap behavior on `isFocused` state.
