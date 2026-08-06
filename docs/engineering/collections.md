# Collections (Android)

## Purpose

Document Pro bookmark collections and the picker shown on save.

## Audience

Engineers, QA.

## Contract

[PRD/10-profile-social.md](../PRD/10-profile-social.md), [PRD/12-pro-subscription.md](../PRD/12-pro-subscription.md).

## Surfaces

| Surface | Status |
| --- | --- |
| Profile → Your Bookmarks / Collections list + detail | Wired (`feature/collections/`, profile mode) |
| `CollectionPickerSheet` on Pro **save** | Wired on **Home** and **Map** |
| Search / Profile engagement bookmark | Same effect pattern not yet mirrored |

## Picker flow

1. User taps bookmark while **not** currently saved  
2. If free and at cap (50) → paywall (`bookmark_cap`)  
3. If Pro → optimistic save + emit `ShowCollectionPicker(spotId)`  
4. UI shows `CollectionPickerSheet` (add to existing / create)  

Effects: `HomeFeedEffect.ShowCollectionPicker`, `MapEffect.ShowCollectionPicker`.

## Code

- UI: `feature/collections/CollectionPickerSheet.kt`, `CollectionPickerViewModel`  
- Repo: `data/collections/`  
- DI: `CollectionsModule`  

## Related

- [PRD/12](../PRD/12-pro-subscription.md)
- Settings subscription entry may deep-link into Collections when Settings is open from profile  
