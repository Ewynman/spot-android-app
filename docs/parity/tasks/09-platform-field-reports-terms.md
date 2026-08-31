# Task 09 — `platform="android"` field on reports + terms

**Size:** Tiny (< 1 h client; needs backend confirmation) • **Priority:** P2
**Status:** Open

## Goal

Send `platform = "android"` when calling `submit_content_report` and
`record_terms_acceptance_v1`, so ops can distinguish Android reports and
terms acceptances from iOS.

## Why it matters

- iOS omits the `platform` field (server infers `ios`).
- Android must be explicit — the `.cursor/rules/project.mdc` rules say
  *"Set `platform = "android"` on `reports` and `user_terms_acceptances`."*
- Without this, the ops dashboard cannot split reports by platform, and
  compliance reporting for terms acceptance is incomplete.

## Contract

### Backend confirmation FIRST

Before shipping the client change, confirm the server accepts the
`platform` argument:

- `submit_content_report(p_target_id, p_target_type, p_reason,
  p_details, p_block_requested, platform)` — verify the last param name.
- `record_terms_acceptance_v1(p_version, p_device_info, p_app_version,
  platform)` — verify the last param name.

If the RPC signatures don't accept `platform`, **stop and open a draft
PR** describing that the server needs to be updated first (via a
Supabase migration). Do not attempt to force it through by adding an
unknown parameter.

### Client change (once server is ready)

- `data/safety/SafetyRpcParams.kt` — add `platform: String = "android"`
  to the report payload.
- `data/safety/SupabaseSafetyRepository.kt` — pass it to the RPC.
- `data/terms/TermsRpcParams.kt` — same.
- `data/terms/SupabaseTermsRepository.kt` — same.

Existing tests should still pass; add a case verifying the field is
serialized.

## iOS reference (for reviewers)

- `../spot-ios-app/Spot/Services/Moderation/ModerationService.swift` —
  does **not** pass `platform`; Android intentionally diverges here.
- `../spot-ios-app/Spot/Services/Moderation/TermsAcceptanceService.swift`.

## Android target (files to touch)

Edit:
- `data/safety/SafetyRpcParams.kt`
- `data/safety/SupabaseSafetyRepository.kt`
- `data/terms/TermsRpcParams.kt`
- `data/terms/SupabaseTermsRepository.kt`

Tests:
- `app/src/test/.../data/safety/` — verify serialized payload contains
  `"platform": "android"`.

## Acceptance criteria

- [ ] Server accepts the new field (verified by running against staging).
- [ ] Every call to `submit_content_report` includes `platform:
      "android"`.
- [ ] Every call to `record_terms_acceptance_v1` includes `platform:
      "android"`.
- [ ] Unit test asserts the serialized payload.
- [ ] `./gradlew testDebugUnitTest lintDebug` green.

## Test plan

- `./gradlew testDebugUnitTest`.
- Manual: on staging, file a report and confirm the row in the `reports`
  table has `platform = 'android'`.
- Manual: sign up a new user; confirm the row in
  `user_terms_acceptances` has `platform = 'android'`.

## Out of scope

- Changing existing reports/terms rows in the DB (that's a data
  migration).
- Adding `platform` to other RPCs.
- Any iOS change.

## Follow-ups

- If ops dashboards need retrofit values, coordinate a data migration
  separately.
