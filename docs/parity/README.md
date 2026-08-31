# Spot Android — iOS parity docs

**Goal:** ship an Android app that is feature‑for‑feature equivalent to the
iOS Spot app, sharing the same Supabase backend (`aeurigbbohyxvtsfiyul`).
No schema fork. No parallel data plane. Same limits, same copy, same states.

These docs replace the previous `PRD/`. They are written to be consumed by
**Cursor Cloud Agents**: each task file is self-contained and one task = one PR.

## The four things every agent must read first

1. [`constraints.md`](constraints.md) — non‑negotiable rules (RLS, moderation,
   layering, safety, secrets). Break these and the PR is rejected.
2. [`limits-constants.md`](limits-constants.md) — every numeric limit that must
   match iOS (feed page 24, map cap 250, OTP 6/30s, publish 90s, image 1600px…).
3. [`data-contracts.md`](data-contracts.md) — RPCs, tables, storage buckets,
   edge functions, DTOs. This is the wire contract with Supabase.
4. [`design-tokens.md`](design-tokens.md) — colors, typography, spacing, radius,
   copy. No inline hex in feature code.

## The two things an agent picks up work from

1. [`agent-brief.md`](agent-brief.md) — the drop-in prompt to launch a Cloud
   Agent. Explains how to work, definition of done, PR format.
2. [`task-queue.md`](task-queue.md) — the ordered list of remaining parity
   tasks. Each links to a file in [`tasks/`](tasks/) with a full spec.

## Reference (for humans + agents that need to peek at iOS)

- [`screen-map.md`](screen-map.md) — iOS surface ↔ Android surface mapping,
  including test-tag vocabulary. Useful when a task says "port
  `PostFlowView.swift`" and you want to know the Android target.
- iOS source of truth (sibling checkout, if available):
  `../spot-ios-app/Spot/` — Views, ViewModels, Services, Managers, Supabase.
  If you don't have this checked out, the task file inlines the contract.

## How the doc set is structured

```
docs/parity/
├── README.md              ← you are here
├── constraints.md         ← non-negotiable rules
├── limits-constants.md    ← every numeric constant, sourced
├── data-contracts.md      ← RPCs, tables, storage, edge functions
├── design-tokens.md       ← colors, typography, spacing, copy
├── screen-map.md          ← iOS ↔ Android surface map
├── agent-brief.md         ← Cloud Agent prompt template
├── task-queue.md          ← ordered list of open parity tasks
└── tasks/
    ├── 01-edit-spot-sheet.md
    ├── 02-collection-picker-search-profile.md
    ├── 03-fcm-follow-request-push.md
    ├── 04-verified-app-links.md
    ├── 05-algorithm-snapshot-repository.md
    ├── 06-firebase-config.md
    ├── 07-missing-repository-fakes.md
    ├── 08-settings-to-collections-nav.md
    ├── 09-platform-field-reports-terms.md
    ├── 10-post-drafts-migrate-to-room.md
    └── 11-parity-audit-checklist.md
```

## What's already done

- All 5 tabs, launch gates, auth (email + Google OAuth + OTP), post composer
  (3 steps + drafts), map (Google Maps + pins + drawer + filters), search
  (segments + history + grids), profile (header + tabs + follow flows),
  collections (list + detail + picker), settings, paywall + billing +
  Pro success tour, safety (report + block + share + delete overflow),
  permissions, first-run coach, deep link overlays.

- Repository layering, Hilt DI, session holder, signed-URL Coil fetcher,
  structured logger, analytics scaffolding, ~50 unit tests + ~13 instrumented tests.

## What's left (see [`task-queue.md`](task-queue.md))

**P1 — user-visible feature parity:**

- Task 12 — Map **photo-preview pins** (iOS PR [#92](https://github.com/Ewynman/spot-ios-app/pull/92))
- Task 13 — Home **place-first Spot card with map flip** (iOS PR [#90](https://github.com/Ewynman/spot-ios-app/pull/90))
- Task 14 — Map **compact preview + clustering** audit (iOS PR [#88](https://github.com/Ewynman/spot-ios-app/pull/88))
- Task 15 — **Pro vibe-to-photo mapping** (`PHOTO_SYNCED`) (iOS PR [#89](https://github.com/Ewynman/spot-ios-app/pull/89))
- Task 01 — Pro **Edit spot** sheet (iOS PR [#83](https://github.com/Ewynman/spot-ios-app/pull/83))
- Task 02 — **Collection picker** on Search/Profile bookmarks
- Task 27 — **Auth session preservation** across reinstalls (iOS PR [#53](https://github.com/Ewynman/spot-ios-app/pull/53))
- Task 03 — **FCM** remote push for follow requests
- Task 04 — **Verified App Links** (`assetlinks.json`)

**P2 — polish, bug fixes, correctness:** tasks 05, 06, 07, 08, 09, 16, 17, 18, 19, 20, 21, 22, 24, 26.

**P3 — cleanup, dev quality:** tasks 10, 23, 25, 28, 29, 30, 31, plus the recurring **11 full parity audit** at the end.

Total: **31 parity tasks**, sized for one Cloud Agent per task = one PR.

## Golden constraint

> **RLS is authoritative. Anon key only. iOS is the reference when contracts
> are ambiguous. When in doubt, read the actual iOS Swift file — never
> guess.** — from [`constraints.md`](constraints.md)
