# 04 — Backend API (RPCs, Storage, Edge Function, Auth)

All server access is through **Supabase**: Auth (GoTrue), PostgREST (tables + RPC), Storage, and one Edge Function. RLS is authoritative. This file lists the full RPC catalog (live signatures), how the client reads/writes, storage buckets, signed-URL handling, and the moderation edge function contract.

## RPC catalog (live signatures)

All RPCs are in schema `public`. Call via `postgrest.rpc("name", params)`. Types shown are Postgres types; map to Kotlin (`uuid`→String, `double precision`→Double, `timestamptz`→ISO-8601 String, `uuid[]`→List<String>, jsonb→JsonObject).

### Identity & account

| RPC | Args | Returns | SecDef | Purpose |
|-----|------|---------|--------|---------|
| `sync_current_user_v1` | `p_username text, p_username_lower text, p_email text=null, p_email_verified bool=false, p_is_private bool=false, p_locale text=null, p_last_active_at timestamptz=null` | `uuid` | ✅ | Upsert caller's `users` row. On conflict refreshes only email/email_verified/last_active_at/locale; preserves username/is_private/profile_image_url/is_pro/pro_until. Call on every app start / login. |
| `is_username_available` | `p_username text` | `bool` | ✅ | Username availability check at signup / username setup. |
| `resolve_login_email` | `p_username text` | `text` | ✅ | Resolve a username to its login email (login by username). |
| `delete_my_account` | — | `void` | ✅ | Full account deletion (purges rows + storage prefixes). Call after re-auth. |

### Feed & ranking

| RPC | Args | Returns | SecDef | Purpose |
|-----|------|---------|--------|---------|
| `get_home_feed_v1` | `p_limit int=24, p_viewer_lat double=null, p_viewer_lng double=null, p_batch_id uuid=gen_random_uuid(), p_force_seen_fallback bool=false` | TABLE (see below) | ✅ | Ranked, paginated home feed. |
| `get_home_feed_status_v1` | — | TABLE(`total_spots int, eligible_spots int, unseen_eligible_spots int, seen_eligible_spots int, status text`) | ✅ | Empty-state reason. `status` ∈ `has_unseen`\|`caught_up`\|`no_eligible_spots`\|`no_spots_global`. |
| `get_map_spots_v1` | `p_min_lat, p_min_lng, p_max_lat, p_max_lng, p_center_lat, p_center_lng double, p_limit int=250` | TABLE (see below) | ✅ | Viewport spots for the map (bbox + center for distance sort). |
| `record_feed_event_v1` | `p_spot_id uuid, p_event_type text, p_dwell_ms int=null, p_metadata jsonb='{}'` | `void` | ✅ | Log a behavioral event; also updates affinities. |
| `recompute_my_feed_profile_v1` | — | `jsonb` | ✅ | Recompute + return caller's feed profile. |

**`get_home_feed_v1` returned columns:** `spot_id uuid, user_id uuid, vibe_tag_id uuid, caption text, latitude double, longitude double, location_name text, likes_count bigint, saves_count bigint, created_at timestamptz, updated_at timestamptz, author_username text, author_profile_image_url text, author_is_private bool, vibe_name text, primary_storage_path text, primary_public_url text, source_bucket text, rank_position int, ranking_score double, seen_before bool, last_seen_at timestamptz`.

**`get_map_spots_v1` returned columns:** `spot_id, user_id, vibe_tag_id, caption, latitude, longitude, location_name, created_at, author_username, author_profile_image_url, vibe_name, primary_storage_path, primary_public_url, distance_meters`.

> Client-side default when `source_bucket` absent: `"personalized_unseen"`; `rank_position`/`ranking_score` default 0; `seen_before` default false.

### Spots (publish / edit / search grids)

