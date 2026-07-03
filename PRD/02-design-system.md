# 02 — Design System

Spot's look is **premium, minimal, calm** — cream background, deep forest-green primary, one soft-green accent reserved for vibe tags. Do not introduce off-brand colors; if a new token is needed, add it centrally.

## Color palette

Ported 1:1 from iOS `Constants.Colors`. Use these as your Compose theme tokens.

### Core

| Token | Hex | Usage |
|-------|-----|-------|
| `background` | `#F5F3EF` | Main app background (cream) |
| `primary` / `textPrimary` | `#1D2C24` | Buttons, icons, and all body text |
| `buttonText` | `#F5F3EF` | Label on dark/primary fills (same as background — never use as body text on cream) |
| `accent` | `#DEE6D8` | **Vibe-tag surfaces only** |

### Map

| Token | Hex / value | Usage |
|-------|-------------|-------|
| `mapMarkerGreen` | `#1D2C24` | Spot pin body |
| `mapMarkerDot` | `#F5F3EF` | Inner dot on pin (readability) |
| `mapMarkerStroke` | `#0F1A14` | Pin outline |
| `mapDensityFill` | `#1D2C24` @ 0.85 | Soft-cluster fill |
| `mapFilterMatch` | `#7AA382` | Filter-match highlight ring/badge |
| `mapSelectedGlow` | `#1D2C24` @ 0.20 | Selected pin glow |
| `proGold` | `#C9A24A` | Pro user-location avatar ring |
| `mapAvatarRing` | `#1D2C24` | Regular user-location avatar ring |
| `mapAvatarHalo` | `#1D2C24` @ 0.18 | Halo while location updating |

### Welcome screen

| Token | Hex |
|-------|-----|
| `welcomeGlow` | `#7AA382` |
| `welcomeSurface` | `#F9F7F1` |
| `welcomeMutedText` | `#607064` |
| `welcomeLine` | `#AEB9AD` |
| `welcomeChipFill` | `#EEF3EA` |
| `welcomeCardShadow` | `#1D2C24` @ 0.12 |

> The app is **light-theme only** in the reference. If you add dark mode on Android, treat it as net-new design work (not in scope for parity).

## Layout tokens (`Constants.Layout`)

| Group | Token | Value (dp on Android) |
|-------|-------|-----------------------|
| Padding | horizontal | 32 |
| Padding | verticalSmall / Medium / Large / XL | 8 / 12 / 16 / 24 |
| Spacing | small / medium / large / XL | 8 / 12 / 16 / 24 |
| Corner radius | small / medium / large | 10 / 12 / 20 |

(iOS values are in points; use the same numbers as dp.)

## Typography

The reference uses the system font (SF Pro) with an ALL-CAPS wordmark "SPOT" in the top nav. On Android use the platform default (Roboto) or a comparable clean sans; keep the uppercase wordmark. Establish Material 3 type roles mapping roughly to: display (wordmark), title (screen headers), body (captions/usernames), label (vibe chips, buttons).

## Vibe-tag catalog

Vibe tags are free-form strings validated client-side, but there is a **canonical default set of 18** shown in pickers, and the live `vibe_tags` table has **20 rows** (defaults may be seeded plus extras). Canonical defaults from iOS `Constants.VibeTags.defaultTags`:

```
Chill Spot, Hidden Gem, Scenic View, Romantic, Great For Photos,
Family Friendly, Nature Escape, Foodie Heaven, Beach Day, Late Night,
Historical, People Watching, Quiet Moment, Cozy Corner, Pet Friendly,
Adventure, Waterfront, Study Spot
```

Custom tags (Pro only) are added to the shared `vibe_tags` catalog keyed by `name_lower`.

**Validation** (`Constants.Limits` / `ValidationMessages`):
- Min length **2**: "Please use at least 2 characters."
- Max length **30**: "Please keep it under 30 characters."
- Blocked tag: "That tag isn't allowed."

## Vibe-chip styling

- Surface color: `accent` (`#DEE6D8`).
- Text: `primary` (`#1D2C24`).
- Rounded (radius ~large), compact padding. Used on spot cards, pickers, and filters.

## Pagination constants (`Constants.Pagination`)

| Token | Value |
|-------|-------|
| defaultPageSize | 24 |
| largePageSize | 100 |
| extraLargePageSize | 200 |
| maxPageSize | 500 |

## Component inventory (build these reusable composables)

| Component | Role |
|-----------|------|
| `SpotCard` | The core content card: header (avatar, username, Pro badge, vibe chips, location) → image gallery (aspect-aware, multi-image pager) → interaction bar (like, bookmark, overflow menu). Used in feed, map drawer, profile grids (expanded), deep-link overlay. |
| `SkeletonSpotCard` | Loading placeholder (feed shows 3). |
| `EmptyFeedView` | Empty-state with status-specific copy. |
| `TopNavigationView` | Top bar with "SPOT" wordmark. |
| Bottom tab bar | Custom 5-item bar with accessibility ids `navigation.homeTab` … `navigation.profileTab`. |
| Vibe chip / chip row | Selectable + display variants. |
| Avatar | Circular user image with optional Pro badge / colored ring. |
| Paywall sheet | Pro upsell (see [12-pro-subscription.md](12-pro-subscription.md)). |
| Report sheet / Block dialog | Safety flows (see [13-moderation-safety.md](13-moderation-safety.md)). |
| Permission pre-prompt | Neutral "Continue" screen before OS permission dialog. |
| Coach overlay | First-run tour spotlight steps (see [05-auth-onboarding.md](05-auth-onboarding.md)). |
| Map spot preview drawer | Bottom sheet with `SpotCard` inside; peek/expanded detents. |
| Toast / banner | Post-success toast, refresh-error toast, publish progress banner. |

## Accessibility

- Preserve stable identifiers used by tests where practical (e.g. `navigation.homeTab`, `home.feedRoot`, `welcome.screen`). On Android use `Modifier.testTag(...)` with the same string values so a shared test vocabulary is possible.
- Ensure sufficient contrast: `#1D2C24` text on `#F5F3EF` background passes; never render `buttonText` cream on cream.
- All interactive controls need content descriptions.

## Motion (map tuning — optional parity)

Selection spring, pin entry stagger, and region debounce values are listed in [07-map.md](07-map.md). Match if you want identical map feel; otherwise use tasteful defaults.
