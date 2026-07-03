# 11 — Settings

Reached from Profile → overflow → Settings. A list of sections, each opening a detail screen.

## Sections & rows

| Section | Row | Destination |
|---------|-----|-------------|
| Account | Account settings | Account detail |
| Security | Security options | Security detail |
| Subscription | Subscription & Pro | Subscription detail (see [12-pro-subscription.md](12-pro-subscription.md)) |
| Permissions | Permissions | Permissions detail |
| Support | Contact Support | mailto `support@spotapp.online` |
| Legal | Legal documents | Legal detail (Terms + Privacy) |
| Debug (debug builds only) | Console logging | Logging settings |
| Debug | Algorithm snapshot | Algorithm debug view |

A **warning badge** appears on the Permissions parent row when any permission needs attention.

## Account settings detail

- Edit **username**, **display name**, **email**, **profile photo** (photo picker → moderated upload as a `profile_image` media asset → set `users.profile_image_url` / `profile_image_asset_id`).
- **Change password** (current / new / confirm).
- **Logout** → sign out (clears session; return to Welcome).
- **Delete account** (Play/App-store required, Guideline 5.1.1(v)):
  - Confirmation toggle: "I understand this permanently deletes my account."
  - **Re-auth** before deletion: password, **or** for OAuth-only accounts re-authenticate via the OAuth provider (Apple/Google) — no password required.
  - Calls RPC **`delete_my_account`** (purges user rows + storage prefixes in `avatars`/`spots`).

## Security settings detail

- **Change password** (current / new / confirm).
- **Private Account** toggle → set `users.is_private` (via sync RPC path).
- **Blocked Users** → blocked-users screen: lists blocked users (resolved via `users_public`, which includes users you block), **Unblock** per row (delete `user_blocks`), loading/empty states.

## Subscription settings detail

- Pro status + expiry (`pro_until`).
- **Go Pro** → paywall.
- **Manage subscription** → open the store subscription management ([ANDROID DECISION]: Google Play subscriptions deep link).
- **Restore purchases**.
- **Collections** link (Pro).

## Permissions settings detail

- Rows for each permission type: **location, notifications, camera, photos** (in that order).
- Show current status; when denied, offer **"Open Settings"** (Android app settings intent).

## Legal detail

- **Terms of Use**: `https://spotapp.online/terms`.
- **Privacy Policy**: `https://spotapp.online/privacy`.
- **Support email**: `support@spotapp.online`.

Confirm store listing metadata uses the same URLs/email.

## Debug (debug builds only)

- **Console logging** screen toggles structured-log categories. Keys to mirror (DataStore): `debugLoggingEnabled`, `logAllDebugCategories`, `logSpotCard`, `logPrivacy`, `logFeedComponent`, `logPostFlow`, `logAuth`, `logNetworkComponent`, `logDeepLink`.
- **Algorithm snapshot** shows the raw `user_feed_profiles.profile` for the current user (debugging the ranker).

## States

- **Loading**: while fetching Pro status / blocked users / permission states.
- **Empty**: no blocked users → "You haven't blocked anyone."
- **Error**: network failures on password change, email change, delete, etc.

## Android implementation notes

- Use `PreferenceScreen`-style Compose lists or your own settings list.
- Account deletion must be reachable within a few taps from a logged-in state (store policy).
- Gate debug section behind `BuildConfig.DEBUG`.
