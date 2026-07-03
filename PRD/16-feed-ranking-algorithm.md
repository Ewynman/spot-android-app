# 16 — Feed Ranking Algorithm

Ranking is **server-authoritative** (Postgres RPCs + affinity tables). The Android client does not need to reimplement scoring — it calls the RPCs and renders. This doc explains the moving parts so the client sends the right signals and displays results correctly.

## Components

| Piece | Where | Role |
|-------|-------|------|
| `get_home_feed_v1` | RPC | Produces the ranked candidate set + score/rank/source per row |
| `get_home_feed_status_v1` | RPC | Explains empty states (`has_unseen`/`caught_up`/`no_eligible_spots`/`no_spots_global`) |
| `feed_impressions` | table (PK user_id,spot_id) | Durable dedupe across sessions; tracks seen source/rank/score/batch |
| `user_feed_events` | table | Raw behavioral signals (insert-only by owner) |
| `user_vibe_affinities` | table | Learned per-vibe preference (score, +/- events) |
| `user_creator_affinities` | table | Learned per-creator preference |
| `user_feed_profiles` | table | Compact computed profile (jsonb) used for ranking + on-device diversity |
| `record_feed_event_v1` | RPC | Logs an event and updates affinities |
| `recompute_my_feed_profile_v1` | RPC | Recomputes the caller's profile |
| `feed_event_weight_v1` | RPC (internal) | Maps event type → strength |

## How a feed request works

1. Client calls `get_home_feed_v1(p_limit=24, p_viewer_lat?, p_viewer_lng?, p_batch_id=<new uuid>, p_force_seen_fallback=false)`.
2. Server builds a candidate set (following, vibe/creator affinity, recency, distance), excludes blocked/suspended/hidden/self as needed, ranks, and returns rows with `source_bucket`, `rank_position`, `ranking_score`, `seen_before`, `last_seen_at`.
3. Server records impressions in `feed_impressions` (dedupe source of truth).
4. If the personalized unseen set is exhausted, the client re-requests with `p_force_seen_fallback=true` to resurface already-seen spots (avoids dead-ends when caught up).

## Signals the client must emit

Send via `record_feed_event_v1(p_spot_id, p_event_type, p_dwell_ms?, p_metadata?)`. Event types (from the `user_feed_events.event_type` check):

Positive/engagement: `impression`, `visible_2s`, `long_dwell`, `detail_open`, `like`, `save`, `share`, `profile_tap`, `vibe_tap`, `map_pin_tap`, `follow_author`.
Negative/neutral: `quick_skip`, `unlike`, `unsave`, `hide`, `report`, `block_author`, `unfollow_author`.

The server assigns `event_strength` (via `feed_event_weight_v1`) and updates vibe/creator affinities. Emit these consistently so both platforms feed the same learning signal.

Guidance:
- `impression` when a card first becomes visible; `visible_2s` after ~2s; `long_dwell` for extended viewing; `quick_skip` when scrolled past quickly. Throttle/coalesce to avoid spamming.
- Always emit `like/unlike/save/unsave/share/detail_open/profile_tap/vibe_tap/follow_author/unfollow_author/hide/report/block_author` when those actions happen.

## `user_feed_profiles.profile` jsonb structure

Read on the algorithm-debug screen and for on-device diversity. Shape (mirror `FeedProfile`):

```
{
  "version": 1,
  "computed_at": "<iso>",
  "stats": {
    "likes_count", "saves_count", "spots_count", "blocks_count", "hidden_count",
    "follows_count", "followers_count", "distinct_vibes_engaged", "distinct_creators_engaged"  // all ints
  },
  "top_vibes": [ { "vibe_tag_id?", "name", "score", "positive_events", "negative_events", "total_events", "last_event_at?" } ],
  "top_creators": [ { "creator_id", "username?", "profile_image_url?", "is_private", "is_pro", "score", "positive_events", "negative_events", "total_events", "last_event_at?" } ],
  "ranker_constants": {
    "affinity_sigmoid_k?", "affinity_clamp": [], "weights_personalized": {},
    "weights_seen_fallback": {}, "freshness_half_life_hours?", "distance_full_score_meters?"
  },
  "event_summary_30d": { "window_days", "total", "positive_total_strength", "negative_total_strength",
                         "by_type": [ { "event_type", "n", "total_strength" } ] },
  "event_summary_90d": { ... same shape, window 90 }
}
```

## On-device diversity reordering

After hydrating the first home page, the client lightly reorders using `top_vibes` signal strength so a single dominant vibe doesn't fill the whole visible window when other tags exist on the page (mirror `FeedDiversity`). This is cosmetic reordering of an already-ranked page, not a re-scoring.

## Dedupe & maintenance

- `feed_impressions` is the durable seen-set (PK `(user_id, spot_id)`), so the client doesn't need cross-session tracking.
- Internal maintenance RPCs (cron/service-role, not client): `prune_feed_impressions_v1(max_per_user=5000, ttl_days=90)`, `archive_user_feed_events_v1(ttl_days=180)`, `recompute_stale_feed_profiles_v1(max_users=1000, stale_after='03:00:00')`.

## Client rules

- Pass viewer lat/lng when location permission is granted (improves distance ranking); omit otherwise.
- Use a **fresh `batch_id`** per fetch/refresh.
- Never treat the on-device ranker as the source of truth for the shipped feed — the RPC is authoritative.
- Respect server exclusions (blocked/suspended/hidden) even though RLS already enforces them.
