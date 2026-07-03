# 15 — Deep Links (App Links & Custom Scheme)

Spot supports opening a **specific Spot** from a shared link and handling a **subscription return** via custom scheme. On iOS these are Universal Links + a `spotapp://` scheme; on Android use **App Links** (verified `https`) + intent filters for the custom scheme.

## Routes

Parsed into one of (mirror `DeepLinkRoute`): `spotDetail(spotId)`, `subscriptionReturn`, `unknown`.

| Link | Example | Behavior | Auth |
|------|---------|----------|------|
| Spot detail (https) | `https://spotapp.online/s/{spotId}` | Fetch spot → detail overlay or "unavailable" | Uses session if present; **stores pending** if logged out |
| Spot detail (www) | `https://www.spotapp.online/s/{spotId}` | Same | Same |
| Spot (custom scheme) | `spotapp://spot/{spotId}` | Same | Same |
| Spot (triple-slash) | `spotapp:///spot/{spotId}` | Same | Same |
| Spot (query) | `spotapp://open?spotId={id}` | Same | Same |
| Subscription return | `spotapp://subscription/return` | Triggers subscription success handling | — |
| Local dev | `http://localhost/s/{spotId}` | Debug only | — |

**Not implemented:** profile links (`/u/{username}`). Only `/s/{spotId}` spot links exist.

## Spot id validation

Reject invalid ids before fetching: non-empty, **max length 50**, characters `[a-zA-Z0-9_-]+` only.

## Behavior

- **Cold start**: capture the launch link, store as `pendingDeepLink`, and process after the session is ready (`processPendingDeepLinks()`).
- **Warm / authenticated**: fetch the spot → on success show a full-screen detail overlay (back bar + `SpotCard`); on failure show a **spot-unavailable** screen. A loading state shows while fetching.
- **Unauthenticated**: store the pending spot link; after sign-in, resolve it.
- **Debounce**: ignore a duplicate spotId within ~1 second.
- **Subscription return**: check `users.is_pro`; if now Pro, show the success screen (`ProSuccessView`).
- **Logout**: clear any pending deep-link session state.

## Overlays (above the tab shell)

- Spot detail overlay, spot loading, spot unavailable, subscription success. These float over the current tab (they are not tabs themselves).

## Sharing

- The share action produces `https://spotapp.online/s/{spotId}` (share base URL from config). Reuse this format on Android.

## Error handling

| Case | Behavior |
|------|----------|
| Spot not found / blocked / private | Spot-unavailable screen |
| Network error | Unavailable + logged |
| Invalid URL/path | `unknown` route, logged |

## Android configuration

1. **App Links (verified https):** host `assetlinks.json` at `https://spotapp.online/.well-known/assetlinks.json` (and `www`) with your package name + signing cert SHA-256. Add intent filters with `android:autoVerify="true"` for:
   - `https://spotapp.online/s/*`
   - `https://www.spotapp.online/s/*`
2. **Custom scheme:** intent filter for scheme `spotapp` (hosts `spot`, `open`, `subscription`).
3. Parse incoming `Intent` data in the single activity → resolve to a `DeepLinkRoute` → drive the shared deep-link state holder (mirror `DeepLinkState`), including pending-link storage when logged out.
4. Log deep-link events with an origin tag (`app_link` vs `custom_scheme`) for analytics.

## Analytics

Track each deep link with origin (universal/app-link vs custom scheme) and resolved route, mirroring `trackDeepLink`.
