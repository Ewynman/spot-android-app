# Design tokens

**Source of truth (Android):** `core/design/theme/Color.kt`,
`core/design/theme/Type.kt`, `core/design/Dimensions.kt`.

**Source of truth (iOS):** `../spot-ios-app/Spot/Utils/Constants.swift` →
`Constants.Colors`, `Constants.Layout`; `Spot/Utils/FontManager.swift`.

Both must stay in sync. If you find drift, fix it in the file that matches
the iOS reference below.

## Colors

Android and iOS already match. Never introduce a new color without matching
it in iOS first.

| Token | Hex | Where used |
|-------|-----|------------|
| Background | `#F5F3EF` | App cream background, button text on dark surfaces |
| Primary | `#1D2C24` | Primary text, primary buttons, deep-green pill fills |
| ButtonText | `#F5F3EF` | Text on primary buttons |
| Accent | `#DEE6D8` | **Vibe chips only** — do not use elsewhere |
| ProGold | `#C9A24A` | Pro badge indicators |
| MapMarkerGreen | `#1D2C24` | Map pin fill |
| MapMarkerDot | `#F5F3EF` | Map pin inner dot |
| MapMarkerStroke | `#0F1A14` | Map pin outline |
| MapFilterMatch | `#7AA382` | Active map filter highlight |
| WelcomeGlow | `#7AA382` | Welcome screen glow |
| WelcomeSurface | `#F9F7F1` | Welcome card surface |
| WelcomeMutedText | `#607064` | Muted welcome copy |
| WelcomeLine | `#AEB9AD` | Welcome dividers |
| WelcomeChipFill | `#EEF3EA` | Welcome chip background |
| Error (M3) | `#B3261E` | Material 3 error scheme |
| ErrorContainer (M3) | `#F9DEDC` | Error banner background |
| OnErrorContainer (M3) | `#410E0B` | Error text |

## Spacing / radius

**Source (Android):** `core/design/Dimensions.kt`  
**Source (iOS):** `Constants.Layout` in `Constants.swift`

| Token | Value |
|-------|-------|
| Horizontal padding | 32.dp |
| Padding vertical S / M / L / XL | 8 / 12 / 16 / 24 dp |
| Spacing S / M / L / XL | 8 / 12 / 16 / 24 dp |
| Radius S / M / L | 10 / 12 / 20 dp |

## Typography

**Android:** `core/design/theme/Type.kt` — Roboto (system default). Sizes:

| Role | Size | Weight |
|------|------|--------|
| displayLarge (SPOT wordmark) | 36 sp | Black |
| titleLarge | 22 sp | Bold |
| titleMedium | 18 sp | SemiBold |
| bodyLarge | 16 sp | Regular |
| bodyMedium | 14 sp | Regular |
| bodySmall | 12 sp | Regular |
| labelLarge / M / S | 14 / 12 / 10 sp | Medium |

**iOS reference:** SF Pro Rounded — logo 24 (Black), section headers 24
(Bold), primary text 12 (Regular), buttons 12 (SemiBold). Android intentionally
uses larger body text than iOS because Android density and Compose defaults
differ. **Do not shrink Android sizes to match iOS points 1:1.**

## Iconography

Prefer Material Icons Outlined via `androidx.compose.material.icons.outlined.*`.
For custom marks (SPOT wordmark, map pin), use the assets in
`app/src/main/res/`. No new custom vector assets without a design review.

## Copy

The following canonical strings must match iOS exactly. If you find a
mismatch, the iOS string wins.

| Context | String |
|---------|--------|
| Wordmark | `SPOT` |
| Search prompt | `Search users, locations, vibes` |
| Post success toast | `Spot posted!` |
| Post email gate | `Please verify your email to post a spot.` |
| Follow request notification title | `New Follow Request` |
| Follow request notification body | `{username} wants to follow you` |
| Follow accepted title | `Follow Request Accepted` |
| Terms URL | `https://spotapp.online/terms` |
| Privacy URL | `https://spotapp.online/privacy` |
| Support email | `support@spotapp.online` |
| Share URL pattern | `https://spotapp.online/s/{spotId}` |

## Motion

- Coach mark step transitions: standard `AnimatedContent` with fade + slide.
- Publish banner: slide-in from top of the tab shell, dismiss on tap or after
  success toast.
- Overlay entry (spot detail from deep link): fade + scale up, matching iOS
  full-screen overlay behavior — do **not** use the default push transition.

## Non-goals

- No dark mode (v1).
- No custom fonts (yet). Roboto system default is sufficient.
- No custom animations beyond what M3 + `AnimatedContent` provide.
