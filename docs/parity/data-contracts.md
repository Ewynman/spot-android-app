# Data contracts

The wire contract between Android and Supabase. This matches iOS exactly —
if you find drift with the live database, the **live database wins**.

**iOS callers** are cited for cross-check. All calls go through supabase-kt
via `SupabaseClientProvider` and a typed repository.

## RPCs called from the client

| RPC | Purpose | iOS caller | Android caller |
|-----|---------|------------|----------------|
| `sync_current_user_v1` | Upsert profile row; source of username availability | `SupabaseUserService.swift` | `SupabaseAuthRepository` / `SupabaseUserSessionRepository` |
| `is_username_available` | Live username validation | `AuthViewModel.swift` | `SupabaseAuthRepository` |
| `delete_my_account` | Account deletion after re-auth | `AuthService.swift` | `SupabaseSettingsRepository` |
| `get_home_feed_v1` | Paginated home feed | `FeedAPI.swift` | `SupabaseFeedRepository` |
| `get_home_feed_status_v1` | Empty-state reason for feed | `FeedAPI.swift` | `SupabaseFeedRepository` |
| `get_map_spots_v1` | Viewport spot fetch | `FeedAPI.swift` | `SupabaseMapRepository` |
| `record_feed_event_v1` | Impression / dwell / like / bookmark event | `FeedEventService.swift` | `FeedEventService` (Android) |
| `recompute_my_feed_profile_v1` | Force-rebuild ranking snapshot | `FeedAPI.swift`, `AlgorithmDebugView` | Not yet — task 05 will use it via a repository |
| `publish_spot_with_approved_media_assets_v1` | Finalize a spot after moderation | `SpotSupabaseRepository.swift` | `SupabaseSpotPublishRepository` |
| `update_spot_metadata_v1` | Non-Pro spot metadata update | `SpotSupabaseRepository.swift` | (via `SpotPublishRepository` — verify) |
| `update_spot_editor_v1` | **Pro** spot edit (media + vibes + location) | `EditSpotEditorSupport.swift` | Task 01 — new `SpotEditRepository` |
| `list_spot_ids_for_vibe_search_v1` | Search: vibe → spot ids | `SpotSupabaseRepository.swift` | `SupabaseSearchRepository` |
| `list_spot_ids_for_location_and_vibe_search_v1` | Search: location+vibe → spot ids | `SpotSupabaseRepository.swift` | `SupabaseSearchRepository` |
| `remove_saved_spot_v1` | Bookmark removal | `SupabaseSpotBookmarkStore.swift` | `SupabaseEngagementRepository` |
| `submit_content_report` | Report spot or profile | `ModerationService.swift` | `SupabaseSafetyRepository` |
| `block_user_v1` | Block a user | `ModerationService.swift` | `SupabaseSafetyRepository` |
| `record_terms_acceptance_v1` | Persist terms acceptance | `TermsAcceptanceService.swift` | `SupabaseTermsRepository` |
| `has_accepted_active_terms` | Check terms gate | `TermsAcceptanceService.swift` | `SupabaseTermsRepository` |

### `platform` field (Android-specific)

Android **must** pass `platform: "android"` on:

- `submit_content_report` — enables review triage.
- `record_terms_acceptance_v1` — required for compliance reporting.

iOS omits this field (defaults to `ios` server-side). Confirm the server
accepts an explicit `platform` param before shipping (task 09).

## Tables read/written from the client

