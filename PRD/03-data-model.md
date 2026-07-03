# 03 — Data Model (Supabase / Postgres)

This is the **authoritative schema**, read from the live Supabase project `aeurigbbohyxvtsfiyul` (Postgres 17) on 2026-07-03. Android uses this **exact** schema — do not fork it.

Conventions:
- All tables are in schema `public`, all have **RLS enabled** (`rls_enabled = true`).
- PKs are `uuid` with `gen_random_uuid()` default unless noted.
- Timestamps are `timestamptz` (`now()` default) unless noted.
- `public.users.id` == `auth.users.id` (Supabase Auth user).
- The client generally does **not** decode Postgres rows directly for the composite UI models (`Spot`, `User`); it reads via **RPCs** (snake_case rows) and assembles domain models. Direct table reads use snake_case column names.

## Entity relationship overview

```
auth.users ─1:1─ users
users ─1:N─ spots ─1:N─ spot_images
                     └─N:M─ vibe_tags (via spot_vibe_tags)
users ─1:N─ spot_likes / spot_bookmarks ─N:1─ spots
users ─1:N─ bookmark_collections ─1:N─ bookmark_collection_spots ─N:1─ spots
users ─follows (follower_id, followee_id)─ users
users ─follow_requests (requester_id, target_user_id)─ users
users ─user_blocks (blocker_id, blocked_user_id)─ users
users ─1:N─ media_assets ─1:N─ media_moderation_events
users ─1:N─ reports (reporter_id, owner_id) ─1:N─ moderation_events
users ─feed_impressions / user_hidden_spots / user_feed_events ─ spots
users ─user_vibe_affinities / user_creator_affinities / user_feed_profiles (ranking)
terms_versions ─1:N─ user_terms_acceptances
support_requests (standalone), content_moderation_results (standalone/admin)
```

---

## Core content & identity

### `users` — app profile (id matches `auth.users.id`)
507 rows. PK `id`. FK `id → auth.users.id`, `profile_image_asset_id → media_assets.id`.

| Column | Type | Null | Default | Notes |
|--------|------|------|---------|-------|
| `id` | uuid | no | — | = auth user id |
| `email` | text | yes | — | login/heartbeat field |
| `email_verified` | bool | no | `false` | |
| `username` | text | no | — | user-managed |
| `username_lower` | text | no | — | lowercased, uniqueness/search |
| `profile_image_url` | text | yes | — | avatar public URL |
| `is_private` | bool | no | `false` | private account |
| `is_pro` | bool | no | `false` | Pro entitlement |
| `pro_until` | timestamptz | yes | — | Pro expiry |
| `last_active_at` | timestamptz | yes | — | heartbeat |
| `locale` | text | yes | — | |
| `spots_count` | int8 | no | `0` | denormalized count |
| `reported_count` | int8 | no | `0` | |
| `created_at` | timestamptz | no | `now()` | |
| `updated_at` | timestamptz | no | `now()` | |
| `profile_image_asset_id` | uuid | yes | — | → media_assets |
| `suspended_for_reports_at` | timestamptz | yes | — | when set, public content hidden from discovery/feed until support clears |
| `account_status` | text | no | `'active'` | check: `active`\|`restricted`\|`suspended`\|`banned` |
| `moderation_status` | text | no | `'approved'` | check: `approved`\|`flagged`\|`rejected`\|`pending_review` |

**RLS/writes:** profile writes go through **`sync_current_user_v1`** (SECURITY DEFINER), NOT a direct upsert. `authenticated` has column-scoped update that deliberately **excludes `id`**. On conflict the RPC only refreshes login/heartbeat fields (`email`, `email_verified`, `last_active_at`, `locale`) and preserves user-managed fields (`username`, `username_lower`, `is_private`, `profile_image_url`, `is_pro`, `pro_until`). Safe public projection is via the `users_public` view (no email).

### `vibe_tags` — global vibe catalog
20 rows. PK `id`.

| Column | Type | Null | Default | Notes |
|--------|------|------|---------|-------|
| `id` | uuid | no | `gen_random_uuid()` | |
| `name` | text | no | — | display name |
| `name_lower` | text | no | — | **unique**, canonical key |
| `created_at` | timestamptz | no | `now()` | |

