# 06 — Home Feed

The default surface after launch: a **server-ranked, paginated** list of Spots tailored to the viewer. Tab index 0, test tag `home.feedRoot`. Top bar shows the "SPOT" wordmark (no upload button — Post is its own tab).

## Data source

- Feed rows: **`get_home_feed_v1(p_limit=24, p_viewer_lat, p_viewer_lng, p_batch_id, p_force_seen_fallback)`**.
- Empty-state reason: **`get_home_feed_status_v1()`**.
- Ranking is **server-authoritative**. Client passes viewer lat/lng (if location available) and a fresh `batch_id` per fetch. See [16-feed-ranking-algorithm.md](16-feed-ranking-algorithm.md).
- After hydration, the client applies **light diversity reordering** on the first page using `user_feed_profiles` signal strength so one liked vibe doesn't dominate the visible window (mirror `FeedDiversity`).

## Domain model (`Spot`) assembled from `HomeFeedRow`

Map each `HomeFeedRow` into the UI `Spot` model. Fields (all nullable unless the source guarantees them):

| Spot field | Source | Notes |
|------------|--------|-------|
| `id` | `spot_id` | |
| `userId` | `user_id` | |
| `username` | `author_username` | |
| `userProfileImageURL` | `author_profile_image_url` | |
| `imageURL` / `thumbnailURL` | signed `primary_storage_path` (bucket `spots`) or `primary_public_url` | |
| `imageURLs` | resolved from `spot_images` when multi-image expanded | feed row is cover-only |
| `vibeTag` / `vibeTags` | `vibe_name` (+ full set when detail loaded) | |
| `latitude`/`longitude`/`locationName` | same-named cols | |
| `likes` | `likes_count` | |
| `isLiked` | client (membership in liked set) | |
| `isSaved` | client (membership in bookmarked set) | |
| `createdAt` | `created_at` | |
| `authorIsPrivate` | `author_is_private` | |
| `mediaDisplayAspectRatio` | `media_display_aspect_ratio` | drives card aspect |
| `mediaCount` | (from spots when known) | multi-image indicator |
| `authorIsPro` | derived from `users_public.is_pro` | Pro badge |

## Screen states

| State | UI |
|-------|-----|
| Loading + no cached spots | 3× skeleton cards |
| Loaded with spots | Scrollable list of `SpotCard`; pull-to-refresh |
| Empty | Empty view with status-specific copy (below) |
| Refresh error while spots exist | Toast over preserved content (don't clear the list) |

Feed load state machine (mirror `FeedLoadState`): `idle`, `loadingInitial`, `loadingMore`, `loaded`, `empty(reason)`, `error(message)`.

## Empty-state copy (from `get_home_feed_status_v1`)

| status | Title | Subtitle |
|--------|-------|----------|
| `caught_up` | "You're all caught up" | Pull to refresh / follow more people |
| `no_eligible_spots` | "Nothing to show yet" | Follow people, unblock, or change filters |
| `no_spots_global` | "No Spots Yet" | Be the first to post |

**Seen fallback:** if the feed comes back empty with status `caught_up`, automatically re-fetch with `p_force_seen_fallback = true` to show already-seen spots rather than a dead-end.

## Pagination & refresh

- Page size **24**.
- Initial load on first appear.
- **Infinite scroll**: when the user scrolls near the bottom, load the next page (`loadingMore`). Cancel in-flight load-more on refresh.
- **Pull-to-refresh** re-fetches from the top with a new `batch_id`.
- Server-side dedupe across sessions is durable via `feed_impressions` (client doesn't need to track seen ids across launches, but should avoid showing dupes within a session).

## `SpotCard` layout

Header → media gallery → interaction bar.

- **Header:** avatar, username, Pro badge (if `authorIsPro`), vibe chip(s), location name. Tapping username/avatar → profile route.
- **Media gallery:** aspect-aware using `mediaDisplayAspectRatio`; multi-image = horizontal pager with page indicator. Images are signed URLs from bucket `spots`.
- **Interaction bar:**
  - **Like / Unlike** → insert/delete `spot_likes`; optimistic count update; emit `like`/`unlike` feed events.
  - **Bookmark / Unbookmark** → insert/delete `spot_bookmarks`. **Free cap = 50** total bookmarks → over cap opens the paywall. For Pro users, the **first** bookmark of a spot can open a **collection picker** sheet.
  - **Overflow (⋮) menu:**
    - **Share** → share sheet with `https://spotapp.online/s/{spotId}`.
    - **Add to Collection** (Pro; else paywall).
    - **Report** (non-owner) → report sheet (see [13-moderation-safety.md](13-moderation-safety.md)).
    - **Block User** (non-owner) → block + remove that author's spots from the feed immediately.
    - **Edit** (owner; Pro else paywall) → edit spot screen.
    - **Delete** (owner) → confirm dialog → delete.

## Impressions & feed events

- Record an **impression** when a card becomes visible; `visible_2s` after 2s; `long_dwell` for extended dwell; `quick_skip` when scrolled past fast; plus `like/unlike/save/unsave/share/detail_open/profile_tap/vibe_tap/hide/report/block_author/follow_author/unfollow_author`.
- Send via `record_feed_event_v1(p_spot_id, p_event_type, p_dwell_ms?, p_metadata?)`. Batch/throttle to avoid excessive calls. These drive affinities and ranking.

## Post-success handling

- After a successful publish, the feed listens for a "spot posted" signal, inserts the new spot at the top, and shows a **"Spot posted!"** toast (~1.5s).
- To handle eventual consistency, retry a refresh up to **3×** (~700ms apart) so the new post reliably appears.

## Delete & local removal

- **Delete** is optimistic; roll back if the server delete fails. Track in-flight delete ids to avoid double-taps.
- A "locally remove" signal (by `spotId` or by `authorUserId`) removes items from the feed instantly (used by block/report and delete).

## Navigation

- Tapping a creator → profile screen (pushed).
- Reselecting the Home tab → scroll to top / refresh (via the tab-reselect event).

## Android implementation notes

- Use a `LazyColumn` with paging (Paging 3 optional, or manual page loading matching iOS semantics).
- Preserve list + scroll position across config changes (hoist to ViewModel).
- Optimistic like/bookmark with rollback on failure; keep the liked/bookmarked sets in the auth/session view model so cards across screens stay consistent.
- Emit feed events from a lightweight service with coalescing.
