# Deep links & App Links (Android)

## Purpose

Document Android deep-link parsing, pending storage, overlays, and verification setup.

## Audience

Engineers, release owners, QA debugging share links.

## Contract

Mirror [PRD/15-deep-links.md](../PRD/15-deep-links.md) and iOS `DeepLinkRouter` / `DeepLinkState`.

## Supported routes

| Link | Example | Behavior |
| --- | --- | --- |
| Spot (https) | `https://spotapp.online/s/{id}` | Fetch → detail overlay or unavailable |
| Spot (www) | `https://www.spotapp.online/s/{id}` | Same |
| Spot (scheme) | `spotapp://spot/{id}` | Same |
| Spot (triple-slash) | `spotapp:///spot/{id}` | Same |
| Spot (query) | `spotapp://open?spotId={id}` | Same |
| Subscription return | `spotapp://subscription/return` | If `isPro`, show Pro success overlay |
| Local http | `http://localhost/s/{id}` | Debug parse only |

**Not implemented:** `/u/{username}` profile links.

### Spot ID validation

Non-empty, max length **50**, charset `[a-zA-Z0-9_-]+`.

## Implementation

| Piece | Location |
| --- | --- |
| Parser | `data/deeplink/DeepLinkRouter.kt` (pure JVM `java.net.URI`) |
| Pending store | `data/deeplink/PendingDeepLinkStore.kt` (DataStore) |
| Fetch | `data/deeplink/SpotDetailRepository.kt` → spots + images + author |
| Orchestration | `data/deeplink/DeepLinkCoordinator.kt` |
| DI | `di/DeepLinkModule.kt` |
| Entry | `MainActivity` — auth-callback vs deep link vs notification extras |
| UI | `feature/overlay/OverlayScreens.kt` + `OverlayHost` |
| Manifest | App Links `autoVerify` for `/s/*`; scheme `spotapp` |

### Behavior

1. **Authenticated:** show loading → fetch → `SpotDetail` overlay with `SpotCard`, or unavailable  
2. **Logged out:** persist URI; after auth, `processPending`  
3. **Debounce:** ignore duplicate spotId within ~1 s  
4. **Analytics:** `AnalyticsTracker.trackDeepLink(origin, route)` with `DeepLinkOrigin.AppLink` / `CustomScheme`  

Share action builds `https://spotapp.online/s/{spotId}` (see [safety-overflow.md](safety-overflow.md)).

## Verification (release)

Host Digital Asset Links at:

- `https://spotapp.online/.well-known/assetlinks.json`
- `https://www.spotapp.online/.well-known/assetlinks.json`

Include package name + release signing cert SHA-256. Manifest already sets `android:autoVerify="true"`.

**Hosting `assetlinks.json` is outside this Android repo** — track as release checklist item ([PRD/17](../PRD/17-non-functional-testing.md)).

## Testing

Unit: `DeepLinkRouterTest` (JVM).  

Manual: `adb shell am start -a android.intent.action.VIEW -d "https://spotapp.online/s/<uuid>" com.spot.android`.

## Related

- [diagrams/deep-link-flow.md](../diagrams/deep-link-flow.md)
- iOS: `../spot-ios-app/docs/engineering/universal-links.md`
