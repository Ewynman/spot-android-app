# 13 — Moderation & Safety

User safety is **priority one** and required for store approval (App Store Guideline 1.2 / Google Play UGC policy). The Android build must ship all of these end-to-end.

## The five required UGC safety guarantees

| Requirement | Implementation |
|-------------|----------------|
| **Terms + Privacy agreement before registration** | Unchecked agreement checkbox on Welcome gates sign-in/sign-up; also on the post-auth username screen for OAuth. Recorded via `record_terms_acceptance_v1`. See [05-auth-onboarding.md](05-auth-onboarding.md). |
| **Filter objectionable content** | Image moderation (Azure Content Safety) on every uploaded image; server-side severe-text blocklist filter on captions/text. |
| **Report objectionable content + abusive users** | "Report Spot" (spot overflow) and "Report User" (profile overflow), both via `submit_content_report`. |
| **Block abusive users** | "Block User" (profile/spot overflow + toggle in report sheets) via `block_user_v1`; instant local feed removal + server `can_view_author` filtering. |
| **Act on reports within 24h** | Service-role `moderation_queue` view prioritizes open reports; SLA tracked by moderators. |

## Image moderation pipeline

**Every** image for a Spot or profile picture must pass moderation before it becomes an approved, publicly referenced asset. No client-only bypass.

Flow (also in [08-post-flow.md](08-post-flow.md)):
1. Client inserts a `media_assets` row (`status='pending'`, `kind='spot_image'|'profile_image'`, `pending_bucket='pending_images'`, `pending_path='{userId}/{assetId}.jpg'`).
2. Client uploads the JPEG to `pending_images/{userId}/{assetId}.jpg`.
3. Client calls the **`moderate-image`** edge function with `{ "mediaAssetId": "<uuid>" }` (JWT attached; `verify_jwt=true`).
4. The function calls **Azure Content Safety** (server-held credentials), writes `media_assets.status` / `scores` / `azure_result` / `moderated_at`, appends a `media_moderation_events` row, and on approval sets `approved_bucket`/`approved_path`.
5. **Approved** → publish can proceed (`publish_spot_with_approved_media_assets_v1`) or profile photo is set. **Rejected** (`image_policy_rejected`) → show a **non-graphic** message; do not publish. **Unavailable** (`moderation_unavailable`) → retry later.

`media_assets.status`: `pending → approved | rejected | failed`; also `deleted`, `legacy_unmoderated`.

**Category thresholds** live in the edge function (server-side); the client only reacts to the `approved` boolean + reason. Never log raw image bytes or PII.

## Text content filtering

- A server-side **severe blocklist** filter runs on user text (captions, etc.); rejects/flags via `content_moderation_results` and can write `moderation_events`. `moderation_status` on `users`/`spots` reflects `approved|flagged|rejected|pending_review`.
- The client should surface a safe rejection message if a publish is blocked for text reasons.

## Reporting

Report sheet fields → RPC `submit_content_report(p_target_type, p_target_id, p_reported_user_id, p_reason, p_details, p_block_requested)`:
- `target_type`: `spot`, `profile`, `spot_image`, `comment`, `collection`, `other`.
- `reason`: one of the report reasons below.
- `details`: optional free text.
- `block_requested`: optional "also block this user".

The RPC assigns a **priority** based on reason, writes the `reports` row, and appends a `report_created` moderation event.

### Report reasons (string values)

Canonical set used by the client (`ModerationReportReason`):
```
inappropriate, harassment, violence, spam, misinformation, privacy, other,
harassment_or_abuse, hate_speech_or_discrimination, sexual_or_nude_content,
violence_or_threats, spam_or_scam, illegal_content, private_information
```
Present a friendly labeled list; store the raw value in `reason`.

## Blocking

- Via `block_user_v1(p_blocked_user_id, p_source_target_type?, p_source_target_id?, p_reason?)` → writes `user_blocks`.
- Immediately remove the blocked author's spots from the feed locally (a "locally remove by author" signal).
- Server `can_view_author` / `users_public` filter the blocked user from discovery thereafter.
- **Unblock** from Settings → Blocked Users (delete `user_blocks`).

## Report-volume auto-suspension

- Trigger on `reports` insert: rolling **30 days**, if the same `owner_id` accrues **≥5 reports AND ≥3 distinct reporters**, set `users.suspended_for_reports_at = now()` (idempotent).
- Effect: `can_view_author` / `users_public` hide them; `get_home_feed_v1` / `get_home_feed_status_v1` exclude their spots.
- **Unsuspend (support only):** `update public.users set suspended_for_reports_at = null where id = '<uuid>';` (does not touch Auth; only unhides public surfaces).

## Account status & moderation flags

- `users.account_status`: `active | restricted | suspended | banned`.
- `users.moderation_status` / `spots.moderation_status`: `approved | flagged | rejected | pending_review`.
- `spots.hidden_at` / `hidden_reason` for hidden content.

## Per-viewer hiding

- `user_hidden_spots` (reasons: `hide`, `report`, `not_interested`) suppresses spots for that viewer in feed and map.

## Client responsibilities checklist (Android)

- [ ] Terms agreement gate before registration (both email + OAuth paths).
- [ ] Moderate every uploaded image before publish/profile set.
- [ ] Safe, non-graphic rejection messaging.
- [ ] Report spot + report user flows with the reason list above.
- [ ] Block/unblock with instant local feed removal.
- [ ] Never log PII/raw image bytes; use structured logging with a moderation category.
- [ ] Set `platform='android'` on `reports` and `user_terms_acceptances`.