| RPC | Args | Returns | SecDef | Purpose |
|-----|------|---------|--------|---------|
| `publish_spot_with_approved_media_assets_v1` | `p_vibe_tag_ids uuid[], p_latitude double, p_longitude double, p_location_name text, p_media_asset_ids uuid[]` | `uuid` (new spot id) | ✅ | Final publish once media assets are approved. |
| `update_spot_metadata_v1` | `p_spot_id uuid, p_vibe_tag_ids uuid[], p_latitude double, p_longitude double, p_location_name text` | `void` | ✅ | Edit a spot's vibes/location (Pro edit). |
| `list_spot_ids_for_vibe_search_v1` | `p_vibe_tag_ids uuid[], p_limit int, p_offset int` | TABLE(`spot_id uuid, created_at timestamptz`) | ✅ | Vibe search grid ids. |
| `list_spot_ids_for_location_and_vibe_search_v1` | `p_location_pattern text, p_vibe_tag_ids uuid[], p_limit int, p_offset int` | TABLE(`spot_id uuid, created_at timestamptz`) | ✅ | Pro location+vibe search grid ids. |

### Safety / moderation / consent

| RPC | Args | Returns | SecDef | Purpose |
|-----|------|---------|--------|---------|
| `block_user_v1` | `p_blocked_user_id uuid, p_source_target_type text=null, p_source_target_id uuid=null, p_reason text=null` | `uuid` (block id) | ❌ (RLS) | Block a user. |
| `submit_content_report` | `p_target_type text, p_target_id uuid, p_reported_user_id uuid, p_reason text, p_details text='', p_block_requested bool=false` | `uuid` (report id) | ✅ | Report a spot/profile/etc; assigns priority, writes moderation audit, may trigger suspension. |
| `record_terms_acceptance_v1` | `p_app_version text=null, p_build_number text=null, p_device_info text=null` | `uuid` | ✅ | Record acceptance of active terms. |
| `has_accepted_active_terms` | — | `bool` | ❌ | Whether caller accepted the active terms version. |

### Maintenance / internal (service-role or cron; not called by the client)

`archive_user_feed_events_v1`, `prune_feed_impressions_v1`, `recompute_stale_feed_profiles_v1`, `recompute_user_feed_profile_v1`, `feed_event_weight_v1`, `set_spot_location_v1` (trigger), `_on_spot_like_change_v1`/`_on_spot_bookmark_change_v1` (triggers), `_record_feed_event_for_user_v1`.

## Direct table access (PostgREST `.from(...)`)

The client reads/writes these tables directly (subject to RLS) in addition to RPCs:

| Table/view | Client operations |
|------------|-------------------|
| `users` | Read own row (flags, pro); update via RPC preferred |
| `users_public` | Read any displayable user (search, feed author enrichment, profiles, follow requests, privacy cache) |
| `spots` | Read (profile grids, search by `location_name` ILIKE), delete own |
| `spot_images` | Read gallery/preview paths for signing |
| `spot_vibe_tags` | Read multi-vibe |
| `vibe_tags` | Read catalog; create custom (Pro) by `name`/`name_lower` |
| `media_assets` | Insert pending assets before moderation |
| `spot_likes` / `spot_bookmarks` | Insert/delete like & save |
| `follows` | Insert/delete follow edges |
| `follow_requests` | Insert (request), read incoming/outgoing; status transitions server-controlled |
| `user_blocks` | Read block list (write via `block_user_v1`) |
| `bookmark_collections` / `bookmark_collection_spots` | CRUD Pro collections |
| `user_feed_profiles` | Read own (algorithm debug screen) |
| `terms_versions` | Read active row (`is_active=true`) |

## Storage buckets

| Bucket | Visibility | Purpose | URL strategy |
|--------|-----------|---------|--------------|
| `spots` | private | Approved spot images (`spot_images.storage_path`) | **Signed URLs** (7-day expiry = 604800s) |
| `pending_images` | private | Pre-moderation uploads (`media_assets.pending_path`) | Signed / service-only |
| `avatars` | public | Profile photos | **Public URL** via `getPublicUrl` |

- Spot image display path: read `spot_images.storage_bucket` + `storage_path`, create a signed URL from that bucket (default bucket `spots`). Feed/map rows expose `primary_storage_path` (sign it) and `primary_public_url` (may already be usable for public content).
- Account deletion purges the `{userId}/` prefix in both `avatars` and `spots`.
- Coil (Android) should use a fetcher that requests signed URLs on demand and caches them until near expiry.

## Auth (GoTrue via supabase-kt)