| Table / view | Ops | Notes |
|--------------|-----|-------|
| `users` | R + U (self only) via `sync_current_user_v1` | **Never** direct upsert. Read-only for other rows via RLS. |
| `users_public` | R | Public profile projection. Search + follower lists. |
| `spots` | R (search, detail); no direct writes | Writes always through `publish_*` / `update_*` RPCs. |
| `spot_images` | R | Signed URL resolution. |
| `spot_vibe_tags` | R | Junction. |
| `vibe_tags` | R + I | Insert only for new vibe creation via Pro flow. |
| `spot_likes` | I + D | Optimistic; server RLS ensures single-row per (user, spot). |
| `spot_bookmarks` | I | Upsert only; **removals go through `remove_saved_spot_v1`**. |
| `bookmark_collections` | R + I + U + D (Pro) | Owner-only via RLS. |
| `bookmark_collection_spots` | R + I + D (Pro) | Junction. |
| `follows` | R + I + D | Public + private profile handling. |
| `follow_requests` | R + I + U + D | Private accounts only. |
| `user_blocks` | R + I + D | Also updates `UserSessionHolder.blocked`. |
| `terms_versions` | R | Active version + `terms_url`, `privacy_url`. |
| `media_assets` | R | Pending moderated asset rows tied to publish. |
| `user_feed_profiles` | R | Debug/algorithm snapshot only. **Task 05** moves this off the ViewModel. |

Server-only tables (never touched by the client): `feed_impressions`, `reports`,
`user_terms_acceptances`, moderation queues. These are written via RPCs.

## Storage buckets

| Bucket | Purpose | Public? |
|--------|---------|---------|
| `avatars` | Approved profile avatars | Public URL |
| `spots` | Approved spot images (default) | Signed URL (7-day) |
| `pending_images` | Pre-moderation uploads | Signed URL for the uploader only |

Feed and map RPCs return a `primary_storage_bucket` per row so per-row bucket
selection works. Do **not** hardcode `"spots"` in image loading paths — pass
through the DTO's bucket.

## Edge functions

| Function | Purpose | Called by |
|----------|---------|-----------|
| `moderate-image` | Azure Content Safety image check; must return `{ approved: boolean, ... }` | Every publish + edit before finalizing |
| `staging-verify-email` | Staging-only internal test OTP (format `UT####`) | Debug builds only; do **not** ship in Release |

## DTOs (Android)

All under `data/dto/`. Field names are **snake_case** matching Postgres.

Key DTOs:

- `SpotRowDto` — raw `spots` row
- `HomeFeedRowDto` — `get_home_feed_v1` result row (includes hydrated author,
  vibe, primary image, ranking, seen state)
- `MapSpotRowDto` — `get_map_spots_v1` result row (compact)
- `UserRowDto` — self row (email, verified, is_pro, pro_until, moderation)
- `UserBriefRowDto` — `users_public` projection
- `VibeRowDto` — `vibe_tags` row
- `SpotImageRowDto` — with `storage_bucket`, `display_aspect_ratio`
- `SpotVibeJunctionRowDto` — `spot_vibe_tags`
- `MediaAssetDto` — pending uploads
- `EngagementRowDto` — likes/bookmarks/blocks
- `BookmarkCollectionDto` — Pro collections + junction
- `FollowRequestRowDto` — private account requests
- `HomeFeedStatusDto` — empty-state reason
- `SearchSpotIdRowDto` — search results

**Mapping to domain models** (`Spot`, `User`, `VibeTag`, etc.) lives in
`data/mapper/`.

## RLS reality check

Because RLS is authoritative, expect these observable behaviors:

- Blocked users' spots are filtered out of feed / map / search **server-side**.
- Private-account content is only visible to accepted followers.
- Empty results from RLS filtering are **not errors** — surface an appropriate
  empty state, not an error toast.
- 401/403 on write means either "session expired" or "you don't own this row."
  Handle each explicitly.

## Environments

| Env | Project ref | Anon key |
|-----|-------------|----------|
| Staging (DEBUG) | `aeurigbbohyxvtsfiyul` | injected via `local.properties` |
| Production (Release) | (matches iOS Release; see `local.properties`) | injected at build time |

**Never** embed real anon keys in the repo. `local.properties` is gitignored.

## RPC add / rename policy

Only add a new RPC if the iOS app also uses it. If iOS is missing a call the
Android task requires, that's a **backend task**, not an Android task —
escalate rather than shipping a client fork.
