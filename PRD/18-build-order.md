# 18 — Build Order & Agent Task Graph

This is the **execution layer** for the PRD. Docs `00–17` describe *what* to build; this doc
describes *in what order*, *what each autonomous agent owns*, and *when a task is done*. It exists
because the feature docs (06–16) all assume a shared foundation already exists — so the foundation
must land first, before feature agents run in parallel.

**How to use it:** run phases in order. Within a phase, each task is sized for one AI agent and can
run in parallel with its siblings unless a dependency is listed. Every task's "done" bar is the
Definition of Done in `.cursor/rules/project.mdc` **plus** the acceptance criteria below.

Legend: `[dep: …]` = must be merged first. `→ PRD/NN` = the authoritative spec for the task.

---

## Phase 0 — Repo bootstrap (1 agent, blocks everything)

- **0.1 Project scaffold** → PRD/01
  - Android Studio project, Kotlin, Compose, Material 3, min SDK 26 / target latest.
  - Gradle version catalog with every dependency in the stack table (supabase-kt, Hilt, Coil,
    maps-compose, play-billing, FCM, DataStore, coroutines).
  - Package skeleton exactly as `PRD/01` (`core/`, `data/`, `feature/`, `navigation/`, `di/`).
  - `local.properties`/`BuildConfig` for Supabase URL + anon key + share base URL + product ids.
  - CI: a workflow that runs `./gradlew assembleDebug lintDebug testDebugUnitTest`.
  - **Done:** empty app compiles and launches to a blank scaffold; CI green.

## Phase 1 — Foundation (parallel after 0; every feature depends on these)

- **1.1 Design system / theme** `[dep: 0.1]` → PRD/02
  - Color tokens, spacing/radius, typography roles, vibe-chip style, uppercase "SPOT" wordmark.
  - **Done:** a preview screen renders all tokens; no hex literals leak into feature code later.
- **1.2 Supabase core** `[dep: 0.1]` → PRD/01, PRD/04
  - `SupabaseClientProvider` (gotrue/postgrest/storage/functions/realtime), encrypted session
    storage, a synchronous **session bridge** exposing current user id.
  - **Done:** app can anon-call a harmless RPC; session persists across restart.
- **1.3 Domain models + DTOs** `[dep: 0.1]` → PRD/03, PRD/04
  - All DTOs (snake_case) + domain models (`Spot`, `User`, `VibeTag`, …) + enum constants
    (report reasons, statuses, event types) + shared numeric `Constants`.
  - **Done:** DTOs decode sample RPC JSON in a unit test.
- **1.4 Signed-URL image loader** `[dep: 1.2]` → PRD/04, PRD/17
  - Coil auth-aware fetcher: sign `spots`/`pending_images`, public `avatars`, cache until near
    7-day expiry.
  - **Done:** unit test proves caching + re-sign near expiry; renders a signed image.
- **1.5 Shared components** `[dep: 1.1, 1.4]` → PRD/02, PRD/06
  - `SpotCard`, `SkeletonSpotCard`, `EmptyFeedView`, vibe chip/row, avatar (Pro ring/badge),
    toast/banner, permission pre-prompt, top nav. Use iOS test tags.
  - **Done:** Compose previews for each; `SpotCard` handles single + multi-image aspect-aware.
- **1.6 Navigation shell + overlay host** `[dep: 1.1]` → PRD/00, PRD/15
  - Custom 5-tab bottom bar (reselect event), NavGraph, top-level overlay host (deep-link detail,
    paywall, subscription success).
  - **Done:** tabs switch; reselect fires event; an overlay can be shown over any tab.
- **1.7 Structured logger + analytics wrapper** `[dep: 0.1]` → PRD/17, PRD/11
  - Per-area categories, debug toggles via DataStore, analytics event names from PRD/17. No PII.
  - **Done:** categories toggle at runtime; release build defaults off.

## Phase 2 — Auth, session & safety spine (mostly sequential; gates the app)

- **2.1 Auth repository + session view model** `[dep: 1.2, 1.3]` → PRD/05, PRD/04
  - Email/password + OTP, login-by-username (`resolve_login_email`), Google OAuth, password reset,
    `sync_current_user_v1` on every start/login, publishes the session state fields in PRD/05.
  - **Done:** unit tests for gating states with a fake auth service.
- **2.2 Launch gate + splash** `[dep: 2.1, 1.6]` → PRD/05
  - Ordered launch decision table (loading → OTP → welcome → username gate → terms gate → shell).
  - **Done:** each branch routes correctly in tests.
- **2.3 Welcome / sign-up / login / OTP screens** `[dep: 2.1]` → PRD/05
  - Includes the **terms agreement checkbox gate** (store requirement) + local pre-auth store.
  - **Done:** UI test launch→welcome→auth gate; terms unchecked disables actions.
- **2.4 Post-auth username gate + terms update gate** `[dep: 2.1]` → PRD/05, PRD/03
  - `record_terms_acceptance_v1`, `has_accepted_active_terms`, fail-open on network error.
- **2.5 Permissions framework** `[dep: 1.5]` → PRD/05
  - Neutral pre-prompt → OS dialog; state keys; never blocking; per-type (location/camera/photos/
    notifications).
