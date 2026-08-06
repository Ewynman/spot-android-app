# Design system (Android implementation)

## Purpose

Map PRD/iOS tokens to Compose files and document shared components.

## Audience

UI engineers and agents.

## Tokens

| Token | Hex / value | Compose |
| --- | --- | --- |
| Background | `#F5F3EF` | `SpotColors.Background` |
| Primary | `#1D2C24` | `SpotColors.Primary` |
| Button text | `#F5F3EF` | `SpotColors.ButtonText` |
| Accent (vibes only) | `#DEE6D8` | `SpotColors.Accent` |
| Welcome* | glow/surface/muted/line/chip | `SpotColors.Welcome*` |
| Pro gold | `#C9A24A` | `SpotColors.ProGold` |
| Padding H | 32 dp | `Dimensions.Padding.horizontal` |
| Spacing | 8 / 12 / 16 / 24 | `Dimensions.Spacing.*` |
| Radius | 10 / 12 / 20 | `Dimensions.Radius.*` |

Files: `core/design/theme/Color.kt`, `Type.kt`, `Theme.kt`, `Dimensions.kt`.

`SpotTheme` fills the full Material 3 light color scheme from Spot tokens so `colorScheme.onSurfaceVariant` / `outline` / etc. do not fall back to Material defaults.

## Shared components (`core/design/component/`)

| Component | Notes |
| --- | --- |
| `SpotCard` | iOS anatomy; test tags `spotCard.*` |
| `RotatingVibeTags` | Accent pill + optional `+N` rotation |
| `SkeletonSpotCard` / `SkeletonFeed` | 3 skeletons on initial feed load |
| `VibeChip` | Display / selectable |
| `Avatar` | Optional Pro gold ring |
| `TopNavigationView` | Heavy display wordmark |
| `EmptyFeedView` (+ variants) | Status-aware empty |
| `Toast` / `Banner` | Success / error / publish |
| `PermissionPrePrompt` | Neutral Continue before OS dialog |

## Images

- Private buckets: `SpotImageRequest` + Coil fetcher (`core/media/`)  
- Downscale ≤1600 px, JPEG ~0.8 before upload (post flow)  
- Signed URLs cached near 7-day expiry  

## Test tags

Prefer iOS identifier strings (`navigation.homeTab`, `home.feedRoot`, `welcome.screen`, …) via `Modifier.testTag`.

## Related

- [product/ui-parity.md](../product/ui-parity.md)
- [PRD/02-design-system.md](../PRD/02-design-system.md)
