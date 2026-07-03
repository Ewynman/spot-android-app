# 10 — Profile & Social

Tab index 4 (own profile), test tag `navigation.profileTab`. Also reachable for other users via navigation from feed/map/search. Covers profile layout, follow/unfollow, follow requests, private accounts, blocks, reports, likes/bookmarks, and collections.

## Domain model (`User`)

Assembled from `users` / `users_public`:

| Field | Type | Notes |
|-------|------|-------|
| `id` | String? | |
| `username` | String | required |
| `profileImageURL` | String? | |
| `isPrivate` | Bool | |
| `isCurrentUser` | Bool | client-computed |
| `vibeStats` | Map<String,Int>? | vibe usage stats |
| `createdAt` | Date? | |
| `blockedUsers` | List<String>? | own profile only |
| `customVibeTags` | List<String>? | Pro custom tags |

## Profile layout

- Top bar: "SPOT" wordmark; back button when pushed or viewing another user.
- **Header**: avatar (~100dp), username, Pro badge (if Pro), "N spots shared" (`users.spots_count`).
- **Tabs**: `["Spots", "Map"]`.
  - **Spots**: grid of the user's spot covers; tap → inline expanded `SpotCard`.
  - **Map**: a `ProfileMapView` (reuse the map component) with just this user's spots + drawer.

## Own-profile menu (overflow)

- **Go Pro** (non-Pro only) → paywall.
- **Your Likes** → likes grid (spots you liked).
- **Your Bookmarks** → Pro: collections screen; free: flat bookmarks grid.
- **Follow Requests** (private accounts only) → follow-requests screen, with a **badge count**.
- **Your Algorithm** → feed-profile/algorithm view (reads `user_feed_profiles`).
- **Settings** → settings (see [11-settings.md](11-settings.md)).

## Other-user menu (overflow)

- **Report User** → profile report sheet.
- **Block User** → confirm dialog → block.

## Follow model

| Account type | Actions |
|--------------|---------|
| **Public** | Follow / Unfollow |
| **Private** | Request to Follow / Requested (disabled) / Cancel Request / Unfollow |

- Public follow: insert into `follows`.
- Private request: insert into `follow_requests` (`status='pending'`). Cancel: settle/delete the pending request.
- Unfollow: delete the `follows` edge.
- Unique constraint on `follows(follower_id, followee_id)` prevents duplicates.

## Private-account content gating

- Viewing rule for another user: `canView = !isPrivate || isFollowing`.
- If a private user is not followed by the viewer, their **spots array is empty** (the grid simply shows empty — no explicit lock screen). Enforced server-side by RLS (`can_view_author`), reflected client-side.
- Suspended authors (report-volume suspension) are hidden server-side regardless.

## Follow requests screen (private accounts)

- Lists incoming pending requests (page size **24**), pull-to-refresh.
- Per row: **Accept** / **Deny**.
  - Accept → create `follows` edge (requester→you or you→? per direction), settle the request, and send a **local "follow request accepted"** notification to the requester's device path (see [14-notifications.md](14-notifications.md)).
  - Deny → set request `rejected`/`cancelled`.
- Empty: "No pending requests."
- **Badge**: on your own private profile, poll pending count every **8 seconds** to update the menu badge.

## Likes & bookmarks grids

- **Likes grid**: spots the user liked (`spot_likes`).
- **Bookmarks grid**: saved spots (`spot_bookmarks`). Free users get a flat grid; **Pro** users get **collections**.
- Distinct empty-state copy per context.

## Collections (Pro)

- `bookmark_collections` (named folders, sort order) + `bookmark_collection_spots` (spot membership, sort order).
- Pro users can create collections, add/remove spots, reorder.
- When a Pro user bookmarks a spot, a **collection picker** sheet can appear to file it.
- Non-Pro: no collections; bookmarks are a flat list capped at 50 (then paywall).

## Blocks & reports (see [13-moderation-safety.md](13-moderation-safety.md))

- **Block** (profile or spot menu) → `block_user_v1`; refresh the local blocked set; instantly remove that author's spots from the feed. Blocked users no longer appear in discovery (server `can_view_author`).
- **Report user** → profile report sheet → `submit_content_report(target_type='profile', ...)`.
- **Report spot** → spot report sheet → `submit_content_report(target_type='spot', ...)`.
- Report sheets include an optional "also block this user" toggle (`p_block_requested`).

## Delete own spot

- From own-profile grid, confirm dialog → delete (optimistic).

## States

- **Loading**: header + grid skeletons.
- **Empty**: no spots / no likes / no bookmarks / no requests → context copy.
- **Error**: load failure → retry.
- **Private not-following**: empty grid.

## Android implementation notes

- Reuse the map component for the profile Map tab.
- Keep the current user's liked/bookmarked/blocked sets in a shared session holder so profile, feed, and map stay consistent.
- Poll follow-request badge with a lifecycle-aware coroutine (only while the owner's profile/menu is visible) at 8s.
