# Task 26 — OTP input: backspace navigation + SMS autofill

**Size:** Small (1–2 h) • **Priority:** P2 • **Status:** Open
**iOS reference PR:** [Ewynman/spot-ios-app #50](https://github.com/Ewynman/spot-ios-app/pull/50)

## Goal

Two OTP input fixes:

1. **Backspace navigation** in `OtpInputRow`: deleting the digit in a
   filled cell moves focus to the previous cell.
2. **Autofill support**: SMS/email code from the system autofill
   populates all six cells at once.

## Contract

### Backspace navigation

- Behavior: when a cell contains a digit and the user presses
  backspace, clear the digit and move focus one cell **left**.
- When a cell is already empty, backspace moves focus left and clears
  the previous cell's digit.
- No wraparound (backspace on cell 0 with an empty value does nothing).

### Autofill

- iOS uses `.textContentType(.oneTimeCode)`. Android equivalents:
  - Compose: `KeyboardOptions(autoCorrect = false, keyboardType = KeyboardType.NumberPassword)`
    + `TextField(modifier = Modifier.semantics { contentType = ContentType.NewPassword })` (Android 14+).
  - For SMS OTP delivery: use Google's **SMS Retriever API**
    (`com.google.android.gms:play-services-auth-api-phone`) which
    surfaces the code from a specifically-formatted SMS without
    requiring `RECEIVE_SMS` permission.
- If the OTP delivery channel is **email** (which is the primary path
  for Spot), autofill from SMS Retriever won't apply — but the
  keyboard IME may still surface pasted / clipboard OTPs when the
  content type hint is set.
- Preferred: also support long-press → paste, and when a 6-digit
  string is pasted anywhere in the row, distribute it across all six
  cells.

### Component

- `feature/auth/component/OtpInputRow.kt` (existing) — extend.
- Preserve all existing test tags: `confirmEmail.otpCell.0` …
  `confirmEmail.otpCell.5` (mirror iOS).

## iOS reference

- PR 50 (see `../spot-ios-app/`):
  - `Views/Auth/ConfirmEmailView.swift` — `.textContentType(.oneTimeCode)`
    + backspace `.onChange`.

## Android target (files to touch)

Edit:
- `feature/auth/component/OtpInputRow.kt` — key handling for
  backspace navigation; paste-distribute; content-type hint.
- `feature/auth/ConfirmEmailScreen.kt` — verify wiring.
- `app/src/test/.../feature/auth/OtpInputRowTest.kt` — unit tests
  around the paste-distribute and backspace helpers (extract pure
  functions to make testable).

Optional (if the app also uses SMS OTP anywhere in the future):
- `data/notifications/SmsOtpRetriever.kt` — SMS Retriever API
  bootstrap.

## Acceptance criteria

- [ ] Backspace on a filled cell clears and moves left.
- [ ] Backspace on an empty cell moves left and clears the previous
      cell.
- [ ] Pasting a 6-digit code into any cell fills all six.
- [ ] Long-press → paste works from the system clipboard.
- [ ] Numeric keyboard shows by default.
- [ ] Existing test tags preserved.
- [ ] Unit tests cover the paste-distribute and backspace logic.
- [ ] `./gradlew testDebugUnitTest lintDebug assembleDebug` green.

## Test plan

- Manual: enter 3 digits, backspace across all → focus + values
  correct.
- Manual: copy a 6-digit code from a message app → paste anywhere in
  the row → row fills.
- Manual: press submit with < 6 digits → button disabled (existing
  behavior).

## Out of scope

- SMS OTP delivery (Spot uses email OTP today).
- Any RPC change.

## Follow-ups

- If SMS OTP is introduced, wire SMS Retriever properly.
