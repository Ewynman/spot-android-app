# 14 — Notifications

Spot uses **local notifications** for social events (follow requests + accepts). There are intentionally **no like/comment notifications**. Remote push is a documented future enhancement.

## Permission request

- Requested **after** the first-run tour completes **or** is skipped (~600ms delay), only if status is not-yet-determined, via a neutral pre-prompt then the OS dialog.
- Android 13+ requires the `POST_NOTIFICATIONS` runtime permission.
- Denial never blocks the app.

## Notification categories & content

| Type | Category id | Actions | Payload keys |
|------|-------------|---------|--------------|
| Follow request received | `FOLLOW_REQUEST` | Accept, View | `type=follow_request`, `requester_uid`, `username` |
| Follow request accepted | `FOLLOW_ACCEPTED` | View Profile | `type=follow_accepted`, `acceptor_uid`, `username` |

- Content never includes sensitive data in the body (ids go in the payload/extras, not the visible text).
- Only delivered when notification authorization is granted.

### Copy examples
- Follow accepted: title "Follow Request Accepted", body "[username] accepted your follow request."

## What's implemented vs future

- **Follow request accepted** — fully client-side local notification: when User B accepts User A's request, B's client fetches B's username and posts a local notification targeted at A's experience path. ✅
- **Follow request received** — infrastructure ready but requires a **backend push** to notify the target in real time (the client can't know another user sent a request without polling/Realtime). Future: DB trigger on `follow_requests` insert → edge function → push. On Android this would use **FCM** with device tokens stored server-side (e.g. a `user_push_tokens` table). ⚠️ Not required for v1 parity.

## Handling taps / actions

Map notification actions to navigation events (mirror iOS `NotificationCenter` posts):
- Accept action → open Follow Requests and auto-accept.
- View action → open Follow Requests.
- View Profile → open the relevant user's profile.

On Android, encode these as intent extras / deep-link-style routes handled by the single activity, switching to the Profile tab (index 4) and opening the follow-requests screen as needed.

## Foreground behavior

- iOS shows banner + sound + badge in foreground. On Android, post notifications via `NotificationManagerCompat` with an appropriate channel; decide foreground presentation (heads-up channel importance).

## Android implementation notes

- Create notification **channels** for `FOLLOW_REQUEST` and `FOLLOW_ACCEPTED`.
- Use action buttons on the notification for Accept/View.
- Route taps through a deep-link-style handler to the Profile tab + follow-requests screen.
- If/when adding remote push: integrate **FCM**, store tokens server-side after permission grant, register token refresh, and add the DB trigger + edge function for follow-request-received.

## Security

- Notification actions require foreground activation (device unlock) for sensitive actions.
- RLS ensures a user can only accept follow requests directed at them.
- Never embed service-role keys client-side.
