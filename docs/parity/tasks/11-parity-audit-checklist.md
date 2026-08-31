# Task 11 — Full parity audit checklist pass

**Size:** Recurring (do after tasks 01–09 ship) • **Priority:** P3
**Deps:** 01–09 shipped • **Status:** Open

## Goal

Walk every user-facing surface end-to-end on Android and diff against
iOS. File follow-up tasks for anything that's off. Do not fix things in
this task — this is an **audit**, not a fix.

## Why it matters

Once the known gaps are closed, small drift is what breaks parity —
copy that says `Delete post` on Android but `Delete spot` on iOS, a
loading state that skips on one platform, a color that's 4 hex points
off. Regular audits catch these before users do.

## Contract

### Checklist

Per surface (from [`screen-map.md`](../screen-map.md)), verify:

- [ ] **Copy** matches (title, body, buttons, empty states, errors,
      toasts, dialog copy).
- [ ] **All four states** implemented: loading, empty, error,
      unauthenticated.
- [ ] **Test tags** match iOS `accessibilityIdentifier` values.
- [ ] **Limits** in `limits-constants.md` are honored (feed page 24,
      OTP 6/30s, publish 90s, image 1600px, etc.).
- [ ] **Gating** is correct: auth-required actions block signed-out
      users, email-verified actions block unverified users, Pro-required
      actions open the paywall.
- [ ] **Optimistic mutations** roll back on failure with an error toast.
- [ ] **No inline hex** in feature code; colors come from `SpotColors`.
- [ ] **No Supabase in composables/VMs** — all data through a
      repository.
- [ ] **Feed events** emitted through `FeedEventService`, coalesced.
- [ ] **Signed URLs** use `ImageUrlSigner` (7-day cache).
- [ ] **Logs** use `SpotLogger` per-area; no PII.

### Surfaces to walk

Take these in order to align with iOS's user journey:

1. Launch → Welcome → Sign up (email) → OTP → username setup → tab shell.
2. Launch → Welcome → Sign in with Google → tab shell (with username gate
   if new).
3. Home feed → scroll → like → bookmark (free + Pro flows) → tap card →
   open in map.
4. Map → filter (following, liked, Pro filters) → tap pin → preview →
   expand drawer → save / like.
5. Post composer → 3 steps → save draft → resume draft → publish → banner
   → success toast → new spot in home feed.
6. Edit spot (Pro) → sheet → change photos/vibes/location → save → feed
   reflects update. **(task 01)**
7. Search → users / locations / vibes segments → history → grid →
   expanded spot → bookmark (Pro picker). **(task 02)**
8. Profile → own + other → follow / unfollow / request → follow requests
   inbox → bookmarks → likes → collections (Pro).
9. Settings → all subsections → delete account → sign out.
10. Paywall → purchase → success → onboarding tour → Pro-gated actions
    re-check.
11. Safety → report spot → report profile → block user → confirm feed
    filters afterward.
12. Deep link → tap `https://spotapp.online/s/<id>` from another app →
    spot detail overlay. **(task 04)**
13. Notifications → follow-request received (push) → tap → follow
    requests inbox. **(task 03)**

### Per surface, file a task if you find

- Copy mismatch → new task, tiny.
- Missing state → new task, small.
- Missing test tag → include in a batched "test-tag alignment" task.
- Missing constant / magic number → new task, tiny.
- Broken behavior → new task, size based on severity.

## iOS reference

Anywhere ambiguous: read the corresponding iOS file from
[`screen-map.md`](../screen-map.md). If the sibling repo isn't checked
out, defer that item and mark it "needs iOS cross-check".

## Android target

**None directly.** This task produces new task files under
`docs/parity/tasks/` and rows in [`task-queue.md`](../task-queue.md), not
code.

## Acceptance criteria

- [ ] Every surface in the checklist above walked on a debug build.
- [ ] For every drift, a task file exists in `docs/parity/tasks/` with
      the standard sections (Goal, Contract, iOS ref, Android target,
      Acceptance criteria).
- [ ] `task-queue.md` updated with the new tasks in the right priority.
- [ ] A summary PR description lists all findings + linked task files.

## Test plan

- No code — just checklist evidence.
- Recommended cadence: once after 01–09 ship, then before every release.

## Out of scope

- Fixing anything you find. That's the follow-up tasks.
- Auditing the backend / RLS. That's a separate ops task.
- Performance testing (memory, launch time, frame rate). Separate task
  when we get there.

## Follow-ups

- Consider turning this checklist into a script that scrapes test-tag
  usage and diffs it against `screen-map.md` automatically.
