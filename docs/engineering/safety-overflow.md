# Safety overflow menus (Android)

## Purpose

Document Spot and profile overflow actions (share, delete, report, block).

## Audience

Engineers, QA, safety reviewers.

## Contract

[PRD/06-home-feed.md](../PRD/06-home-feed.md) overflow matrix, [PRD/13-moderation-safety.md](../PRD/13-moderation-safety.md).

## Host

`SafetyFlowHost` + `SafetyViewModel` provide `LocalSafetyActions.openSpotOverflowMenu(spot)`.

## Spot menu

| Actor | Actions |
| --- | --- |
| Anyone | **Share** → `ACTION_SEND` with `https://spotapp.online/s/{id}` |
| Owner | **Delete** → `ProfileRepository.deleteOwnSpot` + local content removal bus |
| Non-owner | **Report**, **Block User** → existing report/block sheets |

Owner detection: `sessionBridge.currentUserId == spot.userId`.

## Profile menu (other users)

Report User, Block User (unchanged).

## Not yet ported

- **Edit** (Pro) → `update_spot_metadata_v1` edit sheet  
- Share from profile overflow  

## Related

- [deep-links.md](deep-links.md) (share URL format)
- [PRD/13](../PRD/13-moderation-safety.md)
