# Spot Android documentation

## Purpose

Central index for Android product, engineering, parity, and diagram docs.

**Building toward iOS parity?** Start at [`parity/README.md`](parity/README.md) —
that's where the current work lives, sized for Cursor Cloud Agents.

The parity docs replace the previous `PRD/` folder. Backend contracts and
behavior spec are captured under [`parity/`](parity/); this tree documents
**how Android implements** those contracts.

## Audience

New developers, reviewers, release owners, and Cursor agents working on the Android port.

## Current status

Documented against the **iOS parity mega** implementation on branch `feature/ios-parity-mega` (2026-08-05). Gaps vs iOS are called out explicitly.

## Start here

1. [Parity docs](parity/README.md) — the current source of truth for what to build
2. [Parity task queue](parity/task-queue.md) — open work, sized for Cloud Agents
3. [Parity constraints](parity/constraints.md) — non-negotiable rules
4. [Root README](../README.md) — setup, stack, constants
5. [Cursor project rules](../.cursor/rules/project.mdc) — non-negotiable golden rules
6. Pick a reading path below

## Parity

| Doc | Topics |
| --- | --- |
| [parity/README.md](parity/README.md) | Master brief, index, how to use |
| [parity/constraints.md](parity/constraints.md) | Non-negotiable rules distilled |
| [parity/limits-constants.md](parity/limits-constants.md) | Every numeric limit, sourced |
| [parity/data-contracts.md](parity/data-contracts.md) | RPCs, tables, storage, edge functions |
| [parity/design-tokens.md](parity/design-tokens.md) | Colors, typography, spacing, copy |
| [parity/screen-map.md](parity/screen-map.md) | iOS ↔ Android surface map |
| [parity/agent-brief.md](parity/agent-brief.md) | Drop-in prompt for a Cloud Agent |
| [parity/task-queue.md](parity/task-queue.md) | Open + shipped parity tasks |

## Product

| Doc | Topics |
| --- | --- |
| [product/overview.md](product/overview.md) | Surfaces, principles, Android package map |
| [product/onboarding.md](product/onboarding.md) | Welcome, auth gates, first-run coach |
| [product/ui-parity.md](product/ui-parity.md) | Visual contract vs iOS (SpotCard, Welcome, Profile, Search) |

## Engineering

| Doc | Topics |
| --- | --- |
| [engineering/architecture.md](engineering/architecture.md) | Layers, DI, session holder, overlays |
| [engineering/deep-links.md](engineering/deep-links.md) | App Links, `spotapp://`, pending store, overlays |
| [engineering/notifications.md](engineering/notifications.md) | Channels, FOLLOW_ACCEPTED local, FCM gap |
| [engineering/first-run-onboarding.md](engineering/first-run-onboarding.md) | Coach steps, DataStore keys, permission handoffs |
| [engineering/collections.md](engineering/collections.md) | Collections CRUD + Pro bookmark picker |
| [engineering/safety-overflow.md](engineering/safety-overflow.md) | Share / Delete / Report / Block menus |
| [engineering/design-system.md](engineering/design-system.md) | Theme tokens, SpotCard anatomy, shared components |

## Diagrams

| Doc | Contents |
| --- | --- |
| [diagrams/README.md](diagrams/README.md) | Index |
| [diagrams/deep-link-flow.md](diagrams/deep-link-flow.md) | Deep link → overlay |
| [diagrams/first-run-coach-flow.md](diagrams/first-run-coach-flow.md) | Coach + notification prompt |
| [diagrams/app-launch-auth-flow.md](diagrams/app-launch-auth-flow.md) | Launch gate |

## Suggested reading paths

**New developer:** [parity/README](parity/README.md) → [parity/constraints](parity/constraints.md) → [engineering/architecture](engineering/architecture.md) → [engineering/design-system](engineering/design-system.md).

**Cursor Cloud Agent:** [parity/agent-brief](parity/agent-brief.md) → pick a task from [parity/task-queue](parity/task-queue.md) → read the task file + [parity/constraints](parity/constraints.md) + [parity/limits-constants](parity/limits-constants.md) + [parity/data-contracts](parity/data-contracts.md).

**Deep-link / share QA:** [engineering/deep-links](engineering/deep-links.md) → [parity/tasks/04-verified-app-links](parity/tasks/04-verified-app-links.md) → [diagrams/deep-link-flow](diagrams/deep-link-flow.md).

**Onboarding QA:** [product/onboarding](product/onboarding.md) → [engineering/first-run-onboarding](engineering/first-run-onboarding.md).

## Related

- iOS reference docs: `../spot-ios-app/docs/` (when checked out beside this repo)
- Parity work: [`parity/`](parity/README.md) — replaces the previous `PRD/` and `BUILD_PROGRESS.md`

## Open follow-ups

Tracked in [`parity/task-queue.md`](parity/task-queue.md). Highlights:

- Task 01: Pro **Edit spot** sheet
- Task 02: Collection picker on Search/Profile bookmark paths
- Task 03: FCM remote "follow request received"
- Task 04: Hosted `assetlinks.json` for verified App Links
- Task 05: AlgorithmSnapshot ViewModel → repository layering
- Task 06: Real Firebase config + App Check init