### `spots` — user posts
3,895 rows. PK `id`. FKs `user_id → users.id`, `vibe_tag_id → vibe_tags.id`.

| Column | Type | Null | Default | Notes |
|--------|------|------|---------|-------|
| `id` | uuid | no | `gen_random_uuid()` | |
| `user_id` | uuid | no | — | creator |
| `vibe_tag_id` | uuid | yes | — | primary vibe (multi-vibe via `spot_vibe_tags`) |
| `caption` | text | no | `''` | |
| `latitude` | float8 | no | — | |
| `longitude` | float8 | no | — | |
| `geohash` | text | yes | — | |
| `location_name` | text | yes | — | display place name |
| `likes_count` | int8 | no | `0` | denormalized (maintained by trigger) |
| `saves_count` | int8 | no | `0` | denormalized (maintained by trigger) |
| `author_is_private_snapshot` | bool | no | `false` | snapshot of author privacy at post time |
| `created_at` | timestamptz | no | `now()` | |
| `updated_at` | timestamptz | no | `now()` | |
| `location` | geography (PostGIS point) | yes | — | derived from lat/lng by trigger `set_spot_location_v1` |
| `media_display_aspect_ratio` | numeric | no | `1.0` | width/height of cover image (lowest sort_index) |
| `media_count` | int4 | no | `0` | number of `spot_images` |
| `media_layout_version` | int4 | no | `1` | layout contract; app uses `1` (aspect-aware cards) |
| `moderation_status` | text | no | `'approved'` | check: `approved`\|`flagged`\|`rejected`\|`pending_review` |
| `hidden_at` | timestamptz | yes | — | |
| `hidden_reason` | text | yes | — | |

### `spot_images` — one row per image (order by `sort_index`)
7,713 rows. PK `id`. FKs `spot_id → spots.id`, `media_asset_id → media_assets.id`.

| Column | Type | Null | Default | Notes |
|--------|------|------|---------|-------|
| `id` | uuid | no | `gen_random_uuid()` | |
| `spot_id` | uuid | no | — | |
| `storage_path` | text | no | — | path within `storage_bucket` |
| `public_url` | text | no | — | may need signing to display |
| `sort_index` | int4 | no | `0` | cover = lowest |
| `created_at` | timestamptz | no | `now()` | |
| `media_asset_id` | uuid | yes | — | → media_assets |
| `storage_bucket` | text | no | `'spots'` | default `spots`; moderated posts may use `approved_spot_images` |
| `width` | int4 | yes | — | |
| `height` | int4 | yes | — | |
| `aspect_ratio` | numeric | yes | — | raw w/h |
| `display_aspect_ratio` | numeric | no | `1.0` | clamped for display |
| `orientation` | text | no | `'square'` | check: `landscape`\|`square`\|`portrait` |

### `spot_vibe_tags` — spot↔vibe junction (multi-vibe)
3,895 rows. **Composite PK `(spot_id, vibe_tag_id)`**. FKs to `spots`, `vibe_tags`.

| Column | Type | Null | Default |
|--------|------|------|---------|
| `spot_id` | uuid | no | — |
| `vibe_tag_id` | uuid | no | — |
| `sort_order` | int4 | no | `0` |

---

## Social graph

### `follows` — follower → followee
PK `id`. FKs both → users. **Unique** `(follower_id, followee_id)` (index `follows_follower_followee_uidx`).

| Column | Type | Null | Default |
|--------|------|------|---------|
| `id` | uuid | no | `gen_random_uuid()` |
| `follower_id` | uuid | no | — |
| `followee_id` | uuid | no | — |
| `created_at` | timestamptz | no | `now()` |

**RLS:** `follows_select_related`, `follows_insert_self`, `follows_delete_related` (you can only manage edges where you are the follower).

### `follow_requests` — pending requests before a follow
PK `id`. FKs `requester_id`, `target_user_id` → users.

| Column | Type | Null | Default | Notes |
|--------|------|------|---------|-------|
| `id` | uuid | no | `gen_random_uuid()` | |
| `requester_id` | uuid | no | — | |
| `target_user_id` | uuid | no | — | |
| `status` | text | no | `'pending'` | check: `pending`\|`accepted`\|`rejected`\|`cancelled` |
| `created_at` | timestamptz | no | `now()` | |

