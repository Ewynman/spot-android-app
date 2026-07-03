# Spot — Android PRD

This folder is the **complete Product Requirements Document** for building the **Spot** app on Android. It is derived directly from the shipped iOS app (SwiftUI + Supabase) and the live Supabase schema (project ref `aeurigbbohyxvtsfiyul`). It is intended to be accurate enough to rebuild the app feature-for-feature and share the same backend.

## What Spot is

Spot is a **social place-discovery** app. A **Spot** is a saved place recommendation from a real person, centered on **vibe** (how a place feels) rather than rigid categories. The app emphasizes a premium, minimal UI, vibe-tag discovery, a map, posting with photo moderation, social profiles/follows, a **Pro** subscription, and strong user-safety guarantees.

## Golden rules for the Android build

1. **Same backend, same contracts.** Android talks to the **same Supabase project** as iOS. Do not fork the schema. All reads/writes go through the RPCs and tables documented here, and **Row Level Security (RLS) is authoritative** — never rely on client-only authorization.
2. **Supabase is the only data plane** for users, spots, media, and social data. Firebase is used only for Analytics/Crashlytics/App Check (optional on Android; can be swapped for equivalents). Do not introduce a second data plane.
3. **Every uploaded image must pass moderation** (Azure Content Safety via the `moderate-image` edge function) before it becomes an approved, publicly-referenced asset.
4. **Safety features are non-negotiable**: report content, report/block users, terms acceptance gating, and content filtering must all ship.
5. **Preserve limits and gating exactly** (free vs Pro image/vibe counts, bookmark cap, etc.) so both platforms behave identically against shared data.

## Reading order

| # | File | Contents |
|---|------|----------|
| — | [00-overview.md](00-overview.md) | Vision, personas, principles, terminology, surface map |
| — | [01-architecture-android.md](01-architecture-android.md) | Recommended Android stack, module layout, state management, platform mappings |
| — | [02-design-system.md](02-design-system.md) | Colors, typography, spacing/radius tokens, vibe-tag catalog, component inventory |
| — | [03-data-model.md](03-data-model.md) | **Full Supabase schema** — every table, column, type, constraint, FK, RLS behavior |
| — | [04-backend-api.md](04-backend-api.md) | RPC catalog (full signatures), storage buckets, edge function, auth, signed URLs |
| — | [05-auth-onboarding.md](05-auth-onboarding.md) | Welcome, sign up/in, email OTP, Apple/Google, terms gate, first-run tour, permissions |
| — | [06-home-feed.md](06-home-feed.md) | Feed screen, spot card, actions, pagination, states, impressions |
| — | [07-map.md](07-map.md) | Map discovery, pins, drawer, Pro filters, location |
| — | [08-post-flow.md](08-post-flow.md) | Multi-step composer, media, place, vibes, drafts, publish + moderation pipeline |
| — | [09-search.md](09-search.md) | Users/locations/vibes search, history, grids, Pro filters |
| — | [10-profile-social.md](10-profile-social.md) | Profile, follow/requests, private accounts, blocks, likes/bookmarks, collections |
| — | [11-settings.md](11-settings.md) | All settings rows, account deletion, blocked users, legal, debug |
| — | [12-pro-subscription.md](12-pro-subscription.md) | Pro entitlements, gated features, StoreKit→Google Play Billing mapping |
| — | [13-moderation-safety.md](13-moderation-safety.md) | Image + text moderation, reports, blocks, suspensions, terms |
| — | [14-notifications.md](14-notifications.md) | Local + (future) push notifications for social events |
| — | [15-deep-links.md](15-deep-links.md) | App Links / custom scheme routing, pending links, subscription return |
| — | [16-feed-ranking-algorithm.md](16-feed-ranking-algorithm.md) | Server ranking, affinities, feed events, diversity, dedupe |
| — | [17-non-functional-testing.md](17-non-functional-testing.md) | Performance, logging, analytics, testing, release |

## Source of truth

- Live Supabase project: **`aeurigbbohyxvtsfiyul`** (region us-west-2, Postgres 17), URL `https://aeurigbbohyxvtsfiyul.supabase.co`.
- iOS reference implementation: `../spot-ios-app/Spot/` and `../spot-ios-app/docs/`.
- Schema in this PRD was read from the live database on 2026-07-03; cross-check `../spot-ios-app/supabase/migrations/` for policy details before changing anything.

> Anything marked **[ANDROID DECISION]** is a place where the iOS behavior needs a deliberate platform adaptation (e.g. Sign in with Apple, StoreKit, APNs). Defaults are recommended inline.
