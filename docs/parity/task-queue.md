# Parity task queue

One task = one Cloud Agent = one PR. Tasks in **P1** should ship before
tasks in **P2**, but tasks within a priority can run in parallel unless
noted.

To launch a task, follow [`agent-brief.md`](agent-brief.md).

**iOS PR column** links to the source of truth on GitHub for tasks
derived from recent iOS shipping work.

## Open

### P1 — user-visible feature parity (ship first)

| # | Task | Size | iOS PR | Deps |
|---|------|------|--------|------|
| 12 | [Map photo-preview pin markers](tasks/12-map-photo-pins.md) | Medium | [#92](https://github.com/Ewynman/spot-ios-app/pull/92) | — |
| 13 | [Home place-first Spot card + map flip](tasks/13-home-card-place-first-flip.md) | Medium | [#90](https://github.com/Ewynman/spot-ios-app/pull/90) | — |
| 14 | [Map compact preview + clustering audit](tasks/14-map-compact-preview-and-clustering.md) | Medium | [#88](https://github.com/Ewynman/spot-ios-app/pull/88) | — |
| 15 | [Pro vibe-to-photo mapping (`PHOTO_SYNCED`)](tasks/15-pro-vibe-to-photo-mapping.md) | Medium | [#89](https://github.com/Ewynman/spot-ios-app/pull/89) | — |
| 01 | [Pro Edit spot sheet](tasks/01-edit-spot-sheet.md) | Medium | [#83](https://github.com/Ewynman/spot-ios-app/pull/83) | — |
| 02 | [Collection picker on Search/Profile bookmarks](tasks/02-collection-picker-search-profile.md) | Small | [#84](https://github.com/Ewynman/spot-ios-app/pull/84) | — |
| 27 | [Auth session preservation across reinstalls](tasks/27-auth-session-preservation.md) | Medium | [#53](https://github.com/Ewynman/spot-ios-app/pull/53) | — |
| 03 | [FCM remote push for follow requests](tasks/03-fcm-follow-request-push.md) | Medium | — | 06 |
| 04 | [Verified App Links (`assetlinks.json`)](tasks/04-verified-app-links.md) | Small | — | — |

### P2 — polish, bug fixes, correctness

| # | Task | Size | iOS PR | Deps |
|---|------|------|--------|------|
| 16 | [Post + feed UI polish batch](tasks/16-post-and-feed-ui-polish.md) | Small | [#82](https://github.com/Ewynman/spot-ios-app/pull/82) | — |
| 17 | [Optimistic feed author + bucket signing](tasks/17-optimistic-feed-username-and-bucket-signing.md) | Small | [#75](https://github.com/Ewynman/spot-ios-app/pull/75) | — |
| 18 | [Feed / profile / post behavior fixes](tasks/18-feed-profile-post-behavior-fixes.md) | Medium | [#73](https://github.com/Ewynman/spot-ios-app/pull/73) | — |
| 19 | [Create-a-Spot photo workflow v2](tasks/19-photo-workflow-v2.md) | Medium | [#71](https://github.com/Ewynman/spot-ios-app/pull/71) | — |
| 20 | [Posting location picker redesign](tasks/20-posting-location-picker.md) | Small-Medium | [#70](https://github.com/Ewynman/spot-ios-app/pull/70) | — |
| 21 | [Map recenter + fresh permission checks](tasks/21-map-recenter-and-permission-freshness.md) | Small | [#69](https://github.com/Ewynman/spot-ios-app/pull/69), [#33](https://github.com/Ewynman/spot-ios-app/pull/33) | — |
| 22 | [Branded block-user confirmation](tasks/22-block-user-branded-confirmation.md) | Tiny | [#91](https://github.com/Ewynman/spot-ios-app/pull/91) | — |
| 24 | [Profile Spots empty states](tasks/24-profile-spots-empty-states.md) | Small | [#52](https://github.com/Ewynman/spot-ios-app/pull/52) | — |
| 26 | [OTP input: backspace + autofill](tasks/26-otp-input-backspace-autofill.md) | Small | [#50](https://github.com/Ewynman/spot-ios-app/pull/50) | — |
| 05 | [AlgorithmSnapshot VM → repository](tasks/05-algorithm-snapshot-repository.md) | Small | — | — |
| 06 | [Real Firebase config + App Check](tasks/06-firebase-config.md) | Small | — | — |
| 07 | [Missing repository fakes](tasks/07-missing-repository-fakes.md) | Small | — | — |
| 08 | [Settings → Collections navigation](tasks/08-settings-to-collections-nav.md) | Tiny | — | — |
| 09 | [`platform="android"` field on reports/terms](tasks/09-platform-field-reports-terms.md) | Tiny | — | Backend confirm |

### P3 — cleanup, dev quality, and hardening

| # | Task | Size | iOS PR | Deps |
|---|------|------|--------|------|
| 10 | [Post drafts → Room](tasks/10-post-drafts-migrate-to-room.md) | Medium | — | — |
| 23 | [Delete confirmation: remove scrim](tasks/23-delete-dialog-no-scrim.md) | Tiny | [#87](https://github.com/Ewynman/spot-ios-app/pull/87) | 18 (shared overlay) |
| 25 | [User location marker: initials + silhouette fallback](tasks/25-user-location-marker-fallback.md) | Tiny | [#51](https://github.com/Ewynman/spot-ios-app/pull/51) | — |
| 28 | [Staging internal-test OTP (Debug)](tasks/28-staging-internal-test-otp.md) | Small | [#76](https://github.com/Ewynman/spot-ios-app/pull/76) | — |
| 29 | [Debug: one-shot session reset](tasks/29-debug-session-reset.md) | Tiny | [#60](https://github.com/Ewynman/spot-ios-app/pull/60) | — |
| 30 | [Structured logging profiles (0–4) parity](tasks/30-logging-profiles-parity.md) | Small | [#64](https://github.com/Ewynman/spot-ios-app/pull/64) | — |
| 31 | [Private account test suite](tasks/31-private-account-test-suite.md) | Medium | [#38](https://github.com/Ewynman/spot-ios-app/pull/38), [#36](https://github.com/Ewynman/spot-ios-app/pull/36) | — |
| 11 | [Full parity audit checklist pass](tasks/11-parity-audit-checklist.md) | Recurring | — | 01–31 |

## Shipped

_None yet. When you ship a task, move its row here with the PR link._

Format:

```
| 12 | [Map photo pins](tasks/12-map-photo-pins.md) | 2026-08-31 | Android PR #123 |
```

## Task sizing

- **Tiny** — Under 1 h for a Cloud Agent. Config/wiring only.
- **Small** — 1–3 h. A single screen, a single wire, a fake or a repository.
- **Medium** — 3–8 h. A new screen with a new repository + fakes + tests, or
  a cross-cutting change with a UI surface.
- **Large** — 8 h+. Should be broken up. If you're writing a Large task,
  stop and split it.

## Priority definitions

- **P1** — Users can tell the app is not at iOS parity without it.
- **P2** — Polish, bug fixes, architectural correctness; foundation for
  P1 work.
- **P3** — Cleanup, refactors, dev quality; doesn't block release.

## How to pick the next task

**Recommended order (rough dependency + impact):**

Foundation first:
1. **06** Firebase config (unblocks 03 FCM).
2. **04** Verified App Links (release blocker for share-link parity).
3. **07** Missing fakes (unblocks P2 test coverage).

Then high-impact features in parallel (one agent each):

4. **12** Map photo pins
5. **13** Home place-first flip card
6. **14** Map compact preview audit
7. **15** Pro vibe-to-photo mapping
8. **01** Pro Edit spot sheet
9. **02** Collection picker on Search/Profile
10. **27** Auth session preservation
11. **19** Photo workflow v2 (do before 10 Room drafts)
12. **20** Location picker redesign
13. **03** FCM push (after 06)

Then P2 polish (many agents in parallel):

14. **16** Post/feed UI polish
15. **17** Optimistic feed author + bucket signing
16. **18** Feed/profile/post behavior fixes
17. **21** Map recenter + permission freshness
18. **22** Branded block confirmation
19. **24** Profile Spots empty states
20. **26** OTP UX
21. **05** AlgorithmSnapshot VM → repo
22. **08** Settings → Collections nav
23. **09** platform field (needs backend confirm)

Then P3 cleanup:

24. **10** Post drafts → Room (after 19)
25. **23** Delete scrim removal (after 18 extracts the overlay)
26. **25** User location marker fallback
27. **28** Staging test OTP
28. **29** Debug session reset
29. **30** Logging profiles
30. **31** Private account tests

Finally:

31. **11** Full parity audit checklist pass.

## Rules for adding a task

1. Copy the template from any existing task file. Preserve the sections:
   Goal, Why it matters (optional), Contract, iOS reference, Android
   target, Acceptance criteria, Test plan, Out of scope, Follow-ups.
2. Number it sequentially; keep numbering stable even after shipping.
3. Add a row in this file with an honest size estimate.
4. Every task must be executable by a Cloud Agent **without** access to
   the iOS repo. Inline the contract.
5. If the task derives from an iOS PR, cite the PR number + link in the
   task file and the row here.

## Anti-tasks

Do NOT open parity tasks for:

- **iOS CI/CD porting** — Android has its own GitHub Actions and Gradle
  pipeline. Do not copy Xcode Cloud, xctestplan, or Firebase deploy
  lanes from iOS.
- **iOS Cursor rules porting** — Android has its own `.cursor/rules/`.
- **Restoring the old `PRD/` folder** — the parity docs replace it.
- **Adding dark mode** — v1 is light-only for parity.
- **Apple Sign-In parity** — sanctioned platform swap to Google
  Sign-In; do not re-debate.
- **StoreKit / App Store metadata** — Android uses Play Billing and
  Play Console.
- **New product features not in iOS** — parity means matching, not
  extending. Open a design ticket, not a parity task.