Accept flow: insert into `follows`, then delete/settle the pending request. Client update on this table is revoked by policy (server-controlled transitions).

### `user_blocks` — block list
PK `id`. FKs `blocker_id`, `blocked_user_id` → users.

| Column | Type | Null | Default |
|--------|------|------|---------|
| `id` | uuid | no | `gen_random_uuid()` |
| `blocker_id` | uuid | no | — |
| `blocked_user_id` | uuid | no | — |
| `created_at` | timestamptz | no | `now()` |

**RLS:** authenticated select/insert/delete; insert policy uses `user_blocks_duplicate_exists()` (SECURITY DEFINER, `row_security=off`) to avoid recursion. Preferred write path is RPC `block_user_v1`.

---

## Engagement

### `spot_likes`
27 rows. PK `id`. FKs → users, spots. (Replaces legacy `users.liked_spots` array.)

| Column | Type | Null | Default |
|--------|------|------|---------|
| `id` | uuid | no | `gen_random_uuid()` |
| `user_id` | uuid | no | — |
| `spot_id` | uuid | no | — |
| `created_at` | timestamptz | no | `now()` |

Trigger `_on_spot_like_change_v1` maintains `spots.likes_count`.

### `spot_bookmarks`
3 rows. PK `id`. FKs → users, spots. (Replaces legacy `users.bookmarked_spots` array.)

| Column | Type | Null | Default |
|--------|------|------|---------|
| `id` | uuid | no | `gen_random_uuid()` |
| `user_id` | uuid | no | — |
| `spot_id` | uuid | no | — |
| `created_at` | timestamptz | no | `now()` |

Trigger `_on_spot_bookmark_change_v1` maintains `spots.saves_count`. **Free cap = 50** enforced client-side before paywall.

### `bookmark_collections` — Pro bookmark folders
PK `id`. FK `user_id → users.id`.

| Column | Type | Null | Default |
|--------|------|------|---------|
| `id` | uuid | no | `gen_random_uuid()` |
| `user_id` | uuid | no | — |
| `name` | text | no | — |
| `sort_index` | int4 | no | `0` |
| `created_at` | timestamptz | no | `now()` |
| `updated_at` | timestamptz | no | `now()` |

### `bookmark_collection_spots` — collection ↔ spot
PK `id`. FKs `collection_id → bookmark_collections.id`, `spot_id → spots.id`.

| Column | Type | Null | Default |
|--------|------|------|---------|
| `id` | uuid | no | `gen_random_uuid()` |
| `collection_id` | uuid | no | — |
| `spot_id` | uuid | no | — |
| `sort_index` | int4 | no | `0` |
| `created_at` | timestamptz | no | `now()` |

---

## Feed / ranking (see [16-feed-ranking-algorithm.md](16-feed-ranking-algorithm.md))

### `feed_impressions` — durable dedupe of served spots
1,971 rows. **Composite PK `(user_id, spot_id)`**.

| Column | Type | Null | Default | Notes |
|--------|------|------|---------|-------|
| `user_id` | uuid | no | — | |
| `spot_id` | uuid | no | — | |
| `first_seen_at` | timestamptz | no | `now()` | |
| `last_seen_at` | timestamptz | no | `now()` | |
| `seen_count` | int4 | no | `1` | check `> 0` |
| `first_source` / `last_source` | text | yes | — | feed bucket source |
| `first_rank` / `last_rank` | int4 | yes | — | |
| `first_score` / `last_score` | float8 | yes | — | |
| `first_batch_id` / `last_batch_id` | uuid | yes | — | |
| `created_at` / `updated_at` | timestamptz | no | `now()` | |

Maintenance RPC: `prune_feed_impressions_v1(p_max_per_user=5000, p_ttl_days=90)`.

### `user_feed_events` — raw behavioral signals (insert-only by owner)
1,076 rows. PK `id` (`extensions.gen_random_uuid()`). FKs → users, spots, users(creator), vibe_tags.

