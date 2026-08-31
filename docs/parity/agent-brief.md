# Cloud Agent brief

**Paste this as the prompt when launching a Cursor Cloud Agent to work on a
parity task.** Fill in `{TASK_FILE}` with the specific task from
[`task-queue.md`](task-queue.md).

Everything the agent needs is in this repo. It does **not** need the iOS
repo checked out — each task file inlines the contract. If a task says
"see `../spot-ios-app/...`", that's a pointer for humans reviewing the PR;
the agent should rely on the inlined contract and, if uncertain, comment
"needs iOS source cross-check" in the PR description and stop.

---

## Cloud Agent prompt (copy-paste this)

````
You are a Cursor Cloud Agent working on the Android Spot app. Your goal is
to implement ONE parity task end-to-end and open a PR.

## Repo
- Root: (current working directory)
- Target file for this task: `docs/parity/tasks/{TASK_FILE}`

## Read these before touching code (in order)
1. `docs/parity/tasks/{TASK_FILE}` — your specific scope, acceptance criteria,
   and files to touch.
2. `docs/parity/constraints.md` — non-negotiable rules. Break these and the
   PR is rejected.
3. `docs/parity/limits-constants.md` — every numeric constant. Use these,
   don't invent numbers.
4. `docs/parity/data-contracts.md` — RPCs, tables, storage, edge functions.
5. `docs/parity/design-tokens.md` — colors, typography, spacing, copy.
6. `.cursor/rules/project.mdc` — authoritative repo rules (if it exists).
7. The task file may point you at specific Android files to read for context.
   Read those before editing.

## How to work
- Do only what the task file scopes. If you spot other issues, note them in
  the PR description under "Follow-ups"; do not fix them.
- Keep the diff small and reviewable. Split into commits if it helps.
- Follow the existing package layout: `core/`, `data/`, `di/`, `feature/`,
  `navigation/`. Do not invent new top-level packages.
- All Supabase access goes through a repository interface. If your task
  needs a new one, add both the real impl AND a fake under
  `app/src/test/java/.../data/**/Fake<Name>Repository.kt`.
- Reuse shared components: `SpotCard`, `Avatar`, `VibeChip`, `SkeletonSpotCard`,
  `EmptyFeedView`, `Toast`, `TopNavigationView`, `PermissionPrePrompt`,
  `PaywallSheet`. Never inline hex colors.
- Test tags: use the iOS accessibility identifier strings. See
  `docs/parity/screen-map.md` for the vocabulary.

## Definition of done
- ✅ Feature matches the task's inlined contract (states, copy, constants).
- ✅ Loading / empty / error / unauthenticated states all handled.
- ✅ ViewModel unit tests using fakes pass in `app/src/test/`.
- ✅ `./gradlew assembleDebug lintDebug testDebugUnitTest` all green.
- ✅ No hardcoded secrets; anon key only.
- ✅ No new PII in logs (use `SpotLogger` per-area categories).
- ✅ No inline hex colors; no direct Supabase calls from composables/VMs.
- ✅ Test tags match iOS strings.

## Deliverable
1. A single PR titled `parity/{TASK_FILE_STEM}: <one-line summary>`.
2. PR description contains:
   - **Scope:** what shipped
   - **Acceptance criteria met:** copy the task's checklist and check the
     boxes
   - **Screenshots / recordings:** for any UI change
   - **Verification:** the exact commands you ran and their status
   - **Follow-ups:** anything you noticed but did NOT fix
3. Update `docs/parity/task-queue.md` — move this task from "Open" to
   "Shipped" with the PR link.

## When to stop
- If the task file says the RPC/contract needs backend confirmation, DO NOT
  ship a client-side workaround. Stop, describe what's needed in the PR as
  a draft, and comment "needs backend work first".
- If the acceptance criteria conflict with `constraints.md`, `constraints.md`
  wins. Stop and describe the conflict in the PR description.
- If you cannot compile or tests fail after 3 targeted fix attempts, stop
  and open a draft PR describing what you tried and what's blocking.

## Start now
1. Open `docs/parity/tasks/{TASK_FILE}` and read it fully.
2. Read the four references listed above.
3. State your understanding of the task in one paragraph, then implement.
````

---

## Notes for the human launching the agent

- **Cloud Agents run on a single repo.** They cannot read
  `../spot-ios-app/`. Every task file must inline the contract it needs —
  see the existing task files as examples.
- **One task per Cloud Agent per PR.** Don't ask a single agent to bundle
  multiple tasks; the review surface gets too large.
- **Prefer main branch as base.** The agent will create a feature branch.
  When the PR is approved, squash-merge into main.
- **When adding a new task**, follow the same structure as
  [`tasks/01-edit-spot-sheet.md`](tasks/01-edit-spot-sheet.md): Goal, Why
  it matters, Contract (inlined), iOS reference (for reviewers), Android
  target (files to touch), Acceptance criteria (checklist), Test plan,
  Out of scope, Follow-ups. Then add an entry in
  [`task-queue.md`](task-queue.md).

## Notes for the agent

- **Ask before inventing.** If a task file is silent on a decision (copy
  string, color, animation), first check `design-tokens.md`, then check
  the iOS reference path in the task file (if the sibling repo is checked
  out), then default to the safest Android idiom and note the choice in
  the PR description under "Decisions".
- **Do not touch `.cursor/rules/project.mdc`.** It's the authoritative
  rulebook. If you think a rule should change, open a separate issue.
- **Do not delete PRD/ or restore it.** The parity docs replace it
  intentionally.

## Escalations

If you (the agent) hit any of these, stop and open a draft PR with a
clear description:

- The Supabase project doesn't have an RPC the task requires.
- An iOS behavior seems ambiguous and no reviewer can be reached.
- The task requires a design decision (new color, new copy string) that
  isn't in `design-tokens.md`.
- A test fails in a way that suggests a real backend or schema issue.
