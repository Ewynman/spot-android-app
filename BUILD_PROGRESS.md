# Spot Android — Build Progress

This file tracks implementation progress according to `PRD/18-build-order.md`.

Legend: ✅ Complete | 🚧 In Progress | ⏳ Blocked | ⬜ Not Started

---

## Phase 0 — Repo Bootstrap

| Task | Status | PR | Notes |
|------|--------|----|----|
| **0.1** Project scaffold | ✅ | [#1](https://github.com/Ewynman/spot-android-app/pull/1) | Gradle, packages, CI, README |

**Phase 0 Complete** ✅

---

## Phase 1 — Foundation

These can run in parallel after Phase 0.

| Task | Status | PR | Dependencies | Notes |
|------|--------|----|--------------|----|
| **1.1** Design system / theme | ✅ | [#4](https://github.com/Ewynman/spot-android-app/pull/4) | 0.1 | Colors, spacing, typography, vibe chips, preview screen |
| **1.2** Supabase core | ✅ | [#2](https://github.com/Ewynman/spot-android-app/pull/2) | 0.1 | Client provider, session bridge |
| **1.3** Domain models + DTOs | ✅ | [#3](https://github.com/Ewynman/spot-android-app/pull/3) | 0.1 | Spot, User, VibeTag, enums, Constants |
| **1.4** Signed-URL image loader | ✅ | [#5](https://github.com/Ewynman/spot-android-app/pull/5) | 1.2 | Coil fetcher with auth + caching |
| **1.5** Shared components | ✅ | [#6](https://github.com/Ewynman/spot-android-app/pull/6) | 1.1, 1.4 | SpotCard, vibe chips, avatar, empty views |
| **1.6** Navigation shell + overlay | ✅ | [#7](https://github.com/Ewynman/spot-android-app/pull/7) | 1.1 | 5-tab bar, NavGraph, overlay host |
| **1.7** Structured logger + analytics | ✅ | [#8](https://github.com/Ewynman/spot-android-app/pull/8) | 0.1 | SpotLogger, DataStore toggles, Firebase analytics |

**Phase 1 Complete** ✅

**Next recommended**: 2.3 (Welcome / sign-up / login / OTP)

---

## Phase 2 — Auth, Session & Safety Spine

Mostly sequential; gates the app.

| Task | Status | PR | Dependencies | Notes |
|------|--------|----|--------------|----|
| **2.1** Auth repository + session VM | ✅ | [#9](https://github.com/Ewynman/spot-android-app/pull/9) | 1.2, 1.3 | AuthRepository, UserSessionHolder, AuthViewModel |
| **2.2** Launch gate + splash | ✅ | [#11](https://github.com/Ewynman/spot-android-app/pull/11) | 2.1, 1.6 | Decision table routing |
| **2.3** Welcome / sign-up / login / OTP | ⏳ | — | 2.1 | Auth screens + terms checkbox |
| **2.4** Post-auth username + terms gate | ⏳ | — | 2.1 | record_terms_acceptance_v1 |
| **2.5** Permissions framework | ⏳ | — | 1.5 | Pre-prompt + OS dialog |
| **2.6** Safety flows (report + block) | ⏳ | — | 2.1, 1.5 | Report sheet, block dialog |

---

## Phase 3 — Core Content Surfaces

Parallel after Phase 2.

| Task | Status | PR | Dependencies | Notes |
|------|--------|----|--------------|----|
| **3.1** Home feed | ⏳ | — | 2.x, 1.5 | get_home_feed_v1, pagination, like/bookmark |
| **3.2** Post flow | ⏳ | — | 2.x, 2.5 | 3-step composer, moderation pipeline |
| **3.3** Map | ⏳ | — | 2.x, 1.5 | Google Maps, viewport loading, drawer |
| **3.4** Search | ⏳ | — | 2.x, 1.5 | 3 segments, debounce, history |
| **3.5** Profile & social | ⏳ | — | 2.x, 3.3 | Profile tabs, follow, privacy |
| **3.6** Feed-event service | ⏳ | — | 3.1 | Coalescing emitter |

---

## Phase 4 — Monetization, Settings, Links, Notifications

Parallel after Phase 3.

| Task | Status | PR | Dependencies | Notes |
|------|--------|----|--------------|----|
| **4.1** Pro / billing | ⏳ | — | 3.x | Play Billing v6+, paywall, gates |
| **4.2** Collections (Pro) | ⏳ | — | 4.1, 3.5 | Bookmark collections CRUD |
| **4.3** Settings | ⏳ | — | 2.x, 4.1 | Account, security, subscription |
| **4.4** Deep links | ⏳ | — | 1.6, 3.1 | App Links, spot detail overlays |
| **4.5** Notifications | ⏳ | — | 2.5, 3.5 | Local notifications, channels |

---

## Phase 5 — Hardening & Release

| Task | Status | PR | Dependencies | Notes |
|------|--------|----|--------------|----|
| **5.1** Test pass | ⬜ | — | All | Unit + instrumented coverage |
| **5.2** Non-functional | ⬜ | — | All | Performance, offline, accessibility |
| **5.3** Release checklist | ⬜ | — | All | App Links live, deep links tested |

---

## Summary

- **Total tasks**: 28
- **Completed**: 10 (36%)
- **In Progress**: 0
- **Blocked (waiting on deps)**: 4
- **Not Started**: 14

**Current Phase**: Phase 2 (Auth, Session & Safety Spine)  
**Next Task**: 2.3 (Welcome / sign-up / login / OTP)

---

## Quick Reference

To build the next task, check the **Next recommended** line above or look for:
1. ⬜ Not Started tasks with all dependencies ✅ Complete
2. Tasks in the earliest phase with satisfied dependencies
3. Tasks that unblock the most other tasks

Update this file after each PR is merged!