| Column | Type | Null | Default | Notes |
|--------|------|------|---------|-------|
| `id` | uuid | no | gen_random_uuid | |
| `user_id` | uuid | no | — | |
| `spot_id` | uuid | yes | — | |
| `creator_id` | uuid | yes | — | |
| `vibe_tag_id` | uuid | yes | — | |
| `event_type` | text | no | — | check enum below |
| `event_strength` | float8 | no | `0` | server-assigned weight |
| `dwell_ms` | int4 | yes | — | |
| `metadata` | jsonb | no | `'{}'` | |
| `created_at` | timestamptz | no | `now()` | |

`event_type` check values: `block_author, detail_open, follow_author, hide, impression, like, long_dwell, map_pin_tap, profile_tap, quick_skip, report, save, share, unfollow_author, unlike, unsave, vibe_tap, visible_2s`.
Archive RPC: `archive_user_feed_events_v1(p_ttl_days=180)`.

### `user_hidden_spots` — per-viewer suppression
**Composite PK `(user_id, spot_id)`**.

| Column | Type | Null | Default | Notes |
|--------|------|------|---------|-------|
| `user_id` | uuid | no | — | |
| `spot_id` | uuid | no | — | |
| `reason` | text | no | — | check: `hide`\|`report`\|`not_interested` |
| `metadata` | jsonb | no | `'{}'` | |
| `created_at`/`updated_at` | timestamptz | no | `now()` | |

Excluded from feed and map.

### `user_vibe_affinities` — learned vibe preference
41 rows. **Composite PK `(user_id, vibe_tag_id)`**. Written by `record_feed_event_v1` only.

| Column | Type | Null | Default |
|--------|------|------|---------|
| `user_id` | uuid | no | — |
| `vibe_tag_id` | uuid | no | — |
| `score` | float8 | no | `0` |
| `positive_events` | int4 | no | `0` |
| `negative_events` | int4 | no | `0` |
| `last_event_at` | timestamptz | yes | — |
| `created_at`/`updated_at` | timestamptz | no | `now()` |

### `user_creator_affinities` — learned creator preference
223 rows. **Composite PK `(user_id, creator_id)`**. Same shape as vibe affinities with `creator_id` instead of `vibe_tag_id`.

### `user_feed_profiles` — compact computed ranking profile
507 rows. PK `user_id`.

| Column | Type | Null | Default |
|--------|------|------|---------|
| `user_id` | uuid | no | — |
| `profile_version` | int4 | no | `1` |
| `profile` | jsonb | no | `'{}'` |
| `last_computed_at` | timestamptz | yes | — |
| `created_at`/`updated_at` | timestamptz | no | `now()` |

The `profile` jsonb structure (stats, top_vibes, top_creators, ranker_constants, event_summary_30d/90d) is detailed in [16-feed-ranking-algorithm.md](16-feed-ranking-algorithm.md).

---

## Media & moderation (see [13-moderation-safety.md](13-moderation-safety.md))

### `media_assets` — every uploaded image
7,714 rows. PK `id`. FKs `owner_id → auth.users.id`, `linked_spot_id → spots.id`.

| Column | Type | Null | Default | Notes |
|--------|------|------|---------|-------|
| `id` | uuid | no | `gen_random_uuid()` | |
| `owner_id` | uuid | no | — | |
| `kind` | text | no | — | check: `spot_image`\|`profile_image` |
| `status` | text | no | `'pending'` | check: `pending`\|`approved`\|`rejected`\|`failed`\|`deleted`\|`legacy_unmoderated` |
| `pending_bucket` | text | yes | — | e.g. `pending_images` |
| `pending_path` | text | yes | — | `{userId}/{assetId}.jpg` |
| `approved_bucket` | text | yes | — | |
| `approved_path` | text | yes | — | |
| `linked_spot_id` | uuid | yes | — | |
| `mime_type` | text | yes | — | `image/jpeg` |
| `byte_size` | int4 | yes | — | |
| `width` / `height` | int4 | yes | — | |
| `sha256` | text | yes | — | |
| `scores` | jsonb | no | `'{}'` | moderation scores |
| `azure_result` | jsonb | yes | — | raw provider result |
| `rejection_reason` | text | yes | — | |
| `moderation_provider` | text | no | `'azure_content_safety'` | |
| `moderated_at` | timestamptz | yes | — | |
| `created_at`/`updated_at` | timestamptz | no | `now()` | |

