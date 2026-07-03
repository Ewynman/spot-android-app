# 00 — Product Overview

## Vision

Spot helps people **discover places through other people**. Instead of star ratings and rigid categories, Spot organizes recommendations around **vibe** — how a place feels and what it's good for ("Hidden Gem", "Scenic View", "Study Spot"). Every piece of content is a **Spot**: a photo-first recommendation tied to a place and a set of vibe tags, shared by a real user.

## Core value

- **Discover** places through people you follow and broader discovery (home feed + map).
- **Save, like, and revisit** recommendations quickly.
- **Explore geographically** on a map with a bottom **spot drawer** for previews.
- **Share** your own Spots with photos, a location, and vibe tags.

## Product principles

1. **Trust & safety first.** Authentication, RLS, image moderation, text filtering, reporting, and blocking are non-negotiable and ship in v1.
2. **Premium, minimal UI.** Calm cream/green palette, generous spacing, few accent colors. See [02-design-system.md](02-design-system.md).
3. **Vibe-centered discovery.** Tags drive feed, map, and search discovery.
4. **Social proof.** Creators, follows, likes, bookmarks.
5. **Low friction, high safety.** Fast browse and post, but never bypass safety checks.

## Target users

- **Explorers**: browse the feed/map to find places with a certain vibe near them or in a city they're visiting.
- **Curators / creators**: post Spots, build a following, and (with Pro) organize bookmarks into collections and post richer multi-photo Spots.
- **Followers**: follow friends and tastemakers; private accounts gate their content behind follow approval.

## Major surfaces (bottom tab shell)

The app is a 5-tab shell. Tab order and default selection matter for parity.

| Index | Tab | Purpose | Detail file |
|-------|-----|---------|-------------|
| 0 | **Home** | Personalized ranked feed of Spots | [06-home-feed.md](06-home-feed.md) |
| 1 | **Map** | Viewport-based discovery with pins + drawer | [07-map.md](07-map.md) |
| 2 | **Post** | Multi-step composer to create a Spot | [08-post-flow.md](08-post-flow.md) |
| 3 | **Search** | Search users / locations / vibes | [09-search.md](09-search.md) |
| 4 | **Profile** | Own profile, social graph, settings, Pro entry | [10-profile-social.md](10-profile-social.md) |

Default selected tab is **Home (0)**. The tab bar is a **custom bottom bar** (not the system default), so reselecting the already-active tab fires a "reselect" event (used to scroll-to-top / dismiss map drawer / reset search).

Overlays that float above the tab shell (see [15-deep-links.md](15-deep-links.md), [12-pro-subscription.md](12-pro-subscription.md)):
- Deep-linked Spot detail (full-screen), spot loading, spot unavailable.
- Subscription success (`ProSuccessView`), paywall sheet, post-purchase Pro onboarding.

## App lifecycle (launch → usable)

```
Launch splash (≈1.5s)
   → session restore
      → not authenticated ............ Welcome screen
      → awaiting email verification ... Confirm-email (OTP) screen
      → authenticated + Apple/Google user missing username ... Post-auth setup (username)
      → authenticated + needs terms re-acceptance ............ Terms update gate (blocking)
      → authenticated + verified ............................. Main tab shell
         → first session after signup → first-run product tour overlay
         → after tour → notification permission prompt
```

**Permissions never block** account creation, onboarding, or entering the main app. Location/camera/photos/notifications are all requested **contextually** with a neutral pre-prompt before the OS dialog.

## Terminology (shared vocabulary)

| Term | Definition |
|------|------------|
| **Spot** | A place recommendation post: photos + place + vibe tags + creator. Primary content unit (table `spots`). |
| **Vibe tag** | Discovery label describing how a place feels (table `vibe_tags`; 20 canonical rows). |
| **Creator** | The user who published a Spot (`spots.user_id`). |
| **Feed** | Home ranked list of Spots from RPC `get_home_feed_v1`. |
| **Map marker / pin** | Map annotation for a Spot. |
| **Spot drawer / bottom drawer** | Bottom sheet on the map showing the selected Spot preview. |
| **Draft** | In-progress Spot before publish (stored locally on device, not server). |
| **Pro** | Paid subscription tier. iOS product id `spotPro` (yearly). Android uses Google Play Billing. |
| **Bookmark / save** | User saves a Spot for later (table `spot_bookmarks`). Free cap = 50; Pro = unlimited. |
| **Like** | User likes a Spot (table `spot_likes`). |
| **Follow / follow request** | Social graph edge (`follows`) or pending request (`follow_requests`) for private accounts. |
| **Collection** | Pro-only bookmark folder (`bookmark_collections`). |
| **Deep link / App Link** | `https://spotapp.online/s/{spotId}` or `spotapp://...` opening a specific Spot. |
| **Moderation** | Automated image (Azure Content Safety) + text safety checks before content is public. |
| **RLS** | Row Level Security on Postgres/Storage; enforces per-user access server-side. |
| **Suspension (report-volume)** | Auto-hide of an author's public content when report thresholds are hit. |

## Non-goals / not implemented (as of source snapshot)

- **Comments** on Spots (schema references it as a future `target_type` but no feature).
- **Like/comment push notifications** (only follow-request/accept notifications exist; likes intentionally excluded).
- **Server push for "follow request received"** (client infra ready; backend trigger is a future enhancement — see [14-notifications.md](14-notifications.md)).
- **Profile deep links** (`/u/{username}`) — not implemented; only `/s/{spotId}`.

## Success criteria for the Android port

- Feature parity with iOS across all 10 areas in this PRD.
- Byte-compatible behavior against shared Supabase data (same RPCs, same limits, same RLS).
- Passes Google Play policy equivalents of the App Store UGC safety requirements: terms agreement before registration, content filtering, report content, report + block users, act on reports within 24h.