- **2.6 Safety flows (report + block)** `[dep: 2.1, 1.5]` → PRD/13, PRD/10
  - Report sheet (reason list, details, "also block"), block dialog, `submit_content_report` +
    `block_user_v1`, instant local removal signal. **These are ship-blockers.**
  - **Done:** report + block reachable from spot and profile menus; unit tests for both.

## Phase 3 — Core content surfaces (parallel after Phase 2)

- **3.1 Home feed** `[dep: 2.x, 1.5]` → PRD/06, PRD/16
  - `get_home_feed_v1` + status RPC, 24 paging, pull-to-refresh, seen-fallback, empty-state copy,
    optimistic like/bookmark (free cap 50 → paywall), overflow menu, feed-event emission, diversity
    reorder, post-success insert.
- **3.2 Post flow** `[dep: 2.x, 2.5]` → PRD/08, PRD/13
  - 3-step composer, image downscale, media_assets insert → upload → `moderate-image` →
    `publish_spot_with_approved_media_assets_v1`, background queue + 90s timeout + progress banner,
    local drafts, posting-rules sheet, free/Pro limits, edit/delete.
- **3.3 Map** `[dep: 2.x, 1.5]` → PRD/07
  - Google Maps Compose, viewport-debounced `get_map_spots_v1`, branded pins, user-location avatar
    marker, spot drawer with full dismiss policy, location fallback. `ProfileMapView` variant.
- **3.4 Search** `[dep: 2.x, 1.5]` → PRD/09
  - 3 segments, 0.3s debounce, local history (max 20/segment), user/location/vibe grids with the
    24-target / 5-attempt fill semantics.
- **3.5 Profile & social** `[dep: 2.x, 3.3 for map tab]` → PRD/10
  - Profile header/tabs, follow/unfollow, private-account gating, follow-requests screen + 8s badge
    poll, likes/bookmarks grids, delete own spot.
- **3.6 Feed-event service** `[dep: 3.1]` → PRD/16, PRD/06
  - Coalescing emitter for all event types (impression/visible_2s/long_dwell/quick_skip/etc.).

## Phase 4 — Monetization, settings, links, notifications (parallel after Phase 3)

- **4.1 Pro / billing** `[dep: 3.x]` → PRD/12
  - Play Billing v6+, `obfuscatedAccountId` = user UUID, server-verified entitlement write to
    `is_pro`/`pro_until`, restore, account-binding denial, paywall, post-purchase onboarding,
    success screen. Enforce the Pro-gate matrix everywhere.
- **4.2 Collections (Pro)** `[dep: 4.1, 3.5]` → PRD/10, PRD/12
  - `bookmark_collections` CRUD + collection picker on bookmark.
- **4.3 Settings** `[dep: 2.x, 4.1]` → PRD/11
  - Account (edit profile, change password, **delete account with re-auth** → `delete_my_account`),
    security (private toggle, blocked users/unblock), subscription, permissions, legal, debug.
- **4.4 Deep links** `[dep: 1.6, 3.1]` → PRD/15
  - App Links (`assetlinks.json` + autoVerify) + custom scheme, id validation, pending-link storage
    when logged out, spot detail/unavailable overlays, subscription return.
- **4.5 Notifications** `[dep: 2.5, 3.5]` → PRD/14
  - Local follow-request-accepted notification, channels, action buttons routing to follow-requests.
    (Remote FCM push for "request received" is explicitly out of v1 scope.)

## Phase 5 — Hardening & release (1–2 agents)

- **5.1 Test pass** → PRD/17: unit + instrumented coverage for the parity-critical scenarios table.
- **5.2 Non-functional** → PRD/17: perf (60fps feed, map caps, cache-on-memory-pressure), offline
  resilience, accessibility (font scaling, content descriptions, contrast).
- **5.3 Release checklist** → PRD/17: assetlinks live, Play subscription + server verify, legal URLs
  match listing, delete-account reachable, all 5 UGC guarantees demonstrable, anon-key-only, deep
  link smoke test on device, crash/analytics wired.

---

## Dependency summary (critical path)

```
0.1 ─┬─ 1.1 ─┬─ 1.5 ─┐
     ├─ 1.2 ─┼─ 1.4 ─┤
     ├─ 1.3 ─┘       ├─ Phase 2 (2.1→2.2→2.3/2.4, 2.5, 2.6)
     ├─ 1.6 ─────────┤        │
     └─ 1.7           │        └─ Phase 3 (3.1–3.6) ─ Phase 4 (4.1–4.5) ─ Phase 5
```

## Parallelization guidance for autonomous agents

- **Never** run Phase 1 tasks before 0.1 merges — they all import the scaffold.
- Foundation (1.1–1.7) can be up to 7 parallel agents; they only share the scaffold.
- Phase 3 surfaces are independent once Phase 2 + shared components exist — 6 parallel agents.
- Assign **one owner** to `SpotCard`, the theme, the session holder, and the Supabase client. Every
  other agent consumes these; they must not fork them. If a feature needs a change to a shared
  component, the feature agent proposes it and the owner integrates it.
- Each agent works on its own branch, keeps its PR scoped to one task id, and must leave
  `assembleDebug` + unit tests green.

## What is intentionally out of scope for v1 parity

Comments on spots; like/comment push; server "follow request received" push; profile deep links
(`/u/{username}`); dark mode. (See PRD/00 non-goals.)