- **Email/password** sign-up and sign-in (primary). Sign-up triggers **email OTP** (6-digit) confirmation.
- **Login by username**: call `resolve_login_email(username)` → sign in with the resolved email + password.
- **Password reset**: GoTrue reset email (requires an email identifier).
- **OAuth** [ANDROID DECISION]: iOS uses Sign in with Apple (`signInWithIdToken(provider:.apple)`). On Android, use **Google Sign-In** (and optionally Apple) via supabase-kt OAuth. Any OAuth account without a username hits the post-auth username gate.
- **Session**: supabase-kt persists + refreshes the session automatically in app-private encrypted storage. Expose the current user id via a small bridge for gates.
- **Every start/login** → call `sync_current_user_v1(...)` to upsert the profile and heartbeat `last_active_at`.
- Treat **401/403** and empty RLS-filtered results as expected when unauthenticated/unauthorized.

## Edge Function: `moderate-image`

- Slug `moderate-image`, `verify_jwt = true` (send the user's access token).
- **Request body:** `{ "mediaAssetId": "<uuid>" }`.
- **Response:** JSON including boolean `approved`; optional `reason` / `error`.
- Rejection reason string: `"image_policy_rejected"`. Unavailable: `"moderation_unavailable"`.
- Behavior: calls **Azure Content Safety** with server-held credentials, writes `media_assets.status`/`scores`/`azure_result` and a `media_moderation_events` row, and (on approve) sets the approved bucket/path so the asset can be published. Full flow in [08-post-flow.md](08-post-flow.md) and [13-moderation-safety.md](13-moderation-safety.md).

## Typical end-to-end flows (Android parity)

1. **Auth bootstrap:** GoTrue sign-up/in → `sync_current_user_v1` → (if OAuth w/o username) username gate → (if terms outdated) terms gate.
2. **Home feed:** `get_home_feed_v1` → map `HomeFeedRow` → sign `primary_storage_path` (bucket `spots`) → render; log `impression`/`visible_2s`/etc via `record_feed_event_v1`; dedupe persists via `feed_impressions` server-side.
3. **Map:** `get_map_spots_v1(bbox, center)` → `MapSpotRow` → sign images → pins.
4. **Publish:** insert `media_assets` (pending) → upload bytes to `pending_images/{userId}/{assetId}.jpg` → call `moderate-image` per asset → on all approved, `publish_spot_with_approved_media_assets_v1(...)`.
5. **Engagement:** like/save via `spot_likes`/`spot_bookmarks` insert/delete (triggers update counts); also emit feed events.
6. **Report/block:** `submit_content_report(...)` / `block_user_v1(...)`.
7. **Profile grid:** query `spots` + join `spot_images`, `spot_vibe_tags`, `vibe_tags`, `users_public`.

## DTO shapes for decoding (mirror iOS)

Use snake_case field names in your DTOs (the client does **not** enable global snake_case conversion; it names fields to match Postgres). Key DTOs:

- **HomeFeedRow / MapSpotRow** — as returned by the feed/map RPCs (columns listed above).
- **SpotRow** (`spots`): `id, user_id, vibe_tag_id, caption, latitude, longitude, location_name, likes_count, author_is_private_snapshot, created_at, media_display_aspect_ratio, media_count` (+ select `media_layout_version`).
- **SpotImageRow** (`spot_images`): `spot_id, sort_index, storage_path, public_url, storage_bucket`.
- **SpotVibeJunctionRow** (`spot_vibe_tags`): `spot_id, vibe_tag_id, sort_order`.
- **VibeRow** (`vibe_tags`): `id, name, name_lower`.
- **UserBriefRow** (`users_public`): `id, username, profile_image_url, is_pro, pro_until`.
- **MediaAssetInsert** (`media_assets`): `id, owner_id, kind='spot_image', status='pending', pending_bucket='pending_images', pending_path='{userId}/{assetId}.jpg', mime_type='image/jpeg', byte_size, width?, height?`.
- **FeedProfile** jsonb — see [16-feed-ranking-algorithm.md](16-feed-ranking-algorithm.md).

The composite domain models (`Spot`, `User`) are assembled in code from these DTOs — see field lists in [06-home-feed.md](06-home-feed.md) and [10-profile-social.md](10-profile-social.md).
