# Task 03 — FCM remote push for follow-request received

**Size:** Medium (3–8 h) • **Priority:** P1 • **Deps:** Task 06 (Firebase
config must be real) • **Status:** Open

## Goal

Deliver a **remote push notification** to the target user when someone
requests to follow their private account. Tapping the push opens the
follow requests screen with the requester pre-focused.

iOS only ships **local** notifications today — this is an intentional
Android improvement per PRD/14, sanctioned by the golden rules.

## Why it matters

Android already has a local `FOLLOW_ACCEPTED` notification (fires when
your request is accepted). But `FOLLOW_REQUEST` (received) has no
delivery path because there's no FCM wiring. Private-account users
therefore only see requests when they open the app.

## Contract

### Message shape (server → client)

FCM data-only message (no `notification` payload) so the client controls
presentation:

```json
{
  "data": {
    "type": "follow_request",
    "requester_uid": "<uuid>",
    "target_uid": "<uuid>",
    "username": "<requester_username>",
    "avatar_url": "<optional>"
  }
}
```

Server sends via a **backend function or trigger** — that's a backend
task, not this task. **This task ships the client side.** If the server
side doesn't exist yet, ship the client + document how a curl'd FCM
message can test it.

### Client presentation

- Channel: `FOLLOW_REQUEST` (already declared in
  `data/notifications/SpotNotificationService.kt`, `IMPORTANCE_HIGH`).
- Title: `New Follow Request`
- Body: `{username} wants to follow you`
- Tap intent: opens `MainActivity` with an extra
  `spot.deeplink=spotapp://follow-requests?requester=<uid>`, which the
  existing `DeepLinkCoordinator` should route.
- Foreground: still show the notification (do not silently swallow).
- Also update in-app state:
  - Increment `pendingFollowRequestCount` on `UserSessionHolder`.
  - Emit an event so `FollowRequestsViewModel` refreshes if visible.

### Token registration

- Register the FCM token on session establishment
  (`SessionBridge.onAuthenticated`) via a new `PushTokenRepository`
  that upserts into a `device_tokens` table (verify table name against
  server) with `{ user_id, token, platform: "android" }`.
- Unregister on sign-out.
- Refresh on `FirebaseMessagingService.onNewToken(...)`.
- Debounce duplicate uploads.

### App Check

- If Firebase App Check is not initialized (task 06), attest attempts to
  register the token will fail. Do not block auth — log the failure via
  `SpotLogger` under `LogCategory.Notifications` and continue.

## iOS reference (for reviewers)

- `../spot-ios-app/Spot/Services/Notifications/NotificationService.swift`
  (only fires **local** notifications on iOS — Android extends this.)
- `../spot-ios-app/Spot/AppDelegate.swift` — notification categories,
  deep-link routing on tap.

## Android target (files to touch)

Create:
- `data/notifications/SpotFirebaseMessagingService.kt` (extends
  `FirebaseMessagingService`).
- `data/notifications/PushTokenRepository.kt` (interface).
- `data/notifications/SupabasePushTokenRepository.kt` (real impl).
- `app/src/test/.../data/notifications/FakePushTokenRepository.kt`.
- Instrumented test to verify channel + tap intent.

Wire:
- Register the service in `AndroidManifest.xml`:
  ```xml
  <service android:name=".data.notifications.SpotFirebaseMessagingService"
      android:exported="false">
    <intent-filter>
      <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
  </service>
  ```
- Add token upload trigger in `SpotApplication` or `SessionBridge`.
- Add `firebase-messaging-ktx` to `libs.versions.toml` if not already.
- Reuse `SpotNotificationService.createNotificationChannels()` (already
  declares `FOLLOW_REQUEST`).

## Acceptance criteria

- [ ] Sending a test FCM message with the payload above renders a
      high-importance notification with the correct title/body/channel.
- [ ] Tap opens the app to Follow Requests with the requester pre-focused.
- [ ] Token registers on auth, unregisters on sign-out, and refreshes on
      `onNewToken(...)`.
- [ ] `UserSessionHolder.pendingFollowRequestCount` increments in
      foreground when a push arrives.
- [ ] Foreground push still shows the notification.
- [ ] Unit tests cover: token upsert, duplicate debounce, foreground
      dispatch.
- [ ] Instrumented test: sending a fake `RemoteMessage` produces a
      notification with the correct channel and tap PendingIntent.
- [ ] Logs use `LogCategory.Notifications`, no PII (username may be
      logged; UID **must** be redacted).

## Test plan

- `./gradlew testDebugUnitTest connectedAndroidTest`
- Manual: use the Firebase console or an admin script to send a test
  message with the payload above to your token; confirm delivery in
  both foreground and background.
- Manual: sign out and confirm the token row is removed
  (`device_tokens.token` gone for the user).

## Out of scope

- **Backend / server function** that emits the FCM message on a
  `follow_requests` insert. That's a separate backend task; note it in
  the PR description.
- Any other push categories (likes, mentions, etc.) — not part of iOS
  parity today.
- iOS parity: iOS only does local, so we're intentionally ahead of iOS
  here per PRD/14. Do not backport this to iOS as part of this task.

## Follow-ups

- If `device_tokens` schema differs from `{ user_id, token, platform }`,
  align with the actual table in the same PR.
- Add analytics: `push_received`, `push_opened`.
