# Product overview (Android)

## Purpose

Orient readers to Spot on Android: what the app is, main surfaces, and where code lives.

## Audience

Product, engineering, QA, agents.

## Principles

Same as iOS / [PRD/00](../PRD/00-overview.md):

- Same Supabase backend (`aeurigbbohyxvtsfiyul`); RLS is authoritative  
- Vibe-centered discovery; cream + forest-green UI  
- Safety non-negotiable (terms, moderation, report/block)  
- Limits match iOS (feed 24, free bookmark 50, etc.)  

## Surfaces (5-tab shell)

| Tab | Route | Feature package |
| --- | --- | --- |
| Home | `home` | `feature/home` |
| Map | `map` | `feature/map` |
| Post | `post` | `feature/post` |
| Search | `search` | `feature/search` |
| Profile | `profile` | `feature/profile` |

Overlays above the shell: paywall, Pro success/onboarding, deep-link spot loading/detail/unavailable, first-run coach, permission pre-prompts, safety sheets.

## Auth & gates

Launch gate (`feature/launch`) routes: Splash → Welcome / Confirm email / Username setup / Terms update → Main shell.

See [onboarding.md](onboarding.md) and [PRD/05](../PRD/05-auth-onboarding.md).

## Package layout

```
com.spot.android/
├── core/          # design, media, logging, analytics, supabase, util
├── data/          # repositories, DTOs, models, deeplink, notifications, onboarding prefs
├── feature/       # screen + ViewModel per product surface
├── navigation/    # SpotShell, overlays, buses
└── di/            # Hilt modules
```

Layering: **UI (Compose) → ViewModel → Repository interface → Supabase**. No Supabase calls from composables or ViewModels directly.

## Related

- [ui-parity.md](ui-parity.md)
- [engineering/architecture.md](../engineering/architecture.md)
- [PRD/01-architecture-android.md](../PRD/01-architecture-android.md)
