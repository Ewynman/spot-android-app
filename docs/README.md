# Spot Android documentation

## Purpose

Central index for Android product, engineering, and diagram docs. Contracts and backend behavior live in [`PRD/`](../PRD/README.md); this tree documents **how Android implements** those contracts.

## Audience

New developers, reviewers, release owners, and Cursor agents working on the Android port.

## Current status

Documented against the **iOS parity mega** implementation on branch `feature/ios-parity-mega` (2026-08-05). Gaps vs iOS are called out explicitly.

## Start here

1. [Root README](../README.md) — setup, stack, constants  
2. [PRD index](../PRD/README.md) — product contracts (source of truth for behavior)  
3. [Cursor project rules](../.cursor/rules/project.mdc) — non-negotiable golden rules  
4. Pick a reading path below  

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

**New developer:** [architecture](engineering/architecture.md) → [design-system](engineering/design-system.md) → [PRD/01](../PRD/01-architecture-android.md) → [BUILD_PROGRESS](../BUILD_PROGRESS.md).

**Cursor agent:** [.cursor/rules/project.mdc](../.cursor/rules/project.mdc) → relevant `PRD/NN-*.md` → matching `docs/engineering/*` before coding.

**Deep-link / share QA:** [deep-links](engineering/deep-links.md) → [PRD/15](../PRD/15-deep-links.md) → [diagrams/deep-link-flow](diagrams/deep-link-flow.md).

**Onboarding QA:** [product/onboarding](product/onboarding.md) → [first-run-onboarding](engineering/first-run-onboarding.md) → [PRD/05](../PRD/05-auth-onboarding.md).

## Related

- iOS reference docs: `../spot-ios-app/docs/` (when checked out beside this repo)
- Build progress: [BUILD_PROGRESS.md](../BUILD_PROGRESS.md)

## Open follow-ups

- Pro **Edit spot** sheet (`update_spot_metadata_v1`) — overflow Share/Delete shipped; Edit UI not fully ported  
- Collection picker on Search/Profile bookmark paths (Home + Map wired)  
- Hosted `assetlinks.json` for verified App Links  
- FCM remote “follow request received”  
