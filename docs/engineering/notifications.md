# Notifications (Android)

## Purpose

Document local notification channels, permission timing, and known push gaps.

## Audience

Engineering, QA, product.

## Contract

[PRD/14-notifications.md](../PRD/14-notifications.md). Remote push for inbound follow requests is **future** (needs FCM + server).

## Channels

Created at app start (`SpotApplication` → `SpotNotificationService.ensureChannels()`):

| Channel id | Importance | Use |
| --- | --- | --- |
| `FOLLOW_REQUEST` | HIGH | Infrastructure ready; no inbound push yet |
| `FOLLOW_ACCEPTED` | DEFAULT | Local notify after accept |

## Permission

- Requested **after** first-run coach completes **or** is skipped (~600 ms), via existing `PermissionsViewModel` + notification pre-prompt  
- Android 13+: `POST_NOTIFICATIONS`  
- Denial never blocks the app  

## Follow request accepted (v1)

When the current user accepts a follow request (`FollowRequestsViewModel.acceptRequest`):

1. Repository accept succeeds  
2. `SpotNotificationService.notifyFollowAccepted(username, acceptorUid)` posts a local notification  

Same-device limitation as iOS: this fires on the **acceptor’s** device. It is not a substitute for notifying the requester on another device.

Tap extras (`EXTRA_NOTIFICATION_TYPE`, `EXTRA_USER_ID`) are handled in `MainActivity` → Profile tab + `ProfileNavigationBus.openProfile`.

## Follow request received

Requires backend push (DB trigger → edge function → FCM). Channel exists; no client call site for inbound delivery.

## Related

- [first-run-onboarding.md](first-run-onboarding.md)
- iOS: `../spot-ios-app/docs/engineering/notifications.md`