### `media_moderation_events` — moderation audit per asset
8 rows. PK `id`. FKs `media_asset_id → media_assets.id`, `actor_user_id → auth.users.id`.

| Column | Type | Null | Default | Notes |
|--------|------|------|---------|-------|
| `id` | uuid | no | `gen_random_uuid()` | |
| `media_asset_id` | uuid | no | — | |
| `actor_user_id` | uuid | yes | — | |
| `provider` | text | no | `'azure_content_safety'` | |
| `status` | text | no | — | check: `approved`\|`rejected`\|`failed` |
| `scores` | jsonb | no | `'{}'` | |
| `raw_result` | jsonb | yes | — | |
| `reason` | text | yes | — | |
| `error_code` | text | yes | — | |
| `created_at` | timestamptz | no | `now()` | |

### `content_moderation_results` — text/profile filtering results (admin/service-role only)
PK `id`. FKs to auth.users.

| Column | Type | Null | Default | Notes |
|--------|------|------|---------|-------|
| `id` | uuid | no | `gen_random_uuid()` | |
| `target_type` | text | no | — | check: `spot`\|`spot_image`\|`profile`\|`comment`\|`collection`\|`other` |
| `target_id` | uuid | no | — | |
| `user_id` | uuid | yes | — | |
| `input_type` | text | no | — | check: `text`\|`image`\|`mixed` |
| `status` | text | no | — | check: `approved`\|`flagged`\|`rejected`\|`pending_review` |
| `categories` | jsonb | no | `'{}'` | |
| `matched_terms` | text[] | yes | — | |
| `provider` | text | no | `'spot_internal'` | |
| `provider_response` | jsonb | yes | — | |
| `created_at` | timestamptz | no | `now()` | |
| `reviewed_at` | timestamptz | yes | — | |
| `reviewer_user_id` | uuid | yes | — | |
| `reviewer_notes` | text | yes | — | |

---

## Reports & moderation workflow

### `reports` — user reports of content/users
PK `id`. FKs `reporter_id → users.id`, `owner_id → users.id`, `spot_id → spots.id`, `reviewer_user_id → auth.users.id`.

| Column | Type | Null | Default | Notes |
|--------|------|------|---------|-------|
| `id` | uuid | no | `gen_random_uuid()` | |
| `created_at` | timestamptz | no | `now()` | |
| `spot_id` | uuid | yes | — | for spot reports |
| `reporter_id` | uuid | no | — | |
| `owner_id` | uuid | no | — | reported content owner; for spot must match `spots.user_id` |
| `reason` | text | no | — | see report reasons in [13](13-moderation-safety.md) |
| `details` | text | no | `''` | |
| `platform` | text | no | — | `ios` / `android` |
| `app_version` | text | no | — | |
| `block_requested` | bool | no | `false` | reporter chose to also block author |
| `target_type` | text | no | `'spot'` | check: `spot`\|`profile`\|`spot_image`\|`comment`\|`collection`\|`other` |
| `target_id` | uuid | yes | — | reported target uuid |
| `status` | text | no | `'open'` | check: `open`\|`reviewing`\|`actioned`\|`dismissed` |
| `priority` | text | no | `'normal'` | check: `low`\|`normal`\|`high`\|`urgent` (assigned by submit RPC based on reason) |
| `reviewed_at` / `resolved_at` | timestamptz | yes | — | |
| `reviewer_user_id` | uuid | yes | — | |
| `reviewer_notes` / `action_taken` | text | yes | — | |

**RLS:** client **insert-only** via RPC `submit_content_report` (SECURITY DEFINER). After-insert trigger applies **report-volume suspension**: rolling 30 days, **≥5 reports AND ≥3 distinct reporters** against the same `owner_id` → sets `users.suspended_for_reports_at = now()` (idempotent). Suspended authors are hidden from `can_view_author`, `users_public`, and the feed RPCs. Unsuspend (support): `update public.users set suspended_for_reports_at = null where id = '<uuid>'`.

### `moderation_events` — append-only safety audit log (never readable by end users)
PK `id`. FKs to reports, auth.users.

