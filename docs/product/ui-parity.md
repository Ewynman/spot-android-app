# UI parity vs iOS

## Purpose

Document the visual contract Android targets when remaking Spot from the iOS app.

## Audience

Design, engineering, QA, agents doing UI work.

## Source of truth

1. Live iOS UI (`../spot-ios-app/Spot/`) — especially `Constants.swift`, `SpotCard.swift`, `WelcomeView.swift`, screenshots under `Images/`  
2. [PRD/02-design-system.md](../PRD/02-design-system.md)  

## What makes Spot look like Spot

1. Cream canvas `#F5F3EF` everywhere (not pure white as app ground)  
2. Forest green `#1D2C24` for text, icons, filled CTAs, pins, selected tabs  
3. Sage `#DEE6D8` **only** on vibe chips  
4. ALL-CAPS **SPOT** wordmark (heavy / black)  
5. Borderless feed cards — hierarchy from spacing + media, not Material elevation  
6. SpotCard lock: avatar/username · location → rounded photo → ♡ / bookmark / ⋮ · vibe pill  
7. Custom 5-tab cream bar with hairline  
8. Light-only, calm empty/skeleton states  

## SpotCard anatomy (shipped)

| Region | Behavior |
| --- | --- |
| Header | 32 dp avatar + semibold username (+ Pro) left; city/state location right (`cityStateFromLocation`) |
| Media | Aspect-aware pager, **12 dp** rounded clip, white page dots |
| Actions | Heart (red when liked, gray idle), bookmark, more — **no likes count** |
| Vibes | `RotatingVibeTags` on the right; multi-vibe sheet on tap |

Images prefer `SpotImageRequest` / storage paths when present; else signed/public URL strings.

Files: `core/design/component/SpotCard.kt`, `RotatingVibeTags.kt`, `SkeletonSpotCard.kt`.

## Welcome

- Serif headline + muted supporting copy matching iOS  
- Hero collage using Welcome tokens (`WelcomeGlow`, chip fill, etc.)  
- CTA order: Get started → Log in → Google  

File: `feature/auth/WelcomeScreen.kt`.

## Profile

- Centered 100 dp avatar → username (+ Pro pill) → “N spots shared”  
- Own profile: outlined **Menu** pill  
- Underline **Spots | Map** tabs (not Material segmented)  
- Follow: primary-filled capsule  
- Grid: square tiles + solid primary location footer bar  

File: `feature/profile/ProfileComponents.kt`.

## Search

- Underline Users / Locations / Vibes segments  
- Search field placeholder: “Search users, locations, vibes”; primary stroke  

File: `feature/search/SearchComponents.kt`.

## Theme

`SpotTheme` maps full Material 3 roles to Spot tokens so Settings/Paywall/Collections do not leak default purple/gray. See [design-system.md](../engineering/design-system.md).

## Known remaining UI gaps

- Pro Edit-spot composer chrome  
- Coach spotlight geometry (guided steps use bottom card + scrim; full cutout anchors are simplified vs iOS `CoachMarkOverlay`)  
- Collection picker not yet on every bookmark surface (Home + Map done)  
