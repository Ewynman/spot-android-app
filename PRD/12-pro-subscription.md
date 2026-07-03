# 12 — Pro / Subscription

Spot **Pro** is a paid auto-renewing subscription that unlocks premium capabilities. On iOS it's StoreKit product id **`spotPro`** (yearly). On Android, map this to **Google Play Billing**.

## Entitlement source of truth

- Server: `users.is_pro` (bool) + `users.pro_until` (timestamptz).
- Client resolves the **effective** Pro state from the active store entitlement AND syncs `is_pro`/`pro_until` on the server after purchase/restore.
- All Pro gates read the client's resolved Pro flag (mirror `AuthViewModel.isPro`).

## Products

| iOS | Kind | Android mapping |
|-----|------|-----------------|
| `spotPro` | Yearly auto-renewing | Create a Play **subscription** with a base plan (yearly). Store the product id/base plan id in config. |

Pricing is **localized by the store** (`Product.displayPrice` on iOS; `ProductDetails` formatted price on Android) — never hardcode prices.

## Paywall

Presented via a "show paywall" event from many entry points. Contents:

**Feature list shown on the paywall:**
- Custom vibe tags
- Up to 5 images per spot
- Edit spots after posting
- Unlimited bookmarks
- Collections for bookmarks
- Advanced search filters
- Supporter badge

**Actions:** Subscribe, Restore, Try Again (retry product load), Terms / Privacy links.

## Entry points (trigger the paywall)

- **Bookmark cap (50)** reached on a spot card.
- Profile menu **Go Pro**.
- Settings → Subscription.
- Post composer: adding a **custom vibe tag** as a non-Pro.
- Spot card **Edit** or **Add to Collection** as a non-Pro.
- (Map filters are **hidden** for non-Pro, not a paywall trigger.)

## Purchase flow [ANDROID DECISION]

iOS: StoreKit 2 `purchase` with an `appAccountToken` set to the user's UUID so entitlements can be tied to the account; checks `Transaction.currentEntitlements`; a background listener on `Transaction.updates` keeps state fresh; entitlement linked to a **different** account is denied.

Android (Google Play Billing v6+):
1. Query `ProductDetails` for the Pro subscription; show localized price on the paywall.
2. Launch billing flow with `obfuscatedAccountId` = the user's Supabase UUID (analog of `appAccountToken`).
3. On purchase, **verify server-side** (Play Developer API or a Supabase edge function) and set `users.is_pro = true`, `users.pro_until = <expiry>`.
4. Acknowledge the purchase.
5. Listen to `PurchasesUpdatedListener` + query purchases on app start/foreground to refresh entitlement.
6. **Account binding**: if the purchase's obfuscated account id doesn't match the current user, treat Pro as not granted for this account (mirror iOS).
7. **Restore**: query existing purchases and re-apply entitlement.

> Because entitlement is written to `users.is_pro`, a user who is Pro on iOS is Pro on Android too (shared backend). Keep write authority server-verified to prevent spoofing.

## Post-purchase onboarding

After first successful purchase, show a Pro onboarding tour. Steps (mirror `PostPurchaseProOnboardingManager`):
```
welcome → fivePhotos → customVibes → editSpots → bookmarks → collections
→ searchFilters → supporterBadge → finale
```
Persist "seen" per user (key analogous to `hasSeenPostPurchaseProOnboarding.<userId>`).

Also show a **subscription success** screen (`ProSuccessView`) when returning from checkout / on confirmed purchase (also reachable via the `spotapp://subscription/return` deep link — see [15-deep-links.md](15-deep-links.md)).

## Pro-gated feature matrix

| Feature | Free | Pro |
|---------|------|-----|
| Images per post | 1 | 5 |
| Vibes per post | 1 | 5 |
| Custom vibe tags | ✗ | ✓ |
| Edit spot after posting | ✗ | ✓ |
| Bookmarks | max 50 | unlimited |
| Bookmark collections | ✗ | ✓ |
| Map filters (vibe/saved/liked/following) | hidden | ✓ |
| Search location+vibe advanced filter | ✗ | ✓ |
| Pro badge on profile/cards | ✗ | ✓ |
| Map user-location avatar ring | green | gold |

## States

- **Loading**: product fetch, purchase in progress, restoring.
- **Error**: product load failed (Try Again), purchase failed/canceled, verification failed.
- **Success**: entitlement granted → success screen + onboarding.

## Rule

Do **not** alter existing Pro tour / paywall behavior beyond what's needed for the Android platform mapping. Keep gating identical so both platforms behave the same against shared data.