| Column | Type | Null | Default | Notes |
|--------|------|------|---------|-------|
| `id` | uuid | no | `gen_random_uuid()` | |
| `event_type` | text | no | — | check: `report_created`\|`user_blocked`\|`content_filter_rejected`\|`content_filter_flagged`\|`content_removed`\|`user_warned`\|`user_suspended`\|`user_banned`\|`report_resolved` |
| `actor_user_id` | uuid | yes | — | |
| `subject_user_id` | uuid | yes | — | |
| `target_type` | text | yes | — | |
| `target_id` | uuid | yes | — | |
| `report_id` | uuid | yes | — | |
| `metadata` | jsonb | no | `'{}'` | |
| `created_at` | timestamptz | no | `now()` | |

There is also a service-role `moderation_queue` view prioritizing open reports for the 24-hour SLA.

---

## Legal / consent

### `terms_versions` — active + historical Terms/Privacy releases
1 row. PK `id`. Only one row should be `is_active = true`.

| Column | Type | Null | Default |
|--------|------|------|---------|
| `id` | uuid | no | `gen_random_uuid()` |
| `version` | text | no (unique) | — |
| `title` | text | no | — |
| `terms_url` | text | no | — |
| `privacy_url` | text | no | — |
| `is_active` | bool | no | `false` |
| `effective_at` | timestamptz | no | `now()` |
| `created_at` | timestamptz | no | `now()` |

### `user_terms_acceptances` — proof of consent
3 rows. PK `id`. FKs `user_id → auth.users.id`, `terms_version_id → terms_versions.id`.

| Column | Type | Null | Default |
|--------|------|------|---------|
| `id` | uuid | no | `gen_random_uuid()` |
| `user_id` | uuid | no | — |
| `terms_version_id` | uuid | no | — |
| `accepted_at` | timestamptz | no | `now()` |
| `platform` | text | no | `'ios'` (set `'android'`) |
| `app_version` | text | yes | — |
| `build_number` | text | yes | — |
| `device_info` | text | yes | — |
| `created_at` | timestamptz | no | `now()` |

Written via RPC `record_terms_acceptance_v1`; read via `has_accepted_active_terms`.

---

## Support

### `support_requests`
6 rows. PK `id`. Standalone.

| Column | Type | Null | Default |
|--------|------|------|---------|
| `id` | uuid | no | `gen_random_uuid()` |
| `created_at` | timestamptz | no | `now()` |
| `name` | text | no | — |
| `email` | text | no | — |
| `issue_type` | text | no | — |
| `subject` | text | no | — |
| `message` | text | no | — |

> Note: iOS uses a `mailto:` link for support rather than writing this table directly. On Android you can do the same, or POST to this table if you build an in-app support form.

---

## Views

- **`users_public`** — safe public projection of `users` (no email). Rows include: self, users you block (so the Blocked Users screen can resolve username/avatar), and discoverable unblocked users. Fields used by the client: `id`, `username`, `profile_image_url`, `is_pro`, `pro_until` (and privacy fields). Use this everywhere you show another user.
- **`moderation_queue`** — service-role-only prioritized open reports.

## Domain enum quick-reference (string values)

- **account_status:** active, restricted, suspended, banned
- **moderation_status (users/spots):** approved, flagged, rejected, pending_review
- **media_assets.kind:** spot_image, profile_image
- **media_assets.status:** pending, approved, rejected, failed, deleted, legacy_unmoderated
- **media_moderation_events.status:** approved, rejected, failed
- **follow_requests.status:** pending, accepted, rejected, cancelled
- **user_hidden_spots.reason:** hide, report, not_interested
- **reports.target_type:** spot, profile, spot_image, comment, collection, other
- **reports.status:** open, reviewing, actioned, dismissed
- **reports.priority:** low, normal, high, urgent
- **moderation_events.event_type:** report_created, user_blocked, content_filter_rejected, content_filter_flagged, content_removed, user_warned, user_suspended, user_banned, report_resolved
- **user_feed_events.event_type:** see list above (18 values)
- **spot_images.orientation:** landscape, square, portrait

RPC signatures and how the client reads/writes each table are in [04-backend-api.md](04-backend-api.md).
